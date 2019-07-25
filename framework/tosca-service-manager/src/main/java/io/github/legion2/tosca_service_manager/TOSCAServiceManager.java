/*
 * Copyright 2019 Leon Kiefer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.legion2.tosca_service_manager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.opentosca.container.client.ContainerClient;
import org.opentosca.container.client.ContainerClientBuilder;
import org.opentosca.container.client.model.Application;
import org.opentosca.container.client.model.ApplicationInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auto.service.AutoService;

import io.github.amyassist.amy.core.di.Configuration;
import io.github.amyassist.amy.core.di.ServiceProviderLoader;
import io.github.legion2.tosca_service_manager.model.TOSCAService;
import io.github.legion2.tosca_service_manager.model.TOSCAServices;

/**
 * The External Service Manager for TOSCA based Services. It connects via the TOSCA client to a OpenTOSCA Runtime.
 * 
 * @author Leon Kiefer
 */
@AutoService(ServiceProviderLoader.class)
public class TOSCAServiceManager implements ServiceProviderLoader {

	private static final String ENV_CONFIGURATION_FILE_PATH = "TOSCA_SERVICE_MANAGER_CONFIG_FILE";
	private static final String DEFAULT_CONFIGURATION_FILE_PATH = "tosca-service-manager.properties";
	private static final String DEFAULT_DEPLYMENT_DESCRIPTOR_PATH = "TOSCAServices.xml";
	private static final String DEFAULT_MANAGER_ADAPTER_BASEPATH = ".tosca-adapters";

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	private ContainerClient containerClient;

	private Path adapterBasePath;

	@Override
	public Set<ClassLoader> load(Configuration configuration, ClassLoader classLoader) {
		if (classLoader != this.getClass().getClassLoader())
			return null;
		Properties properties = this.readConfig();

		this.adapterBasePath = Paths
				.get(properties.getProperty("manager.adapter.basepath", DEFAULT_MANAGER_ADAPTER_BASEPATH));

		String containerHost = properties.getProperty("tosca.container.host");
		Integer port = Integer.parseInt(properties.getProperty("tosca.container.port"));
		this.containerClient = new ContainerClientBuilder().withHostname(containerHost).withPort(port).build();

		this.setupArtifactDirectory();
		List<TOSCAService> requiredApplications = this.readTOSCAServiceDeploymentDescription(
				properties.getProperty("manager.servcies.file", DEFAULT_DEPLYMENT_DESCRIPTOR_PATH));

		List<Application> availableApplications = this.containerClient.getApplications();
		Map<TOSCAService, Application> bindApplications = this.bindApplications(requiredApplications,
				availableApplications);

		Map<TOSCAService, ApplicationInstance> provisionApplications = this.provisionApplications(bindApplications);

		Map<TOSCAService, Map<String, String>> bondaryDefintionProperties = this
				.getBondaryDefintionProperties(provisionApplications);

		for (Map<String, String> map : bondaryDefintionProperties.values()) {
			map.forEach((key, value) -> configuration.registerContextProvider(key, bla -> value));
		}

		return downloadApplicationConnectors(bindApplications);
	}

	private Properties readConfig() {
		String configPath = System.getenv(ENV_CONFIGURATION_FILE_PATH);
		if (configPath == null) {
			configPath = DEFAULT_CONFIGURATION_FILE_PATH;
		}
		Path path = Paths.get(configPath);

		Properties properties = new Properties();
		try (InputStream reader = Files.newInputStream(path)) {
			properties.load(reader);
		} catch (IOException e) {
			this.logger.error("Error loading config file", e);
		}
		return properties;
	}

