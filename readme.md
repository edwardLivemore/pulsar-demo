# Pulsar部署及实验手册

## 1. 部署pulsar
### 1.1 docker镜像下载
```shell
docker pull apachepulsar/pulsar-all:latest
```
### 1.2 创建pulsar目录及其子目录并授权
目录结构如下:
```
pulsar/
├── compose.yml
├── data
│   ├── bookkeeper
│   │   ├── journal
│   │   │   └── current
│   │   └── ledgers
│   │       └── current
│   └── zookeeper
│       └── version-2
```
由于docker容器访问权限问题， 需创建zookeeper用户及bookkeeper用户，且设置data/bookkeeper及其子目录的用户权限为
 bookkeeper:bookkeeper, data/zookeeper及其子目录用户权限为 zookeeper:zookeeper，且这些目录的访问权限为可读可写
```
edward@deptest225:~/pulsar/data$ ll
total 16
drwxr-xr-x 4 root       root       4096 Apr 25 13:58 ./
drwxr-xr-x 5 edward     root       4096 Apr 26 14:20 ../
drwxrwxrwx 4 bookkeeper bookkeeper 4096 Apr 25 14:18 bookkeeper/
drwxr-xr-x 3 zookeeper  zookeeper  4096 Apr 25 14:54 zookeeper/
```
### 1.3 编写compose.yml
```
version: '3'
networks:
  pulsar:
    driver: bridge
services:
# Start zookeeper
  zookeeper:
    image: apachepulsar/pulsar:latest
    container_name: zookeeper
    restart: on-failure
    networks:
      - pulsar
    volumes:
      - ./data/zookeeper:/pulsar/data/zookeeper
    environment:
      - metadataStoreUrl=zk:zookeeper:2181
    command: >
      bash -c "bin/apply-config-from-env.py conf/zookeeper.conf && \
             bin/generate-zookeeper-config.sh conf/zookeeper.conf && \
             exec bin/pulsar zookeeper"
    healthcheck:
      test: ["CMD", "bin/pulsar-zookeeper-ruok.sh"]
      interval: 10s
      timeout: 5s
      retries: 30

# Init cluster metadata
  pulsar-init:
    container_name: pulsar-init
    hostname: pulsar-init
    image: apachepulsar/pulsar:latest
    networks:
      - pulsar
    command: >
      bin/pulsar initialize-cluster-metadata \
               --cluster cluster-a \
               --zookeeper zookeeper:2181 \
               --configuration-store zookeeper:2181 \
               --web-service-url http://broker:8080 \
               --broker-service-url pulsar://broker:6650
    depends_on:
      zookeeper:
        condition: service_healthy

# Start bookie
  bookie:
    image: apachepulsar/pulsar:latest
    container_name: bookie
    restart: on-failure
    networks:
      - pulsar
    environment:
      - clusterName=cluster-a
      - zkServers=zookeeper:2181
      - metadataServiceUri=metadata-store:zk:zookeeper:2181
    depends_on:
      zookeeper:
        condition: service_healthy
      pulsar-init:
        condition: service_completed_successfully
  # Map the local directory to the container to avoid bookie startup failure due to insufficient container disks.
    volumes:
      - ./data/bookkeeper:/pulsar/data/bookkeeper
    command: bash -c "bin/apply-config-from-env.py conf/bookkeeper.conf
      && exec bin/pulsar bookie"

# Start broker
  broker:
    image: apachepulsar/pulsar:latest
    container_name: broker
    hostname: broker
    restart: on-failure
    networks:
      - pulsar
    environment:
      - metadataStoreUrl=zk:zookeeper:2181
      - zookeeperServers=zookeeper:2181
      - clusterName=cluster-a
      - managedLedgerDefaultEnsembleSize=1
      - managedLedgerDefaultWriteQuorum=1
      - managedLedgerDefaultAckQuorum=1
      - advertisedAddress=broker
      - advertisedListeners=external:pulsar://10.101.12.225:6650
    depends_on:
      zookeeper:
        condition: service_healthy
      bookie:
        condition: service_started
    ports:
      - "6650:6650"
      - "8080:8080"
    command: bash -c "bin/apply-config-from-env.py conf/broker.conf
      &&  exec bin/pulsar broker"
```
### 1.4 启动容器
```shell
sudo docker compose up -d
```
### 1.5 查看容器进程
```
sudo docker ps -a
edward@deptest225:~/pulsar$ sudo docker ps -a
[sudo] password for edward:
CONTAINER ID   IMAGE                        COMMAND                  CREATED             STATUS                         PORTS                                                                                  NAMES
5cf68895b734   apachepulsar/pulsar:latest   "bash -c 'bin/apply-…"   About an hour ago   Up About a minute              0.0.0.0:6650->6650/tcp, :::6650->6650/tcp, 0.0.0.0:8080->8080/tcp, :::8080->8080/tcp   broker
2cb0c695bc15   apachepulsar/pulsar:latest   "bash -c 'bin/apply-…"   About an hour ago   Up About a minute                                                                                                     bookie
66fe9003ad8f   apachepulsar/pulsar:latest   "bin/pulsar initiali…"   About an hour ago   Exited (0) About an hour ago                                                                                          pulsar-init
eb3047460dd6   apachepulsar/pulsar:latest   "bash -c 'bin/apply-…"   About an hour ago   Up About a minute (healthy)                                                                                           zookeeper
```
### 1.6 进入Broker端容器, 生成JWT TOKEN需要用到的secret key
```

```