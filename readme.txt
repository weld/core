Web Beans RI
------------

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
