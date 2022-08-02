# KBomber

Getting Started
=====
KBomber is a personal *Kotlin* set of libraries.
If you want to use this set in your *Gradle* project, you have to add the [Jitpack](https://jitpack.io/) to the repository in `build.gradle`:

	repositories {
    	...
        maven { url 'https://jitpack.io' }
    }


KBomber Collections
=====
To use this module add this line in your `build.gradle` dependencies:

	implementation 'com.github.LM-96.KBomber:kbomber-collections:version'
    
(replace version with the lastest release name)

*KBomber Collections* actually contains some classes for:
- *parameters*: containers of object that can be stored and then retrieved with a name;
- *values*: immutable or loadable values that are encapsulated inside useful classes.

KBomber Reflection
=====
To use this module add this line in your `build.gradle` dependencies:

	implementation 'com.github.LM-96.KBomber:kbomber-reflection:version'
    
(replace version with the lastest release name)

*KBomber Reflection* actually contains some classes for:
- *method*: utility classes to invoke method that can also be suspend or to check signature;
- *classes and intefaces*: utility classes to check the hierarchy by using reflection.

KBomberx Concurrency
=====
To use this module add this line in your `build.gradle` dependencies:

	implementation 'com.github.LM-96.KBomber:kbomberx-concurrency:version'
    
(replace version with the lastest release name)

*KBomberx Reflection* actually contains some classes for *Kotlin* concurrecy.
This classes expose functionality to create shared and synchronized objects between coroutines and add supports for concurrency both in shared memory (like conditions) and in message exchanging (using channels).