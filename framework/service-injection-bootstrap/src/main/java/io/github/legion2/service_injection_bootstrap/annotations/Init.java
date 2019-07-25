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

package io.github.legion2.service_injection_bootstrap.annotations;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.github.amyassist.amy.core.di.annotation.RequireDeploymentDescriptor;
import io.github.amyassist.amy.deployment.DeploymentDescriptorUtil;
import io.github.legion2.service_injection_bootstrap.InitComponent;

/**
 * Declare a Init component. This annotation is used on classes to declare them as a Init component. Init Components
 * must implement the {@link InitComponent} interface. Additional the class MUST be listed in the deployment descriptor
 * <code>META-INF/io.github.legion2.service_injection_bootstrap.annotations.Init</code>. For more information about the
 * deployment descriptor format see {@link DeploymentDescriptorUtil}.
 * 
 * @author Leon Kiefer
 */
@RequireDeploymentDescriptor
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface Init {

}
