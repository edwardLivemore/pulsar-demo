### 消费消息
```shell
bin/pulsar-client \
--url "pulsar://127.0.0.1:6650" \
--auth-plugin "org.apache.pulsar.client.impl.auth.AuthenticationToken" \
--auth-params {"token":"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyMS1wcm9kdWNlciJ9.0OoVBSs5ZndG-vytuAybr5edEdD1MpXXApDP9RJJlQI"} \
consume persistent://user1/ns/topic -s "user1-subscription"
```