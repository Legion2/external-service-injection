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

package io.github.legion2.demo_message_consumer;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.github.amyassist.amy.core.di.annotation.Reference;
import io.github.legion2.messaging_adapter.MessagingAdapter;
import io.github.legion2.service_injection_bootstrap.InitComponent;
import io.github.legion2.service_injection_bootstrap.annotations.Init;

/**
 * Provider for fake temperature data published to a topic of the
 * MessagingAdapter.
 * 
 * @author Leon Kiefer
 */
@Init
public class TemperatureProvider implements InitComponent {
	private static final String TOPIC = "temperature";

	private double temp = 15;

	private Random random = new Random();

	@Reference
	private MessagingAdapter messagingAdapter;

	@Override
	public void init() {
		ScheduledExecutorService newScheduledThreadPool = Executors.newScheduledThreadPool(1);
		newScheduledThreadPool.scheduleAtFixedRate(this::publishData, 500, 700, TimeUnit.MILLISECONDS);
	}

	private void publishData() {
		double nextGaussian = this.random.nextGaussian();
		temp += nextGaussian * 0.1;
		messagingAdapter.publish(TOPIC + "/outdoor", String.format("%1$,.2f °C", temp), 2, false);
	}

}
