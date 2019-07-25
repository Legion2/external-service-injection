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

package io.github.legion2.service_injection_logger;

import java.util.Collections;
import java.util.Set;

import com.google.auto.service.AutoService;

import io.github.amyassist.amy.core.di.Configuration;
import io.github.amyassist.amy.core.di.ServiceProviderLoader;

/**
 * Register the Services for the Logger
 * 
 * @author Leon Kiefer
 */
@AutoService(ServiceProviderLoader.class)
public class LoggerServiceProviderLoader implements ServiceProviderLoader {

	@Override
	public Set<ClassLoader> load(Configuration configuration, ClassLoader classLoader) {
		if (classLoader.equals(this.getClass().getClassLoader())) {
			configuration.register(new LoggerProvider());
		}
		return Collections.emptySet();
	}
}
