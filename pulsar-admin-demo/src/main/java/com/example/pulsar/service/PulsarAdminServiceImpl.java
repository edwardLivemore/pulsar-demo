package com.example.pulsar.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.admin.PulsarAdmin;
import org.apache.pulsar.client.admin.PulsarAdminException;
import org.apache.pulsar.common.policies.data.AuthAction;
import org.apache.pulsar.common.policies.data.TenantInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class PulsarAdminServiceImpl implements PulsarAdminService {
    @Autowired
    private PulsarAdmin pulsarAdmin;

    @Value("${pulsar.cluster}")
    private String cluster;

    @Value("${pulsar.tenant1.name}")
    private String tenent1;

    @Value("${pulsar.tenant1.namespace}")
    private String namespace1;

    @Value("${pulsar.tenant1.topic}")
    private String topic1;

    @Value("${pulsar.tenant2.name}")
    private String tenent2;

    @Value("${pulsar.tenant2.namespace}")
    private String namespace2;

    @Value("${pulsar.tenant2.topic}")
    private String topic2;

    @Override
    public void init() {
        log.info("admin service init...");

        String role1 = tenent1 + "-producer";
        String role2 = tenent2 + "-producer";

        try {
            List<String> tenants = pulsarAdmin.tenants().getTenants();
            log.info("tenants: {}", tenants);
            if (!tenants.contains(tenent1)) {
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
            }

            if (!tenants.contains(tenent2)) {
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
            }

            List<String> namespaces1 = pulsarAdmin.namespaces().getNamespaces(tenent1);
            log.info("namespaces1: {}", namespaces1);
            if (!namespaces1.contains(namespace1)) {
                // 创建namespace
                pulsarAdmin.namespaces().createNamespace(namespace1);
            }

            List<String> namespaces2 = pulsarAdmin.namespaces().getNamespaces(tenent2);
            log.info("namespaces2 {}", namespaces2);
            if (!namespaces2.contains(namespace2)) {
                // 创建namespace
                pulsarAdmin.namespaces().createNamespace(namespace2);
            }

            List<String> topics1 = pulsarAdmin.topics().getList(namespace1);
            log.info("topics1: {}", topics1);
            if (!topics1.contains(topic1)) {
                // 创建topic
                pulsarAdmin.topics().createNonPartitionedTopic(topic1);
            }

            List<String> topics2 = pulsarAdmin.topics().getList(namespace2);
            log.info("topics2: {}", topics2);
            if (!topics2.contains(topic2)) {
                // 创建topic
                pulsarAdmin.topics().createNonPartitionedTopic(topic2);
            }

            // 授权
            // user1-producer
            pulsarAdmin.namespaces().grantPermissionOnNamespace(namespace1, role1, new HashSet<>(Collections.singletonList(AuthAction.produce)));
            // user2-producer
            pulsarAdmin.namespaces().grantPermissionOnNamespace(namespace2, role2, new HashSet<>(Collections.singletonList(AuthAction.produce)));
            // 查看授权
            Map<String, Set<AuthAction>> permissionMap1 = pulsarAdmin.namespaces().getPermissions(namespace1);
            for (Map.Entry<String, Set<AuthAction>> entry : permissionMap1.entrySet()) {
                log.info("permission ===> role:{}, actions: {}", entry.getKey(), entry.getValue());
            }

            Map<String, Set<AuthAction>> permissionMap2 = pulsarAdmin.namespaces().getPermissions(namespace2);
            for (Map.Entry<String, Set<AuthAction>> entry : permissionMap2.entrySet()) {
                log.info("permission ===> role:{}, actions: {}", entry.getKey(), entry.getValue());
            }
            log.info("admin service init done");
        } catch (PulsarAdminException e) {
            e.printStackTrace();
        }
    }
}
