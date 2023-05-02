# Pulsar部署及实验手册

## 1. 部署pulsar
### 1.1 docker镜像下载
```shell
docker pull apachepulsar/pulsar-all:latest
```

### 1.2 更新conf文件及scripts文件
由于单节点部署原因，需修改bookkeeper.conf及broker.conf文件
```
bookkeeper.conf修改参数如下:
# Whether the bookie itself can start auto-recovery service also or not
autoRecoveryDaemonEnabled=false
```
```
broker.conf修改参数如下:
# Number of bookies to use when creating a ledger
managedLedgerDefaultEnsembleSize=1

# Number of copies to store for each message
managedLedgerDefaultWriteQuorum=1

# Number of guaranteed copies (acks to wait before write is complete)
managedLedgerDefaultAckQuorum=1
```

### 1.3 编写compose.yml
```
version: '3.4'

networks:
  pulsar:
    driver: bridge

services:

  zk1:
    container_name: zk1
    hostname: zk1
    image: apachepulsar/pulsar-all:latest
    command: >
      bash -c "python3 bin/apply-config-from-env.py conf/zookeeper.conf && \
               python3 bin/apply-config-from-env.py conf/pulsar_env.sh && \
               bin/generate-zookeeper-config.sh conf/zookeeper.conf && \
               exec bin/pulsar zookeeper"
    environment:
      ZOOKEEPER_SERVERS: zk1
    volumes:
      - ./conf/scripts/apply-config-from-env.py:/pulsar/bin/apply-config-from-env.py
    networks:
      pulsar:

  pulsar-init:
    container_name: pulsar-init
    hostname: pulsar-init
    image: apachepulsar/pulsar-all:latest
    command: ./bin/init-cluster.sh
    environment:
      clusterName: cluster-a
      zkServers: zk1:2181
      configurationStore: zk1:2181
      pulsarNode: proxy1
    volumes:
      - ./conf/scripts/init-cluster.sh/:/pulsar/bin/init-cluster.sh
    depends_on:
      - zk1
    networks:
      pulsar:

  bk1:
    hostname: bk1
    container_name: bk1
    image: apachepulsar/pulsar-all:latest
    command: >
      bash -c "export dbStorage_writeCacheMaxSizeMb="$${dbStorage_writeCacheMaxSizeMb:-16}" && \
               export dbStorage_readAheadCacheMaxSizeMb="$${dbStorage_readAheadCacheMaxSizeMb:-16}" && \
               python3 bin/apply-config-from-env.py conf/bookkeeper.conf && \
               python3 bin/apply-config-from-env.py conf/pulsar_env.sh && \
               python3 bin/watch-znode.py -z $$zkServers -p /initialized-$$clusterName -w && \
               exec bin/pulsar bookie"
    environment:
      clusterName: cluster-a
      zkServers: zk1:2181
      numAddWorkerThreads: 8
      useHostNameAsBookieID: "true"
    volumes:
      - ./conf/scripts/apply-config-from-env.py:/pulsar/bin/apply-config-from-env.py
      - ./conf/bookkeeper.conf:/pulsar/conf/bookkeeper.conf
      - ./conf/broker.conf:/pulsar/conf/broker.conf
    depends_on:
      - zk1
      - pulsar-init
    networks:
      pulsar:

  broker1:
    hostname: broker1
    container_name: broker1
    image: apachepulsar/pulsar-all:latest
    restart: on-failure
    command: >
      bash -c "python3 bin/apply-config-from-env.py conf/broker.conf && \
               python3 bin/apply-config-from-env.py conf/pulsar_env.sh && \
               python3 bin/watch-znode.py -z $$zookeeperServers -p /initialized-$$clusterName -w && \
               exec bin/pulsar broker"
    environment:
      clusterName: cluster-a
      zookeeperServers: zk1:2181
      configurationStore: zk1:2181
      webSocketServiceEnabled: "false"
      functionsWorkerEnabled: "false"
    volumes:
      - ./conf/scripts/apply-config-from-env.py:/pulsar/bin/apply-config-from-env.py
      - ./conf/bookkeeper.conf:/pulsar/conf/bookkeeper.conf
      - ./conf/broker.conf:/pulsar/conf/broker.conf
    depends_on:
      - zk1
      - pulsar-init
      - bk1
    networks:
      pulsar:

  proxy1:
    hostname: proxy1
    container_name: proxy1
    restart: on-failure
    image: apachepulsar/pulsar-all:latest
    command: >
      bash -c "python3 bin/apply-config-from-env.py conf/proxy.conf && \
               python3 bin/apply-config-from-env.py conf/pulsar_env.sh && \
               python3 bin/watch-znode.py -z $$zookeeperServers -p /initialized-$$clusterName -w && \
               exec bin/pulsar proxy"
    environment:
      clusterName: cluster-a
      zookeeperServers: zk1:2181
      configurationStoreServers: zk1:2181
      webSocketServiceEnabled: "true"
      functionWorkerWebServiceURL: http://fnc1:6750
    volumes:
      - ./conf/scripts/apply-config-from-env.py:/pulsar/bin/apply-config-from-env.py
    ports:
      - "6650:6650"
      - "8080:8080"
    depends_on:
      - zk1
      - pulsar-init
      - bk1
      - broker1
    networks:
      pulsar:
```
### 1.4 启动容器
```shell
sudo docker compose up -d
```
### 1.5 查看容器进程
```
edward@ubuntu:~/pulsar$ sudo docker ps -a
CONTAINER ID   IMAGE                            COMMAND                  CREATED          STATUS                      PORTS                                                                                  NAMES
799e446b88f4   apachepulsar/pulsar-all:latest   "bash -c 'python3 bi…"   15 minutes ago   Up 14 minutes               0.0.0.0:6650->6650/tcp, :::6650->6650/tcp, 0.0.0.0:8080->8080/tcp, :::8080->8080/tcp   proxy1
d293a748519f   apachepulsar/pulsar-all:latest   "bash -c 'python3 bi…"   15 minutes ago   Up 15 minutes                                                                                                      broker1
dbf4661a0b57   apachepulsar/pulsar-all:latest   "bash -c 'export dbS…"   15 minutes ago   Up 15 minutes                                                                                                      bk1
e96dab0f64ee   apachepulsar/pulsar-all:latest   "./bin/init-cluster.…"   15 minutes ago   Exited (0) 14 minutes ago                                                                                          pulsar-init
465afdea4f41   apachepulsar/pulsar-all:latest   "bash -c 'python3 bi…"   15 minutes ago   Up 15 minutes                                                                                                      zk1
```
### 1.6 进入Broker端容器, 生成JWT TOKEN需要用到的secret key
```

```