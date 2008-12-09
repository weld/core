The Web Beans RI currently comes with a single example, webbeans-numberguess.

To run the example on JBoss AS 5.0.0.GA, you need to add the Web Beans RI 
deployer to JBoss 5. First, set the path to JBoss 5 in ../build.properties. 
Then, run ant -f ../build.xml install-jboss5 in the examples directory.

To deploy the example to JBoss AS 5, change into numberguess directory and 
choose between:

* ant restart / ant explode - deploy the example in exploded format
* ant deploy - deploy the example in compressed jar format
* ant undeploy - remove the example from the server
* ant clean - clean the example
