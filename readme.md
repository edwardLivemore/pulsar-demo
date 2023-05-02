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


#volumes:
#  zk1-data:
#  zk2-data:
#  zk3-data:
#  bk1-data:
#  bk2-data:
#  bk3-data:
#  manager-data:

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
#      ZOOKEEPER_SERVERS: zk1,zk2,zk3
      ZOOKEEPER_SERVERS: zk1
      JAVA_OPTS: -Xms1024m -Xmx1024m
    volumes:
      - ./conf/scripts/apply-config-from-env.py:/pulsar/bin/apply-config-from-env.py
#      - ./conf/zookeeper1.conf:/pulsar/conf/zookeeper1.conf
#      - zk1-data:/pulsar/data/zookeeper
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
#      zkServers: zk1:2181,zk2:2181,zk3:2181
      zkServers: zk1:2181
      numAddWorkerThreads: 8
      useHostNameAsBookieID: "true"
      JAVA_OPTS: -Xms1024m -Xmx1024m
    volumes:
      - ./conf/scripts/apply-config-from-env.py:/pulsar/bin/apply-config-from-env.py
      - ./conf/bookkeeper.conf:/pulsar/conf/bookkeeper.conf
      - ./conf/broker.conf:/pulsar/conf/broker.conf
#      - bk1-data:/pulsar/data/bookkeeper
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
      #      zookeeperServers: zk1:2181,zk2:2181,zk3:2181
      zookeeperServers: zk1:2181
      #      configurationStore: zk1:2181,zk2:2181,zk3:2181
      configurationStore: zk1:2181
      webSocketServiceEnabled: "false"
      functionsWorkerEnabled: "false"
      JAVA_OPTS: -Xms1024m -Xmx1024m
    volumes:
      - ./conf/scripts/apply-config-from-env.py:/pulsar/bin/apply-config-from-env.py
      - ./conf/bookkeeper.conf:/pulsar/conf/bookkeeper.conf
      - ./conf/broker.conf:/pulsar/conf/broker.conf
      - ./keys/secret.key:/pulsar/secret.key
    depends_on:
      - zk1
      - pulsar-init
      - bk1
    ports:
      - "6650:6650"
      - "8080:8080"
    networks:
      pulsar:
```
### 1.4 启动容器
```shell
sudo docker compose up -d
```

### 1.5 进入Broker端容器, 生成JWT TOKEN需要用到的secret key
```
bin/pulsar tokens create-secret-key --output  /pulsar/secret.key --base64
```

### 1.6 通过secret.key 生成token
```shell
bin/pulsar tokens create --secret-key /pulsar/secret.key --subject admin
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiJ9.EPWQdbkfBQO_6NsG-zYsmqZ_kF6Cfc_kslq_7LF-99M

bin/pulsar tokens create --secret-key /pulsar/secret.key --subject user1-producer
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyMS1wcm9kdWNlciJ9.0OoVBSs5ZndG-vytuAybr5edEdD1MpXXApDP9RJJlQI

bin/pulsar tokens create --secret-key /pulsar/secret.key --subject user2-producer
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyMi1wcm9kdWNlciJ9.zfz-9ZkAl4CMiBKdX0LR_geja9itvbrggKiqAQhXsww
```

保存并导出secret.key，以便后续重启容器时使用

### 1.7 验证token
```shell
bin/pulsar tokens validate -sk  /pulsar/secret.key -i "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiJ9.EPWQdbkfBQO_6NsG-zYsmqZ_kF6Cfc_kslq_7LF-99M"
{sub=admin}

bin/pulsar tokens validate -sk  /pulsar/secret.key -i "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyMS1wcm9kdWNlciJ9.0OoVBSs5ZndG-vytuAybr5edEdD1MpXXApDP9RJJlQI"
{sub=user1-producer}

bin/pulsar tokens validate -sk  /pulsar/secret.key -i "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyMi1wcm9kdWNlciJ9.zfz-9ZkAl4CMiBKdX0LR_geja9itvbrggKiqAQhXsww"
{sub=user2-producer}
```

### 1.8 修改broker.conf
```
# Enable authentication
authenticationEnabled=true

# Authentication provider name list, which is comma separated list of class names
authenticationProviders=org.apache.pulsar.broker.authentication.AuthenticationProviderToken

# Role names that are treated as "super-user", meaning they will be able to do all admin
# operations and publish/consume from all topics
superUserRoles=admin

