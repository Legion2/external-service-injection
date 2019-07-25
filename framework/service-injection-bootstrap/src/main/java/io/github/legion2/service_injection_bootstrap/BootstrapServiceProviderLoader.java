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

package io.github.legion2.service_injection_bootstrap;

import java.util.Set;

import com.google.auto.service.AutoService;

import io.github.amyassist.amy.core.di.Configuration;
import io.github.amyassist.amy.core.di.ServiceProviderLoader;
import io.github.amyassist.amy.core.di.annotation.Service;
import io.github.amyassist.amy.core.di.provider.ClassServiceProvider;
import io.github.amyassist.amy.deployment.DeploymentDescriptorUtil;

/**
 * This is the bootstrap ServiceProviderLoader. It loads the all Services from the DDs. The loaded services will then be
 * registered in the Service injection with the {@link ClassServiceProvider}. This ServiceProviderLoader is by default
 * activated if not excluded.
 * 
 * @author Leon Kiefer
 */
@AutoService(ServiceProviderLoader.class)
public class BootstrapServiceProviderLoader implements ServiceProviderLoader {

	private static final String SERVICE_DEPLOYMENT_DESCRIPTOR = "META-INF/" + Service.class.getName();

	@Override
	public Set<ClassLoader> load(Configuration configuration, ClassLoader classLoader) {
		Set<Class<?>> services = DeploymentDescriptorUtil.getClasses(classLoader, SERVICE_DEPLOYMENT_DESCRIPTOR);
		services.forEach(configuration::register);
		return null;
	}
}
