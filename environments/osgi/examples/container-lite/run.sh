#!/bin/sh

cd ./target/weld-osgi-container-lite-1.1.3-SNAPSHOT-all/weld-osgi-container-lite-1.1.3-SNAPSHOT/;

LIBS=./modules
LIBS_CLASSPATH=`find $LIBS -type f -name \*.jar` 
LIBS_CLASSPATH=`echo $LIBS_CLASSPATH | tr ' ' ':'`

java -cp bin/pojosr.jar:$LIBS_CLASSPATH de.kalpatec.pojosr.framework.PojoSR

