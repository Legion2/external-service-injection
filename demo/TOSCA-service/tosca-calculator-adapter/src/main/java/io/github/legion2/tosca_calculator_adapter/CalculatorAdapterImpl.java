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

package io.github.legion2.tosca_calculator_adapter;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import io.github.amyassist.amy.core.di.annotation.Context;
import io.github.amyassist.amy.core.di.annotation.PostConstruct;
import io.github.amyassist.amy.core.di.annotation.Service;
import io.github.legion2.calculator.Calculator;

/**
 * 
 * @author Leon Kiefer
 */
@Service
public class CalculatorAdapterImpl implements Calculator {

	private WebTarget calculatorTarget;

	@Context("calculator.uri")
	private String uri;

	@PostConstruct
	private void setup() {
		Client client = ClientBuilder.newClient();
		this.calculatorTarget = client.target(uri).path("api/calculate");
	}

	@Override
	public float add(float a, float b) {
		ResponseModel response = calculatorTarget.queryParam("expression", a + "+" + b)
				.request(MediaType.APPLICATION_JSON).get(ResponseModel.class);
		return Float.parseFloat(response.result);
	}

	@Override
	public float sub(float a, float b) {
		ResponseModel response = calculatorTarget.queryParam("expression", a + "-" + b)
				.request(MediaType.APPLICATION_JSON).get(ResponseModel.class);
		return Float.parseFloat(response.result);
	}

	@Override
	public float mul(float a, float b) {
		ResponseModel response = calculatorTarget.queryParam("expression", a + "*" + b)
				.request(MediaType.APPLICATION_JSON).get(ResponseModel.class);
		return Float.parseFloat(response.result);
	}

	@Override
	public float div(float a, float b) {
		ResponseModel response = calculatorTarget.queryParam("expression", a + "/" + b)
				.request(MediaType.APPLICATION_JSON).get(ResponseModel.class);
		return Float.parseFloat(response.result);
	}

	static class ResponseModel {
		public String result;
		public String instance;
	}

}
