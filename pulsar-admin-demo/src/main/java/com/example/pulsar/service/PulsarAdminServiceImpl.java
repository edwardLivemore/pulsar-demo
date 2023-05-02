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
    private String tenent;

    @Value("${pulsar.tenant1.namespace}")
    private String namespace;

    @Value("${pulsar.tenant1.topic}")
    private String topic;

    @Override
    public void init() {
        log.info("admin service init...");

        String roleA = tenent + "RoleA";

        try {
            List<String> tenants = pulsarAdmin.tenants().getTenants();
            log.info("tenants: {}", tenants);
            if (!tenants.contains(tenent)) {
                // 创建租户及角色
                pulsarAdmin.tenants().createTenant(tenent, new TenantInfo() {
                    @Override
                    public Set<String> getAdminRoles() {
                        return new HashSet<>(Collections.singletonList(roleA));
                    }

                    @Override
                    public Set<String> getAllowedClusters() {
                        return new HashSet<>(Collections.singletonList("cluster-a"));
                    }
                });
            }

            List<String> namespaces = pulsarAdmin.namespaces().getNamespaces(tenent);
            log.info("namespaces: {}", namespaces);
            if (!namespaces.contains(namespace)) {
                // 创建namespace
                pulsarAdmin.namespaces().createNamespace(namespace);
            }

            List<String> topics = pulsarAdmin.topics().getList(namespace);
            log.info("topics: {}", topics);
            if (!topics.contains(topic)) {
                // 创建topic
                pulsarAdmin.topics().createNonPartitionedTopic(topic);
            }

            // 授权
            pulsarAdmin.namespaces().grantPermissionOnNamespace(namespace, roleA, new HashSet<>(Collections.singletonList(AuthAction.produce)));
            // 查看授权
            Map<String, Set<AuthAction>> permissionMap = pulsarAdmin.namespaces().getPermissions(namespace);
            for (Map.Entry<String, Set<AuthAction>> entry : permissionMap.entrySet()) {
                log.info("permission ===> role:{}, actions: {}", entry.getKey(), entry.getValue());
            }

            log.info("admin service init done");
        } catch (PulsarAdminException e) {
            e.printStackTrace();
        }
    }
}
