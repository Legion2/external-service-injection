# Use Multiple ClassLoaders

* Deciders: Leon Kiefer
* Date: 2019-04-14

## Context and Problem Statement

In Java classes and resources are loaded using ClassLoaders, they build a hirachy and can be extendend by new ClassLoaders at Runtime.
For modular applications are packaged in multipe jars and must be loaded using multiple ClassLoaders.
Because the Framework should support declarative programming a discovery mechanism must be provided, which makes it possible to load classes form all ClassLoaders.

Some libraries use Thread Context Class Loader for discovery, but that requires that the caller knows the ClassLoader which contains the needed resouces.
That is not what we want.

## Considered Options

* Multiple ClassLoaders
* One ClassLoader
* Special ClassLoader
* No solution on Framework layer

## Decision Outcome

Chosen option: "Multiple ClassLoaders", because is is transparent and easy to add new ClassLoaders. No magic is done in the ClassLoaders.

### Positive Consequences

* the class loading is transparent
* simple to add custom ClassLoader

### Negative Consequences

* difficult to integrate this solution with classpath scanning

### Multiple ClassLoaders

Use multiple ClassLoaders, so each jar have it's own.
The ClassLoaders have a commen parent and are added to a list of ClassLoaders of the Framework.
This way Classes can be loaded form all ClassLoaders, by going though the list.

* Good, because the class loading is transparent
* Good, because very simple solution
* Good, because simple to add custom ClassLoader
* Bad, because does not work out of the box with other solutions

### One ClassLoader

Use one URLClassLoader which loads form all jars.
This ClassLoader must be updated if new jar must be included.

* Good, because no framentation of classes
* Good, because no problem of loading classes from other jars
* Bad, because ClassLoader should be immutable
* Bad, because URLClassLoader must be extended
* Bad, because only URLClassLoader can be used

### Special ClassLoader

Create a Special ClassLoader, which knows it's children and searches in all Childs after/before it's parent.
This ClassLoader must detect if the Children delegates the loading to it.

* Good, because child ClassLoaders don't have to care about
* Good, because no changes on users code required
* Bad, because difficult implementation and detection

### No solution on Framework layer

The Framework does not provide any function to manage ClassLoaders or specify how the classloading should be implemented.

* Good, because users can use own solution
* Bad, because for bigger projects there are multiple ClassLoaders that must be managed
* Bad, because of bad compatiblity of different solutions
