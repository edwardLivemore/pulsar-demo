### 1. 创建token
```shell
bin/pulsar tokens create --secret-key /pulsar/secret.key --subject admin
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiJ9.EPWQdbkfBQO_6NsG-zYsmqZ_kF6Cfc_kslq_7LF-99M

bin/pulsar tokens create --secret-key /pulsar/secret.key --subject user1-producer
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyMS1wcm9kdWNlciJ9.0OoVBSs5ZndG-vytuAybr5edEdD1MpXXApDP9RJJlQI

bin/pulsar tokens create --secret-key /pulsar/secret.key --subject user2-producer
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyMi1wcm9kdWNlciJ9.zfz-9ZkAl4CMiBKdX0LR_geja9itvbrggKiqAQhXsww
```

### 2. 验证token
```shell
bin/pulsar tokens validate -sk  /pulsar/secret.key -i "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiJ9.EPWQdbkfBQO_6NsG-zYsmqZ_kF6Cfc_kslq_7LF-99M"
{sub=admin}

bin/pulsar tokens validate -sk  /pulsar/secret.key -i "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyMS1wcm9kdWNlciJ9.0OoVBSs5ZndG-vytuAybr5edEdD1MpXXApDP9RJJlQI"
{sub=user1-producer}

bin/pulsar tokens validate -sk  /pulsar/secret.key -i "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyMi1wcm9kdWNlciJ9.zfz-9ZkAl4CMiBKdX0LR_geja9itvbrggKiqAQhXsww"
{sub=user2-producer}
```