JSON-B ExecutionContextSerializer
=================================

A Spring Batch `ExecutionContextSerializer` implementation that uses [JSON-B](http://json-b.net) to provide (de)serialization.

This can act as a replacement for the default [Jackson](https://github.com/FasterXML/jackson) based serialization that Spring Batch offers.

Usage
-----

You need to have a JSON-B implementation eg. [Eclipse Yasson](https://projects.eclipse.org/projects/ee4j.yasson), this is automatically the case if you deploy in a Jakarta EE container.




