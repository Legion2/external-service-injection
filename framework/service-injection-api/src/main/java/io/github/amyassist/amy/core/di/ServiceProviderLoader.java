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

import java.util.Set;

/**
 * A Interface for use with {@link java.util.ServiceLoader} to register Service classes, Service provider and Context
 * Providers in the DI via the Configuration.
 * 
 * Implementations of this interface MUST have a default public constructor.
 * 
 * @author Leon Kiefer
 */
public interface ServiceProviderLoader {
	/**
	 * Called form the DI when loading services
	 * 
	 * @param configuration
	 *            the Configuration used to register Service classes, Service provider and context provider
	 * @param classLoader
	 *            the classLoader, which should be used to load stuff from. This Method is called for each ClassLoader.
	 *            Note: ClassLoader have a parent and you should only load classes for the given ClassLoader, so check
	 *            if a Class was loaded with this ClassLoader.
	 * @return new ClassLoaders which was created while loading and must be considered for loading additional resources
	 */
	Set<ClassLoader> load(Configuration configuration, ClassLoader classLoader);
}
