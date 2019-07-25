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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.github.amyassist.amy.core.di.DependencyInjection;
import io.github.amyassist.amy.core.di.ServiceLocator;
import io.github.legion2.calculator.Calculator;

/**
 * Calculator Servlet using Service Injection Framework to get External Service
 * reference.
 * 
 * @author Leon Kiefer
 */
public class CalculatorServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4571924064701447199L;

	private ServiceLocator serviceLocator;

	private DependencyInjection dependencyInjection;

	@Override
	public void init() throws ServletException {
		this.dependencyInjection = new DependencyInjection();
		this.dependencyInjection.loadServices();
		this.serviceLocator = this.dependencyInjection.getServiceLocator();
		System.out.println("Servlet " + this.getServletName() + " has started");
	}

	@Override
	protected void doGet(HttpServletRequest reqest, HttpServletResponse response) throws ServletException, IOException {
		Calculator calculator = this.serviceLocator.getService(Calculator.class);

		String parametera = reqest.getParameter("a");
		if (parametera == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		float a = Float.parseFloat(parametera);

		String parameterb = reqest.getParameter("b");
		if (parameterb == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		float b = Float.parseFloat(parameterb);

		String path = reqest.getPathInfo();
		float result;
		switch (path) {
		case "/add":
			result = calculator.add(a, b);
			break;
		case "/sub":
			result = calculator.sub(a, b);
			break;
		case "/mul":
			result = calculator.mul(a, b);
			break;
		case "/div":
			result = calculator.div(a, b);
			break;
		default:
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		response.getWriter().printf("Result of calculatio is '%s'", result);
	}

	@Override
	public void destroy() {
		System.out.println("Servlet " + this.getServletName() + " has stopped");
	}

}
