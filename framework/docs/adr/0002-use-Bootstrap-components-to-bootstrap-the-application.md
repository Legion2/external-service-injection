# Use Bootstrap components to bootstrap the application

* Deciders: Leon Kiefer
* Date: 2019-04-08

## Context and Problem Statement

Normally services are only instantiated/started if they are needed by another service or component.
But at the beging there is nothing, not a single service or component is instantiated.
There MUST be an external trigger that instantiate the first Service.

Also there are Services, that don't provide a Java interface but a external interface like HTTP, TCP or is a producer of events/requests/messages for other services.
This Services don't provide services in the scope of the framework and therefore can't be required as dependency by other services in the framework.

Combinations of Services with global side effect and a Java interface exists.
For example a WebServer with a Java interface to control the server with other services.

All the services described in this problem are **Services with global side effects**.
We can not instantiate these services on demand, they must be always running.

Open questions:
* When to instantiate the Services with global side effects?
* How to handle the context in Services with global side effects?
* What happens if the user instantiate a Service manually  before the Autostart Services are started?

Services with global side effects(as example):
* [Web Socket Server](https://github.com/AmyAssist/Amy/blob/v0.7.1/chat-socket/src/main/java/io/github/amyassist/amy/socket/ChatServer.java)
* [Mail Notification Service](https://github.com/AmyAssist/Amy/blob/v0.7.1/plugins/email/src/main/java/io/github/amyassist/amy/plugin/email/MailUpdateService.java)
* [Task Scheduler Service](https://github.com/AmyAssist/Amy/blob/v0.7.1/core/src/main/java/io/github/amyassist/amy/core/taskscheduler/TaskSchedulerImpl.java)
* [Console Client](https://github.com/AmyAssist/Amy/blob/v0.7.1/core/src/main/java/io/github/amyassist/amy/core/console/ConsoleImpl.java)
* [Massaging Adapter](https://github.com/AmyAssist/Amy/blob/v0.7.1/amy-message-hub/src/main/java/io/github/amyassist/amy/messagehub/MQTTAdapter.java)
* [HTTP Callback Endpoint](https://github.com/AmyAssist/Amy/blob/v0.7.1/plugins/calendar/src/main/java/io/github/amyassist/amy/plugin/calendar/google/VerificationCodeReceiverService.java)

Related:
* [High Level Abstraction of the ServiceLocator](https://github.com/AmyAssist/Amy/issues/201)

## Considered Options

* Use ServiceLocator to instantiate first Service
* Special Autostart Services
* Bootstrap Components
* Autostart Component with service dependency
* Events triggered by Framework
* deep integration of Services with global side effects
* Services with global side effects not supported
* not specified

## Decision Outcome

Chosen option: "Bootstrap Components", because it have the lease drawbacks and make best use of the dependency injection.
This Option resolves all forces und is very simple.
No changes on the current Framework needed, only additions for a Bootstrapping Service.

### Positive Consequences

* No changes on the current Framework needed
* Easy declaration of Bootstrap component with annotation
* Make it possible to deploy application without a main method
* You have full control over the bootstrapping and can deactivate it or implement your own

### Negative Consequences

* Services that are bootstrapped from a bootstrap component can be accessed by other services with same interface

## Pros and Cons of the Options

### Use ServiceLocator to instantiate first Service

After loading all services, one service is instantiated using the `ServiceLocator.getService(class)` and a start method is called.

``` java
public static void main(String[] args) {
	DependencyInjection di = new DependencyInjection();
	di.loadServices();
	StartService startService = di.getServiceLocator().getService(StartService.class);
	startServic.start();
}
```

* Good, because it works without changing the Framework
* Good, because the entrypoint is specified explicitly
* Good, because the instant of instantiation of the first service can be chosen
* Bad, because the Framework has no control over the lifetime of the first Service
* Bad, because no standard for always running services
* Bad, because it't an imperative approach

### Special Autostart Services

Services can be annotated to be a RunnableService.
RunnableServices implement the [RunnableService interface](https://github.com/AmyAssist/Amy/blob/v0.7.1/api/src/main/java/io/github/amyassist/amy/core/service/RunnableService.java).

* Good, because easy to declare new always  running services
* Good, because the framework has control over the lifetime of the service
* Bad, because it makes the framework more complex due to a special case
* Bad, because the framework must interpret the implementation of services
* Bad, because special handling of context required

### Bootstrap Components

Components annotated with `Bootstrap` used to bootstrap the application.
This Components can represent Services with global side effects or instantiate them at a appropriated time in the future.
Because we use Components instead of Services they better match the global side effect pattern.
Dependencies can be declared to Services.
The Service that runs the Bootstrap components can be called to trigger the bootstrapping.

* Good, because components are dynamic entrypoints and don't have the context Problem
* Good, because declarative approach
* Good, because easy to realize and does not require changes in the framework
* Good, because easy use of dependency injection
* Bad, because Services with global side effects with a Java interface have context problems

### Autostart Component with service dependency

Use [Bootstrap Components](#3bootstrap-components), but the Component is only a wrapper for the Services, which is instantiated as a dependency of the Component.

* Good, because the service can be injected into other services and components
* Good, because declarative approach
* Bad, because context Problem with the context of the Autostart Component used by the service
* Bad, because require a wrapper Component for the service
* Bad, because it's not obvious why the wrapper is needed
* Bad, because wrapper can lead to code duplications

### Events triggered by Framework

Similar to [Special Autostart Services](#Special-Autostart-Services)
but can't see differentiation to a real event bus

* Good, because declarative approach
* Bad, because a lot of implementation effort
* Bad, because infinite running event handler is not a good solution
* Bad, because not clear what is triggert and how the services are handled

### deep integration of Services with global side effects

### Services with global side effects not supported

Services with global side effects can't be defined as part of the framework.
They can be defined outside of the framework and use the ServieLocator to interact with on demand services managed by the Framework.

* Good, because we can provide services on demand
* Good, because the framework don't have to be changed
* Good, because the instant of instantiation of the services with global side effects can be chosen
* Bad, because services outside of the framework can't use all features of the Framework
* Bad, because additional effort for user of the Framework
* Bad, because it's not possible to reuse services which require custom startup process
* Bad, because services outside of the framework can't be described declarative

### not specified

A Solution for the Problem is not specified.

* Good, because we do not have to make a decision
* Bad, because the problem is not solved
* Bad, because everyone must take care of there own solution
* Bad, because additional effort for user of the Framework
* Bad, because it's not possible to reuse services which require custom startup process
