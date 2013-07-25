#!/bin/sh

cd ./examples/container-knopflerfish/target/weld-osgi-container-knopflerfish-all/weld-osgi-container-knopflerfish/
java -jar knopflerfish.jar -xargs conf/config.xargs
