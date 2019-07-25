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

package io.github.legion2.tosca_mqtt_service_adapter;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.slf4j.Logger;

import io.github.amyassist.amy.core.di.annotation.Context;
import io.github.amyassist.amy.core.di.annotation.PostConstruct;
import io.github.amyassist.amy.core.di.annotation.PreDestroy;
import io.github.amyassist.amy.core.di.annotation.Reference;
import io.github.amyassist.amy.core.di.annotation.Service;
import io.github.legion2.messaging_adapter.MessageConsumer;
import io.github.legion2.messaging_adapter.MessagingAdapter;

/**
 * Implementation of the MessagingAdapter using eclipse paho. Based on
 * https://github.com/AmyAssist/Amy/blob/dev/amy-message-hub/src/main/java/io/github/amyassist/amy/messagehub/MQTTAdapter.java
 * 
 * @author Leon Kiefer
 */
@Service(MessagingAdapter.class)
public class MQTTMessagingAdapterImpl implements MessagingAdapter, IMqttActionListener, MqttCallback {

	private static final int DISCONNECTED_BUFFER_SIZE = 10000;

	// all durations are given in seconds
	private static final int CONNECTION_TIMEOUT = 10;
	private static final int KEEP_ALIVE_INTERVAL = 5;
	private static final int DISCONNECT_TIMEOUT = 2;

	private MqttAsyncClient client;
	private MqttConnectOptions options;
	private IMqttToken connectToken;

	@Context("brokerAddress")
	private String brokerAddress;

	private MessageConsumer consumer;

	@Reference
	private Logger logger;

	@PostConstruct
	private void init() {

		Path persistencePath = Paths.get(".mqtt-persistence");

		try {
			this.client = new MqttAsyncClient(this.brokerAddress, UUID.randomUUID().toString(),
					new MqttDefaultFilePersistence(persistencePath.toAbsolutePath().toString()));
			this.client.setCallback(this);
		} catch (MqttException e) {
			throw new IllegalStateException("Failed to initialize mqtt client", e);
		}

		DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
		disconnectedBufferOptions.setBufferEnabled(true);
		disconnectedBufferOptions.setPersistBuffer(true);
		disconnectedBufferOptions.setBufferSize(DISCONNECTED_BUFFER_SIZE);
		disconnectedBufferOptions.setDeleteOldestMessages(false);
		this.client.setBufferOpts(disconnectedBufferOptions);

		this.options = new MqttConnectOptions();
		this.options.setCleanSession(false);
		this.options.setConnectionTimeout(CONNECTION_TIMEOUT);
		this.options.setKeepAliveInterval(KEEP_ALIVE_INTERVAL);
		this.options.setAutomaticReconnect(true);

		this.connect();
		try {
			this.connectToken.waitForCompletion();
		} catch (MqttException e) {
			throw new IllegalStateException("Error while waiting for connection", e);
		}
	}

	private void connect() {
		try {
			this.connectToken = this.client.connect(this.options, null, this);
		} catch (MqttException e) {
			throw new IllegalStateException("Error while connecting", e);
		}
	}

	@PreDestroy
	private void disconnect() {
		try {
			this.client.disconnect(DISCONNECT_TIMEOUT * 1000L);
		} catch (MqttException | IllegalStateException e) {
			this.logger.error("Exception while disconnect", e);
		}
	}

	public void publish(String topic, String payload, int qualityOfService, boolean retain) {
		MqttMessage msg = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
		msg.setQos(qualityOfService);
		msg.setRetained(retain);
		try {
			this.client.publish(topic, msg, "publish", this);
		} catch (MqttException e) {
			throw new IllegalStateException("Error while publishing.", e);
		}
	}

	public void subscribe(String topic, MessageConsumer consumer) {
		try {
			this.client.subscribe(topic, 2, "Subscribe", this);
			this.consumer = consumer;
		} catch (MqttException e) {
			throw new IllegalStateException("Error while subscribing", e);
		}
	}

	public void unsubscribe(String topic, MessageConsumer consumer) {
		try {
			this.client.unsubscribe(topic, "Unsubscribe", this);
			this.consumer = null;
		} catch (MqttException e) {
			throw new IllegalStateException("Error while unsubscribing", e);
		}
	}

	public void onSuccess(IMqttToken asyncActionToken) {
		if (asyncActionToken.isComplete() && asyncActionToken.equals(this.connectToken)) {
			this.connectToken = null;
		}
	}

	public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
		if (asyncActionToken.equals(this.connectToken)) {
			this.connectToken = null;
			this.logger.error("Could not connect to Broker");
		}
		this.logger.error("Async action failed", exception);
	}

	@Override
	public void connectionLost(Throwable cause) {
		this.logger.warn("Connection Lost", cause);
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		if (this.consumer != null) {
			this.consumer.consumer(new String(message.getPayload(), StandardCharsets.UTF_8), topic);
		}
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// nothing to do here
		
	}
}
