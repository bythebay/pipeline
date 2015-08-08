#!/bin/bash
#
echo ...Building Feeder app...
cd $PIPELINE_HOME
sbt feeder/assembly
echo ...Starting Feeder app...
java -Xmx1g -jar feeder/target/scala-2.10/feeder-assembly-1.0.jar 2>&1 1>feeder-out.log &
echo    logs available by tailing feeder-out.log