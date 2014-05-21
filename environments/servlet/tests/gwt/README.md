[general]
This is a minimal GWT example using maven to demonstrate https://issues.jboss.org/browse/WELD-1615


src/main/webapp/WEB-INF/web.xml will start org.jboss.weld.environment.servlet.Listener
which will fail.


[build]
Run
    mvn clean install

[run with maven]
Run
    mvn gwt:run

Open your browser
   http://localhost:8888

To use the code server (requires a browser plugin) go to
   http://127.0.0.1:8888/Testi.html?gwt.codesvr=127.0.0.1:9997

[run with eclipse]
Make sure that the GWT plugin is installed. See therefore,
    https://developers.google.com/eclipse/docs/getting_started

Import this project by using the "Existing Maven Projects" wizzard.

Activate the GWT nature Properties -> Google -> Web Toolkit -> Use Google Web Toolkit


Then, run the "testi.launch" run configuration

Open your browser
   http://localhost:8888

To use the code server (requires a browser plugin) go to
    http://127.0.0.1:8888/Testi.html?gwt.codesvr=127.0.0.1:9997

