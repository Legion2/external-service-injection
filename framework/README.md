# Service Injection Framework

The `service-injection-api` contains the set of interfaces and annotations to getting started with programming.
The `service-injection` contains the implementation of the interfaces and minimal tools to run an application with the Service Injection Framework.

To get more tool support and an easy start with the framework use the `service-injection-basic` dependency.
It contains a ServiceProviderLoader that loads ServiceClasses form Deployment descriptors.
Also a Main class with a `main` method is provided, which can optionally used to bootstrap the application.

The `tosca-service-manager` contains the External Service Manager implementation for the TOSCA Runtime which can load and deploy OpenTOSCA services.

Architectural Decision Records for the framework are located in [docs/adr/](docs/adr/).