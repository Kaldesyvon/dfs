#!/bin/sh
java -cp lib/dfslib-0.7.jar:bin/dfs.jar dfs.ReplicaLockService start 6001 localhost 9001 nomaster 0
