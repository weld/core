#!/bin/sh

cd ./examples/container-knopflerfish/target/weld-osgi-container-knopflerfish-1.1.4-SNAPSHOT-all/weld-osgi-container-knopflerfish-1.1.4-SNAPSHOT/
java -jar knopflerfish.jar -xargs conf/config.xargs
