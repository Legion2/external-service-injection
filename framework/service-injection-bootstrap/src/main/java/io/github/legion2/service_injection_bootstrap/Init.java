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

import java.util.HashSet;
import java.util.Set;

import io.github.amyassist.amy.core.di.ClassLoaderManager;
import io.github.amyassist.amy.core.di.ServiceLocator;
import io.github.amyassist.amy.core.di.annotation.Reference;
import io.github.amyassist.amy.core.di.annotation.Service;
import io.github.amyassist.amy.deployment.DeploymentDescriptorUtil;

/**
 * Implementation of the {@link InitService}.
 * 
 * @author Leon Kiefer
 */
@Service
public class Init implements InitService {
	private static final String COMPONENT_DEPLOYMENT_DESCRIPTOR = "META-INF/"
			+ io.github.legion2.service_injection_bootstrap.annotations.Init.class.getName();

	private Set<InitComponent> initializedComponents = new HashSet<>();

	@Reference
	private ServiceLocator serviceLocator;

	@Reference
	private ClassLoaderManager classLoaderManager;

	@Override
	public void init(String[] args) {
		for (ClassLoader classLoader : this.classLoaderManager.getClassLoaders()) {
			Set<Class<?>> classes = DeploymentDescriptorUtil.getClasses(classLoader, COMPONENT_DEPLOYMENT_DESCRIPTOR);
			Set<InitComponent> initialized = new HashSet<>();
			for (Class<?> clazz : classes) {
				initialized.add(this.init(clazz));
			}

			this.initializedComponents.addAll(initialized);
		}
	}

	private InitComponent init(Class<?> initComponentClass) {
		if (!initComponentClass
				.isAnnotationPresent(io.github.legion2.service_injection_bootstrap.annotations.Init.class)) {
			throw new IllegalArgumentException("Missing @Init annotation on class " + initComponentClass.getName());
		}

		if (!InitComponent.class.isAssignableFrom(initComponentClass)) {
			throw new IllegalArgumentException(
					"Init components must implement the InitComponent interface. But the class "
							+ initComponentClass.getName() + " does not.");
		}
		InitComponent initComponent = (InitComponent) this.serviceLocator
				.createAndInitialize(initComponentClass);
		try {
			initComponent.init();
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Can not init " + initComponentClass.getName() + ", because it threw an exception.", e);
		}

		return initComponent;
	}

}
