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

package io.github.legion2.tosca_amqp_service_adapter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import io.github.amyassist.amy.core.di.annotation.Context;
import io.github.amyassist.amy.core.di.annotation.PostConstruct;
import io.github.amyassist.amy.core.di.annotation.PreDestroy;
import io.github.amyassist.amy.core.di.annotation.Service;
import io.github.legion2.messaging_adapter.MessageConsumer;
import io.github.legion2.messaging_adapter.MessagingAdapter;

@Service
public class AMQPMessagingAdapterImpl implements MessagingAdapter {

	@Context("host")
	private String host;

	@Context("port")
	private String portNumber;

	@Context("exchangeName")
	private String exchangeName;

	private Channel channel;

	private String consumerTag;

	private String queue;

	@PostConstruct
	private void setup() {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(host);
		factory.setPort(Integer.parseInt(portNumber));

		try {
			Connection conn = factory.newConnection();
			this.channel = conn.createChannel();
			this.channel.exchangeDeclare(exchangeName, "topic");
			queue = channel.queueDeclare().getQueue();
		} catch (IOException | TimeoutException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void publish(String topic, String payload, int qualityOfService, boolean retain) {
		try {
			topic = topic.replace('/', '.');
			channel.basicPublish(exchangeName, topic, null, payload.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void subscribe(String topic, MessageConsumer consumer) {
		try {
			channel.queueBind(queue, this.exchangeName, topic.replace('/', '.'));
			DeliverCallback deliverCallback = (c, delivery) -> {
				String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
				String routingKey = delivery.getEnvelope().getRoutingKey();
				consumer.consumer(message, routingKey.replace('.', '/'));
			};
			consumerTag = channel.basicConsume(queue, true, deliverCallback, c -> {
			});
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void unsubscribe(String topic, MessageConsumer consumer) {
		try {
			channel.basicCancel(consumerTag);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
	
	@PreDestroy
	private void end() {
		try {
			this.channel.close();
		} catch (IOException | TimeoutException e) {
			e.printStackTrace();
		}
	}

}
