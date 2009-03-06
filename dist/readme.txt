Web Beans
------------

Java Contexts and Dependency Injection (JSR-299) is a new Java standard for 
dependency injection and contextual lifecycle management. Web Beans is the
reference implementation of JSR-299.

This distribution, as a whole, is licensed under the terms of the FSF Lesser Gnu 
Public License (see lgpl.txt). Parts of it are licensed under the Apache Public
License (see apl.txt). In particular, the API and the Web Beans runtime are 
licensed under the APL. At least these parts are licensed under the APL:

* src/impl/main/**/*
* src/api/main/**/*
* src/spi/main/**/*
* lib/webbeans/webbeans-core.jar
* lib/webbeans/jsr299-api.jar
* lib/webbeans/webbeans-spi.jar

This distribution consists of:

doc/
   -- The Reference guide, take a look at doc/en-US/html/index.html for getting 
      started using Web Beans and the facilities offered by JSR-299. 
 
examples/
   -- The Web Beans examples, the examples are described in more detail in the 
      reference guide 
   
jboss-as/
   -- Installer for JBoss AS, change into this directory, and run ant update
      There are more details in the reference guide

lib/
   -- Libraries for building the examples
   
lib/webbeans
   -- The Web Beans and API jars, for use outside of JBoss AS
   
src/
   -- The sources of Web Beans, including src/api, src/impl and 
      src/reference. To build the sources, just type mvn in the subdirectory.
