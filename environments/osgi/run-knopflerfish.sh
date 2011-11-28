#!/bin/sh

cd ./examples/container-knopflerfish/target/weld-osgi-container-knopflerfish-1.1.5-SNAPSHOT-all/weld-osgi-container-knopflerfish-1.1.5-SNAPSHOT/
java -jar knopflerfish.jar -xargs conf/config.xargs
