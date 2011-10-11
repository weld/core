#!/bin/sh

cd ./examples/container-knopflerfish/target/weld-osgi-container-knopflerfish-1.1.3-SNAPSHOT-all/weld-osgi-container-knopflerfish-1.1.3-SNAPSHOT/
java -jar knopflerfish.jar -xargs conf/config.xargs