# Authentication settings of the broker itself. Used when the broker connects to other brokers,
# either in same or other clusters
brokerClientAuthenticationPlugin=org.apache.pulsar.client.impl.auth.AuthenticationToken
brokerClientAuthenticationParameters={"token":"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiJ9.EPWQdbkfBQO_6NsG-zYsmqZ_kF6Cfc_kslq_7LF-99M"}

## Symmetric key
# Configure the secret key to be used to validate auth tokens
tokenSecretKey=/pulsar/secret.key
```

### 1.9 重启容器
```shell
sudo docker-compose down
sudo docker-compose up -d
```

### 1.10 使用admin的token配置admin端
```
@Bean
public PulsarAdmin getPulsarAdmin() throws PulsarClientException {
    return PulsarAdmin.builder()
//                .authentication("com.org.MyAuthPluginClass", )
            .serviceHttpUrl(pulsarServiceUrl)
            .authentication(AuthenticationFactory.token("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiJ9.EPWQdbkfBQO_6NsG-zYsmqZ_kF6Cfc_kslq_7LF-99M"))
            .tlsTrustCertsFilePath(null)
            .allowTlsInsecureConnection(false)
            .build();
}
```

### 1.11 创建多租户
```
// 创建租户1及角色
pulsarAdmin.tenants().createTenant(tenent1, new TenantInfo() {
    @Override
    public Set<String> getAdminRoles() {
        return new HashSet<>(Collections.singletonList(tenent1 + "-admin"));
    }

    @Override
    public Set<String> getAllowedClusters() {
        return new HashSet<>(Collections.singletonList("cluster-a"));
    }
});

// 创建租户2及角色
pulsarAdmin.tenants().createTenant(tenent2, new TenantInfo() {
    @Override
    public Set<String> getAdminRoles() {
        return new HashSet<>(Collections.singletonList(tenent2 + "-admin"));
    }

    @Override
    public Set<String> getAllowedClusters() {
        return new HashSet<>(Collections.singletonList("cluster-a"));
    }
});
```

### 1.12 创建namespace
```
pulsarAdmin.namespaces().createNamespace(namespace1);
pulsarAdmin.namespaces().createNamespace(namespace2);
```

### 1.13 创建topic
```
pulsarAdmin.topics().createNonPartitionedTopic(topic1);
pulsarAdmin.topics().createNonPartitionedTopic(topic2);
```

### 1.14 授权
```
// user1-producer
pulsarAdmin.namespaces().grantPermissionOnNamespace(namespace1, role1, new HashSet<>(Collections.singletonList(AuthAction.produce)));
// user2-producer
pulsarAdmin.namespaces().grantPermissionOnNamespace(namespace2, role2, new HashSet<>(Collections.singletonList(AuthAction.produce)));
```

### 1.15 使用user1-producer的token配置produce端
```
@Bean
public PulsarClient pulsarClient() throws PulsarClientException {
    return PulsarClient.builder()
            .serviceUrl(pulsarUrl)
            .authentication(AuthenticationFactory.token("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyMS1wcm9kdWNlciJ9.0OoVBSs5ZndG-vytuAybr5edEdD1MpXXApDP9RJJlQI"))
            .tlsTrustCertsFilePath(null)
            .allowTlsInsecureConnection(false)
            .build();
}

@Bean
public Producer<PersonInfo> producer(PulsarClient client) throws PulsarClientException {
    return client.newProducer(Schema.JSON(PersonInfo.class))
            .topic(topic)
            .accessMode(ProducerAccessMode.Shared)
            .create();
}
```

### 1.15 使用admin的token配置消费端
因为user1-producer及user2-producer的权限只能生产消息，不能消费消息, 所以使用admin的token消费消息
```
@Bean
public PulsarClient pulsarClient() throws PulsarClientException {
    return PulsarClient.builder()
            .serviceUrl(pulsarUrl)
            .authentication(AuthenticationFactory.token("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiJ9.EPWQdbkfBQO_6NsG-zYsmqZ_kF6Cfc_kslq_7LF-99M"))
            .tlsTrustCertsFilePath(null)
            .allowTlsInsecureConnection(false)
            .build();
}

@Bean
public Consumer<PersonInfo> consumer(PulsarClient client) throws PulsarClientException {
    return client.newConsumer(Schema.JSON(PersonInfo.class))
            .topic(topic)
            .subscriptionName(SCRIBE)
            .subscriptionType(SubscriptionType.Failover)
            .subscribe();
}
```