	private void setupArtifactDirectory() {
		if (Files.exists(this.adapterBasePath)) {
			try (Stream<Path> stream = Files.walk(this.adapterBasePath)) {
				stream.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
			} catch (IOException e) {
				this.logger.error("Could not clear the Adapter Base Path directory", e);
			}
		}

		try {
			Files.createDirectories(this.adapterBasePath);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private List<TOSCAService> readTOSCAServiceDeploymentDescription(String path) {
		JAXBContext jc;
		Unmarshaller unmarshaller;
		try {
			jc = JAXBContext.newInstance(TOSCAServices.class);
			unmarshaller = jc.createUnmarshaller();
		} catch (JAXBException e1) {
			throw new IllegalStateException(e1);
		}

		try (InputStream resourceAsStream = Files.newInputStream(Paths.get(path))) {
			TOSCAServices toscaServices = (TOSCAServices) unmarshaller.unmarshal(resourceAsStream);
			return toscaServices.TOSCAServices;
		} catch (IOException | JAXBException e) {
			throw new IllegalStateException("Could not read the deployment descriptor", e);
		}
	}

	private Map<TOSCAService, Application> bindApplications(List<TOSCAService> tOSCAServiceDescriptions,
			List<Application> applications) {
		Map<TOSCAService, Application> binding = new HashMap<>();

		for (final TOSCAService toscaServiceDescription : tOSCAServiceDescriptions) {
			for (final Application application : applications) {
				if (application.getId().equals(toscaServiceDescription.id)) {
					if (binding.containsKey(toscaServiceDescription)) {
						throw new IllegalArgumentException(
								"Multiple Applications match the identifier: " + toscaServiceDescription);
					}
					binding.put(toscaServiceDescription, application);
				}
			}
			if (!binding.containsKey(toscaServiceDescription)) {
				throw new IllegalStateException("No Applications match the identifier: " + toscaServiceDescription.id);
			}
		}

		return binding;
	}

	private Map<TOSCAService, ApplicationInstance> provisionApplications(
			Map<TOSCAService, Application> bindApplications) {
		Map<TOSCAService, ApplicationInstance> dynamicBindings = new HashMap<>();

		for (Entry<TOSCAService, Application> binding : bindApplications.entrySet()) {
			TOSCAService toscaService = binding.getKey();
			Application application = binding.getValue();
			ApplicationInstance applicationInstance;
			if (toscaService.instanceId != null && !toscaService.instanceId.isEmpty()) {
				applicationInstance = this.containerClient.getApplicationInstance(application, toscaService.instanceId)
						.orElseThrow(() -> new IllegalArgumentException("Application Instance "
								+ toscaService.instanceId + " of Application" + application.getId() + " not found!"));
			} else {
				List<ApplicationInstance> applicationInstances = this.containerClient
						.getApplicationInstances(application);
				if (applicationInstances.isEmpty()) {
					Map<String, String> collect = Optional.ofNullable(toscaService.inputProperties)
							.orElse(Collections.emptyList()).stream()
							.collect(Collectors.toMap(inProp -> inProp.key, inProp -> inProp.value));
					applicationInstance = this.containerClient.provisionApplication(application, collect);
				} else {
					if (applicationInstances.size() > 1) {
						this.logger.info("Multipe ApplicationsInstances found, selecting one");
					}
					applicationInstance = applicationInstances.get(0);
				}
			}
			dynamicBindings.put(toscaService, applicationInstance);
		}

		return dynamicBindings;
	}

	@SuppressWarnings("resource")
	private Set<ClassLoader> downloadApplicationConnectors(Map<TOSCAService, Application> bindApplications) {
		Set<ClassLoader> classLoaders = new HashSet<>();
		for (Entry<TOSCAService, Application> binding : bindApplications.entrySet()) {
			Application application = binding.getValue();
			List<String> fileLocations = application.getFileLocations();
			fileLocations.removeIf(url -> {
				URI uri = URI.create(url);
				String path = uri.getPath();
				String[] pathSegments = path.split("/");
				return !(pathSegments.length >= 9 && "csars".equals(pathSegments[1])
						&& binding.getKey().id.equals(pathSegments[2]) && "content".equals(pathSegments[3])
						&& "artifacttemplates".equals(pathSegments[4])
						&& "http%3A%2F%2Flegion2.github.io%2Ftosca%2Fartifacttemplates".equals(pathSegments[5]));
			});
			List<URL> files = new ArrayList<>(fileLocations.size());
			for (String baseURI : fileLocations) {

				Response response = ClientBuilder.newClient().target(baseURI)
						.request(MediaType.APPLICATION_OCTET_STREAM_TYPE).get();

				try (InputStream in = (InputStream) response.getEntity()) {
					Path path = Files.createTempFile(this.adapterBasePath, application.getId(), ".jar");
					Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
					files.add(path.toUri().toURL());
				} catch (IOException e) {
					throw new IllegalStateException(e);
				}

			}

			URL[] urls = new URL[files.size()];
			files.toArray(urls);
			classLoaders.add(new URLClassLoader(urls, this.getClass().getClassLoader()));
		}
		return classLoaders;
	}

	private Map<TOSCAService, Map<String, String>> getBondaryDefintionProperties(
			Map<TOSCAService, ApplicationInstance> provisionApplications) {
		Map<TOSCAService, Map<String, String>> map = new HashMap<>();

		for (Entry<TOSCAService, ApplicationInstance> binding : provisionApplications.entrySet()) {
			ApplicationInstance applicationInstance = binding.getValue();
			Map<String, String> properties = applicationInstance.getProperties();

			map.put(binding.getKey(), properties);
		}
		return map;
	}
}
