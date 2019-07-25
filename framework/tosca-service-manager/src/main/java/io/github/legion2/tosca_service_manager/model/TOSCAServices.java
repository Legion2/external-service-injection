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

package io.github.legion2.tosca_service_manager.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Root model class for the xml DD of the TOSCA Service Manager.
 * 
 * @author Leon Kiefer
 */
@XmlRootElement(name = "TOSCAServices")
public class TOSCAServices {
	@XmlElement(name = "TOSCAService")
	public List<TOSCAService> TOSCAServices;

}
