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
package io.github.legion2.demo_calculator;

import io.github.amyassist.amy.core.di.annotation.Reference;
import io.github.legion2.calculator.Calculator;
import io.github.legion2.service_injection_bootstrap.InitComponent;
import io.github.legion2.service_injection_bootstrap.annotations.Init;

@Init
public class DemoCalculator implements InitComponent {

	@Reference
	private Calculator calculator;

	@Override
	public void init() {
		System.out.printf("17 + 42 = %s%n", this.calculator.add(17, 42));
		System.out.printf("17 - 42 = %s%n", this.calculator.sub(17, 42));
		System.out.printf("17 * 42 = %s%n", this.calculator.mul(17, 42));
		System.out.printf("17 / 42 = %s%n", this.calculator.div(17, 42));
	}

}
