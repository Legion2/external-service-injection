# Attache connector implementation to NodeTemplate with custom type

* Deciders: Leon Kiefer
* Date:2019-04-08

## Context and Problem Statement

When a TOSCA Service is required by the Java applications the implementation of the adapter service is not present.
The implementation is chosen based on the available TOSCA Service and is specific to that TOSCA Service.
The Java adapter service implementation is an artifact packaged as jar and must be downloaded from somewhere.

## Considered Options

* use TOSCA Deployment Artifacts of one Node Template
* use artifact with custom artifact type
* use custom property pointing to artifact

## Decision Outcome

Chosen option: "use TOSCA Deployment Artifacts of one Node Template", because it then it is packaged into the csar of the TOSCAService.
This is combined with the "use Artifact with custom Artifact Type" Option to uniquely identify the artifact as of the required type.
The used type namespace is `http://legion2.github.io/tosca/artifacttemplates`.

### Positive Consequences

* Prevent side effects, as TOSCA should not interpret the Artifact
* works out of the Box, with no modifications to TOSCA

### Negative Consequences

* multiple different artifacts of that type could be present

## Pros and Cons of the Options <!-- optional -->

### use TOSCA Deployment Artifacts of one Node Template

The jar containing the service implementation is attached as a Deployment Artifact to a node template in the Service Template.

* Good, because simple management of Artifacts
* Good, because works out of the Box
* Bad, because Deployment Artifact may be interpreted by TOSCA, resulting in side effects
* Bad, because which node template is the Deployment Artifact attached to?

### use Artifact with custom Artifact Type

Create custom Artifact Type for jars containing Service Implementation for the Service Injection

* Good, because simple management of Artifacts
* Good, because makes it possible to identify by type
* Bad, because custom Artifact Type must be defined
* Bad, because where to attache the Artifact to

### use custom Property pointing to Artifact

Store the url of the Artifact in a Property

* Good, because flexible, Artifact must not be uploaded to TOSCA
* Bad, because how to structure the Property
