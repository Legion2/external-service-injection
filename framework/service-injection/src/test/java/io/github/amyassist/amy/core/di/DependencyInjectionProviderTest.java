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

package io.github.amyassist.amy.core.di;

import static org.junit.jupiter.api.Assertions.*;

import javax.annotation.Nonnull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.github.amyassist.amy.core.di.consumer.ServiceConsumer;
import io.github.amyassist.amy.core.di.provider.ServiceProvider;
import io.github.amyassist.amy.core.di.runtime.ServiceDescriptionImpl;
import io.github.amyassist.amy.core.di.runtime.ServiceInstantiationDescriptionImpl;

/**
 * Test for DependencyInjection
 * 
 * @author Leon Kiefer
 */
class DependencyInjectionProviderTest {

	private DependencyInjection dependencyInjection;

	@BeforeEach
	public void setup() {
		this.dependencyInjection = new DependencyInjection();
	}

	@Test
	void testServiceAnnotation() {
		this.dependencyInjection.getConfiguration().register(new ServiceProvider<ServiceWithServiceLocator>() {

			@Override
			@Nonnull
			public ServiceDescription<ServiceWithServiceLocator> getServiceDescription() {
				return new ServiceDescriptionImpl<>(ServiceWithServiceLocator.class);
			}

			@Override
			public ServiceInstantiationDescription<ServiceWithServiceLocator> getServiceInstantiationDescription(
					@Nonnull ContextLocator locator,
					@Nonnull ServiceConsumer<ServiceWithServiceLocator> serviceConsumer) {
				return new ServiceInstantiationDescriptionImpl<>(this.getServiceDescription(),
						ServiceWithServiceLocator.class);
			}

			@Override
			@Nonnull
			public ServiceWithServiceLocator createService(@Nonnull SimpleServiceLocator locator,
					@Nonnull ServiceInstantiationDescription<ServiceWithServiceLocator> serviceInstantiationDescription) {
				return new ServiceWithServiceLocator(locator);
			}

			@Override
			public void dispose(@Nonnull ServiceWithServiceLocator service,
					@Nonnull ServiceInstantiationDescription<ServiceWithServiceLocator> serviceInstantiationDescription) {
			}
		});
		ServiceWithServiceLocator service = this.dependencyInjection.getServiceLocator()
				.getService(ServiceWithServiceLocator.class);

		ServiceConsumer<Service1> serviceConsumer = Mockito.mock(ServiceConsumer.class);
		assertThrows(RuntimeException.class, () -> service.getLocator().getService(serviceConsumer));
	}

}
