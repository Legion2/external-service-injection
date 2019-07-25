/*
 * This source file is part of the Amy open source project.
 * For more information see github.com/AmyAssist
 * 
 * Copyright (c) 2018 the Amy project authors.
 *
 * SPDX-License-Identifier: Apache-2.0
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For more information see notice.md
 */

package io.github.amyassist.amy.deployment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for reading deployment descriptors and get the classes. A deployment descriptor is a text file
 * containing full class names of the classes that should be deployed. Each line can only contain one class name. Lines
 * stating with <code>#</code> or empty lines are ignored. The full class name includes the package name separated by
 * dots. e.g. <code>io.github.amyassist.amy.deployment.DeploymentDescriptorUtil</code>
 * 
 * @author Leon Kiefer
 */
public class DeploymentDescriptorUtil {

	private DeploymentDescriptorUtil() {
		// hide constructor
	}

	/**
	 * Get all classes from the deployment descriptor using the given {@link ClassLoader}.
	 * 
	 * 
	 * @param classLoader
	 *            the ClassLoader to use
	 * @param deploymentDescriptor
	 *            the path of the deployment descriptor from which to read the class names
	 * @param excludeParentClassLoaders
	 *            this controls if only Classes for the given ClassLoader should be returned. This can be used if the
	 *            method is called multiple times with different ClassLoaders that have a commen parent ClassLoader.
	 *            Setting it to <code>true</code> will prevent classes from processed multiple times.
	 * @return all classes specified in the deployment descriptor from the ClassLoader. depending on
	 *         {@code excludeParentClassLoaders} only classes from the given ClassLoader or including classes form it's
	 *         parents.
	 */
	public static Set<Class<?>> getClasses(ClassLoader classLoader, String deploymentDescriptor,
			boolean excludeParentClassLoaders) {
		Set<Class<?>> classes = getAllClasses(classLoader, deploymentDescriptor);
		if (excludeParentClassLoaders) {
			classes.removeIf(clazz -> !clazz.getClassLoader().equals(classLoader));
		}
		return classes;
	}

	/**
	 * This method only returns classes form the given ClassLoader not its parents.
	 * 
	 * @see #getClasses(ClassLoader, String, boolean)
	 * @param classLoader
	 *            the ClassLoader to use
	 * @param deploymentDescriptor
	 *            the path of the deployment descriptor from which to read the class names
	 * @return all classes specified in the deployment descriptor from the ClassLoader
	 */
	public static Set<Class<?>> getClasses(ClassLoader classLoader, String deploymentDescriptor) {
		return getClasses(classLoader, deploymentDescriptor, true);
	}

	private static Set<Class<?>> getAllClasses(ClassLoader classLoader, String deploymentDescriptor) {
		Enumeration<URL> resources;
		try {
			resources = classLoader.getResources(deploymentDescriptor);
		} catch (IOException e) {
			throw new IllegalStateException("Could not read the deployment descriptor", e);
		}
		Set<Class<?>> classes = new HashSet<>();
		while (resources.hasMoreElements()) {
			try (InputStream resourceAsStream = resources.nextElement().openStream()) {
				Set<String> entries = readDeploymentDescriptorFile(resourceAsStream);
				for (String entry : entries) {
					classes.add(getClass(entry, classLoader));
				}
			} catch (IOException e) {
				throw new IllegalStateException("Could not read the deployment descriptor file", e);
			}
		}
		return classes;
	}
	/**
	 * Reads the set of entries from a deployment descriptor which is formatted as a service file.
	 *
	 * @param input
	 *            not {@code null}. Closed after use.
	 * @return a not {@code null Set} of entry class names.
	 */
	public static Set<String> readDeploymentDescriptorFile(InputStream input) {
		Set<String> entries = new HashSet<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				int commentStart = line.indexOf('#');
				if (commentStart >= 0) {
					line = line.substring(0, commentStart);
				}
				line = line.trim();
				if (!line.isEmpty()) {
					entries.add(line);
				}
			}
		} catch (IOException e) {
			throw new IllegalStateException("Could not read the deployment descriptor", e);
		}
		return entries;
	}

	/**
	 * @param className
	 *            the name of the class
	 * @param classLoader
	 *            the classLoader to load the class from
	 * @return the class with the given name from the classLoader
	 * @throws IllegalArgumentException
	 *             if the class could not be loaded
	 */
	private static Class<?> getClass(String className, ClassLoader classLoader) {
		try {
			return Class.forName(className, true, classLoader);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(
					"Could not load class " + className + " with the given ClassLoader " + classLoader, e);
		}
	}
}
