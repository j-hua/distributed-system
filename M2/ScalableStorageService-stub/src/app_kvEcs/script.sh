echo $1
ssh -n 127.0.0.1 nohup java -jar /nfs/ug/homes-1/h/haquewar/distributed-system/M2/ScalableStorageService-stub/ms3-server.jar $1
clear
