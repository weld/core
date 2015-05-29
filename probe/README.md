Probe REST API description
==========================

| HTTP method | Path          | Content type | Description |
|------------|---------------|--------------|--------------|
| GET | /deployment | application/json | All CDI Beans deployment archives |
| GET | /beans      | application/json | All beans available on the running container <br/><br/> Available filters: <ul><li>kind</li><li>beanClass</li><li>beanType</li><li>qualifier</li><li>scope</li><li>bda</li><li>isAlternative</li><li>stereotypes</li></ul>|
| GET | /beans/{id} | application/json | Bean detail |
| GET | /beans/{id}/instance | application/json | Bean instance details |
| GET | /observers | application/json |All available Observer methods <br/><br/> Available filters: <ul><li>beanClass</li><li>observedType</li><li>qualifier</li><li>reception</li><li>txPhase</li><li>kind</li><li>bda</li></ul>|
| GET | /observers/{id} |application/json | Observer detail||
| GET | /contexts | application/json |Monitored contexts (Conversation, Session, Application)|
| GET | /contexts/{context}| application/json  |All instances for given context|
| GET | /invocations | application/json |All method invocations trees <br/><br/> Available filters: <ul><li>beanClass</li><li>methodName</li></ul>|
| GET | /invocations/{id} | application/json | Invocation tree detail containing JsonArray of all childs invocation methods|
| GET | /events | application/json | All fired events <br/><br/> Available filters:<ul><li>kind</li><li>type</li><li>qualifiers</li><li>eventInfo</li></ul>|

Besides the specified filter key values it's possible to use the following for collection of resources:
* pageSize - set number of items per page, if it is 0 then all items will be displayed 
* page - go to the specified page





