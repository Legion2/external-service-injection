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

import io.github.amyassist.amy.core.di.annotation.Reference;
import io.github.legion2.messaging_adapter.MessagingAdapter;
import io.github.legion2.service_injection_bootstrap.InitComponent;
import io.github.legion2.service_injection_bootstrap.annotations.Init;

/**
 * Example to show how to use the InitComponent and how to inject services.
 * 
 * @author Leon Kiefer
 */
@Init
public class TemperatureConsumer implements InitComponent {

	private static final String TOPIC = "temperature/#";

	@Reference
	private MessagingAdapter messagingAdapter;

	private void callback(String message, String topic) {
		System.out.printf("The current temperature at %s is %s.%n",
				topic.substring(Math.min(TOPIC.length() - 1, topic.length())), message);
	}

	@Override
	public void init() {
		this.messagingAdapter.subscribe(TOPIC, this::callback);
	}
}
