#!/usr/bin/env bash
ssh -n $1 nohup java -jar /Users/JHUA/school_proj/distributed-system/M2/ScalableStorageService-stub/ms3-server.jar $2 127.0.0.1 40000 &
