echo $1
echo $2
echo $3
ssh -n 127.0.0.1 nohup java -jar /nfs/ug/homes-1/h/haquewar/distributed-system/M1/BasicStorageServer-stub/m3-server.jar $1 $2 $3
