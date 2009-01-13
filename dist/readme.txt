Web Beans RI
------------

Web Beans (JSR-299) is a new Java standard for dependency injection and 
contextual lifecycle management.

This distribution, as a whole, is licensed under the terms of the FSF Lesser Gnu 
Public License (see lgpl.txt). Parts of it are licensed under the Apache Public
License (see apl.txt). In particular, the Web Beans API and the Web Beans RI 
rutimes are licensed under the APL. At least these parts are licensed under the 
APL:

* src/webbeans-ri/main/**/*
* src/webbeans-api/main/**/*
* src/webbeans-ri-spi/main/**/*
* lib/webbeans/webbeans-ri.jar
* lib/webbeans/webbeans-api.jar
* lib/webbeans/webbeans-ri-spi.jar

This distribution consists of:

doc/
   -- The Web Beans Reference guide, take a look at doc/en/html/index.html for
      getting started using Web Beans and the Web Beans RI
 
examples/
   -- The Web Beans RI examples, the examples are described in more detail in 
      the reference guide 
   
jboss-as/
   -- Installer for JBoss AS, change into this directory, and run ant update
      There are more details in the reference guide

lib/
   -- Libraries for building the examples
   
lib/webbeans
   -- The Web Beans RI and API jars, for use outside of JBoss AS
   
src/
   -- The sources of the Web Beans RI, including src/webbeans-api, 
      src/webbeans-ri and src/reference. To build the sources, just type mvn in
      the subdirectory.
