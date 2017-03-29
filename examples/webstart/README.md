Weld SE Numberguess example (WebStart)
======================================

This web application allows the weld-se-numberguess (Swing)
application to be run using Java Web Start.

Running the Example
-------------------

- Make sure that the weld-se-numberguess application, located
  in `examples/se/numberguess` is built. If not, run `mvn clean install`
  in `examples/se/numberguess`

- Build and run the launcher application by running `mvn clean jetty:run-war` in this
  directory. This will build the application and start a Jetty server hosting the application.
  Alternatively, you can deploy the `weld-se-numberguess-webstart.war` to any other Servlet container.
  
- Launch the Java WebStart application by running 
  `http://localhost:9090/weld-se-numberguess-webstart/webstart/weld-se-numberguess.jnlp`
  The weld-se-numberguess application is self-signed. Therefore, you may need to configure
  a less strict security policy for Java WebStart first. This can be done by running `jcontrol`
  
Note that Java WebStart applications are cached locally. In order to clear the cache, run `javaws uninstall`

