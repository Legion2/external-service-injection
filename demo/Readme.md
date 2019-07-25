# Demo
There are three demo applications:
- Application using messaging service
- Application using HTTP service
- Servlet using HTTP service

## Application using messaging service
The demo consists of three components.
The TOSCA service which is injected and two Java Applications which uses the TOSCA Service.
There are two Massage Broker available: An Eclipse Mosquite MQTT Broker and a RabbitMQ Broker.
One Java Application publishes to a topic and the other subscribes to this topic.
The Java Applications can be deployed independently.

## messaging-adapter-api
The api contained in the `messaging-adapter/` directory is the api specification of the messaging adapter used by the Java Applications to interact with messaging adapter.
This api is vendor independent and does not refer to a specific implementation.
It demonstrate the Loose Coupling of service consumers and service providers.

## Application using HTTP service
In this demo application a java application have to do some calculations.
The calculation logic is implemented by a external HTTP Service the `Calculator-Backend`.
The local application have to do a HTTP call to the api of the HTTP Service with the correct arguments.
The domain specific interface `Calculator` is defined in `calculator-api/`.
It is used by the local application, so no http stuff must be handled in by the application.
The `Calculator-Backend` provides a adapter that implements the `Calculator` interface and makes the correct http request with the given arguments to the `Calculator-Backend`.

## Servlet using HTTP service
This demo uses demonstrate how to use the Service Injection Framework in the JavaEE applications.
The HTTPServlet use the programmatic api of the framework to load and access a external HTTP Service.
The same adapter as in the previous demo are used.

# Getting Started

Run the `install.sh` script in the project root folder, this will build the framework, all adapters and the demo applications.
Use the `tosca-definitions.zip` of the GitHub release and upload it as repository to winery, also upload the [tosca-definitions-public](https://github.com/OpenTOSCA/tosca-definitions-public) to winery.
Instead of using the artifact from GitHub release, you can build it yourself.
The `TOSCA-service/` directory contains the TOSCA Service templates and the adapter implementations.
The TOSCA artifacts are structured as winery repository, so it can be easily imported in winery.

Run the build jar files in the `target` directory of the demo projects or open eclipse.
To run the demo with eclipse, add a run configuration with the main class `io.github.legion2.service_injection_bootstrap.BootstrapMain`.
The servlet demo require docker and can be started with `docker-compose up` in the `demo-servlet/` directory.
