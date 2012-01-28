#!/bin/sh

cd ./examples/container-knopflerfish/target/weld-osgi-container-knopflerfish-1.2.0-SNAPSHOT-all/weld-osgi-container-knopflerfish-1.2.0-SNAPSHOT/
java -jar knopflerfish.jar -xargs conf/config.xargs
