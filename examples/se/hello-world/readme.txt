====
    JBoss, Home of Professional Open Source
    Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
    contributors by the @authors tag. See the copyright.txt in the
    distribution for a full listing of individual contributors.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
====

-------------------
Running the Example
-------------------

As with all Weld SE applications, this example is executed
by starting Java with org.jboss.weld.environment.se.StartMain
as the main class. Of course you will need all of the relevant jar dependencies
on your classpath, which is most easily done by loading the project into your
favourite Maven-capable IDE and running it from there..

If you are using m2eclipse, and the application won't start, make sure you uncheck 
"Resolve dependencies from Workspace projects" in the Maven properties panel. Then
run a full build to ensure all classes are in the right place.

To run this example using Maven directly:

- Open a command line/terminal window in the examples/se/hello-world directory
- Ensure that Maven 3 is installed and in your PATH
- Ensure that the JAVA_HOME environment variable is pointing to your JDK installation
- execute the following command

mvn -Drun -Dname=Pete
