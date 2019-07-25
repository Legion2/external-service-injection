# Use Java ServiceLoader as entrypoint

* Deciders: Leon Kiefer
* Date: 2019-02-10

## Context and Problem Statement

Service Implementations/Service Providers must be registered in the framework before they can be used.
The registration is the first interaction between the Service Implementations/Service Providers and the framework, therefore called the entrypoint.

## Considered Options

* [ServiceLoader](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html) - entrypoint `load` method
* No default entrypoint - each implementation has it's own entrypoint
* XML-based ServiceProvider descriptions
* ServiceLoader style configuration file for ServiceImplementations

## Decision Outcome

Chosen option: "ServiceLoader", because its the most general approach and is part of the default Java API.

### Positive Consequences

* Well documented [API](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html)
* [tool](https://maven.apache.org/plugins/maven-shade-plugin/examples/resource-transformers.html#ServicesResourceTransformer) support in maven
* New ServiceProvider can be added without changing any existing code/file

### Negative Consequences

* ClassLoader management required
* in the jar build process the service configurtaion file must be handled corectly

## Pros and Cons of the Options

### ServiceLoader

Using [ServiceLoader](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html), the entrypoint for ServiceProviders is a method that gets called.
In this entrypoint method any arbitrary logic can be executed to register ServiceProviders.
The entrypoint must be placed in a well-known provider-configuration file in the resource directory META-INF/services.

* Good, because arbitrary logic can be executed
* Good, because it has no limitations
* Good, because Java API is used
* Bad, because it't an imperative approach

### No default entrypoint

Each implementation has it's approach for an entrypoint.

* Bad, because can't use ServiceProvider with other implementation

### XML-based ServiceProvider descriptions

The ServiceProviders are registered by placing a description in a XML file.

* Good, because declarative approach
* Good, because it's simple to use
* Bad, because a lot of implementation effort
* Bad, because limited possibilities of instantiation of ServiceProviders
* Bad, because no dynamic loading possible

### ServiceLoader style configuration file for ServiceImplementations

The ServiceImplementations are registered by placing the fully-qualified binary name in a configuration file.

* Good, because very simple to add new ServiceImplementation
* Bad, because ServiceProviders can't be instanceated
* Bad, because loss of abstraction layer "ServiceProvider"
* Bad, because only ServiceImplementations using the ClassServiceProvider can be used
* Bad, because no dynamic loading possible
