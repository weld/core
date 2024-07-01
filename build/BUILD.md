Requirements
==============
- gradle
- maven
- sed
- java

Running build
================
You can run build by executing single "gradle" command and you will be asked for required attributes or you can provide them e.g:

 gradle -PweldVersion=6.0.0.Beta2 -PweldPath=/home/user/weld

You can also specify additional optional "readme" attribute which will override preface text in README file in created Weld distribution zip e.g:

 gradle -PweldVersion=6.0.0.Beta2 -PweldPath=/home/user/weld -Preadme="This is cutting edge CDI X.Y prototype implementation"
