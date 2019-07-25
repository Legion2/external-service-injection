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

package io.github.legion2.servlet_demo;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import io.github.amyassist.amy.core.di.DependencyInjection;
import io.github.amyassist.amy.core.di.ServiceDescription;
import io.github.amyassist.amy.core.di.ServiceLocator;
import io.github.amyassist.amy.core.di.consumer.ServiceConsumer;
import io.github.amyassist.amy.core.di.provider.ServiceHandle;

@ApplicationScoped
public class DependencyInjectionWrapper implements ServiceLocator {

	private ServiceLocator serviceLocator;
	
	private DependencyInjection di;
	
	@PostConstruct
    private void init() {
		this.di = new DependencyInjection();
		this.di.loadServices();
		this.serviceLocator = this.di.getServiceLocator();
    }

	@Override
	public <T> T createAndInitialize(Class<T> serviceClass) {
		return this.serviceLocator.createAndInitialize(serviceClass);
	}

	@Override
	public <T> T getService(Class<T> serviceType) {
		return this.serviceLocator.getService(serviceType);
	}

	@Override
	public <T> ServiceHandle<T> getService(ServiceDescription<T> serviceDescription) {
		return this.serviceLocator.getService(serviceDescription);
	}

	@Override
	public <T> ServiceHandle<T> getService(ServiceConsumer<T> serviceConsumer) {
		return this.serviceLocator.getService(serviceConsumer);
	}

	@Override
	public void inject(Object injectMe) {
		this.serviceLocator.inject(injectMe);
	}

	@Override
	public void postConstruct(Object postConstructMe) {
		this.serviceLocator.postConstruct(postConstructMe);
	}

	@Override
	public void preDestroy(Object destroyMe) {
		this.serviceLocator.preDestroy(destroyMe);
	}

	@Override
	public void shutdown() {
		this.serviceLocator.shutdown();
	}

}
