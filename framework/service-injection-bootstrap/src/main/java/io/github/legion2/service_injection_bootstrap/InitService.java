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

/**
 * The main entrypoint when launching a application using the service injection framework. This Service is responsible
 * for the Init components.
 * 
 * @author Leon Kiefer
 */
public interface InitService {

	/**
	 * Init the application with the given arguments. This will create all Init components and executes them.
	 * This method return after all components are initiated.
	 * 
	 * @param args
	 *            the arguments pass to the application
	 */
	void init(String[] args);

}
