This example is similar to its sibling "conversations" example, but implemented 
with wicket and the wicket-webbeans integration.  It also follows the wicket 
standard usage of running from within eclipse with jetty.  So, for example, to 
run the app, right-click on Start.java in the project and choose "Run as Java 
Application," which will launch jetty with the example.  Then hit 
http://localhost:8080/


Note that conversational behavior for wicket is different than that of jsf.  
Roughly:

- Conversations are started/ended in the same way, with Conversation.begin/end

- Conversations are by default propagated to new PageTargets, whether directly 
instantiated or created with boomkarkable mounts.  In the former case, the new 
Page instance will have the conversation id embedded in its page metadata.  In 
the latter, a "cid" parameter is passed along.

- To switch a conversation, you need to explicitly pass the cid in wicket 
RequestParameters.  See the example.


