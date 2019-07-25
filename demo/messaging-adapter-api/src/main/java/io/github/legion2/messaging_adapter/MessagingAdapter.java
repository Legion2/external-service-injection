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

package io.github.legion2.messaging_adapter;

/**
 * Simple MessagingAdapter for PubSub messaging. It provide subscribe,
 * unsubscribe and publish functionality.
 * 
 * @author Leon Kiefer
 */
public interface MessagingAdapter {
	/**
	 * Publishes a message on the given topic.
	 * 
	 * @param topic            The topic to publish to
	 * @param message          The message to publish
	 * @param qualityOfService The quality of service level to publish with (0-2)
	 * @param retain           Whether the message should be retained.
	 * 
	 * @throws IllegalStateException When an error occurs while publishing.
	 */
	void publish(String topic, String message, int qualityOfService, boolean retain);

	/**
	 * Subscribe to a topic
	 * 
	 * @param topic    The topic to subscribe to.
	 * @param consumer The consumer which will consume messages published to the
	 *                 given topic
	 * @throws IllegalStateException When an error occurs while subscribing.
	 */
	void subscribe(String topic, MessageConsumer consumer);

	/**
	 * Unsubscribe from a topic
	 * 
	 * @param topic    The topic to unsubscribe from
	 * @param consumer The consumer which consume messages published to the given
	 *                 topic
	 * @throws IllegalStateException    When an error occurs while unsubscribing.
	 * @throws IllegalArgumentException When the given consumer was not subscribed.
	 */
	void unsubscribe(String topic, MessageConsumer consumer);
}
