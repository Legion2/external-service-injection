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

package io.github.amyassist.amy.core.di.provider;

import java.util.Set;

import javax.annotation.Nonnull;

import io.github.amyassist.amy.core.di.*;
import io.github.amyassist.amy.core.di.consumer.ServiceConsumer;
import io.github.amyassist.amy.core.di.runtime.ServiceDescriptionImpl;
import io.github.amyassist.amy.core.di.runtime.ServiceInstantiationDescriptionImpl;

/**
 * Provider for the ServiceLocator in the DI with the correct consumer class.
 * 
 * @author Leon Kiefer
 */
public class ClassLoaderManagerProvider implements ServiceProvider<ClassLoaderManager> {

	private Set<ClassLoader> classLoaders;

	/**
	 * @param classLoaders
	 */
	public ClassLoaderManagerProvider(Set<ClassLoader> classLoaders) {
		this.classLoaders = classLoaders;
	}

	@Override
	@Nonnull
	public ServiceDescription<ClassLoaderManager> getServiceDescription() {
		return new ServiceDescriptionImpl<>(ClassLoaderManager.class);
	}

	@Override
	public ServiceInstantiationDescription<ClassLoaderManager> getServiceInstantiationDescription(
			@Nonnull ContextLocator locator, @Nonnull ServiceConsumer<ClassLoaderManager> serviceConsumer) {
		return new ServiceInstantiationDescriptionImpl<>(this.getServiceDescription(), ClassLoaderManagerImpl.class);
	}

	@Override
	@Nonnull
	public ClassLoaderManager createService(@Nonnull SimpleServiceLocator locator,
			@Nonnull ServiceInstantiationDescription<ClassLoaderManager> serviceInstantiationDescription) {
		return new ClassLoaderManagerImpl(this.classLoaders);
	}

	@Override
	public void dispose(@Nonnull ClassLoaderManager service,
			@Nonnull ServiceInstantiationDescription<ClassLoaderManager> serviceInstantiationDescription) {
		// nothing to do here
	}

}
