package com.tsingtech.ws.websocket.discovery;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.LeaseInfo;
import com.netflix.discovery.DiscoveryClient;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.eureka.EurekaClientConfigBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class WsRegister implements SmartInitializingSingleton {

    @Value("${eureka.client.serviceUrl.defaultZone}")
    private String defaultZone;

    @Value("${netty-websocket.discovery.serviceName}")
    private String serviceName;

    @Value("${netty-websocket.discovery.port}")
    private Integer port;

    @Autowired
    private EurekaInstanceConfig config;

    private DiscoveryClient discoveryClient;

    @Override
    public void afterSingletonsInstantiated() {
        EurekaClientConfigBean eurekaClientConfigBean = new EurekaClientConfigBean();
//        eurekaClientConfigBean.setProxyHost("127.0.0.1");
//        eurekaClientConfigBean.setProxyPort("8880");
        eurekaClientConfigBean.setServiceUrl(new HashMap<String, String>(){{
            put("defaultZone", defaultZone);
        }});

        this.discoveryClient = new DiscoveryClient(new ApplicationInfoManager(new InstanceConfig(port), create(config)),
                eurekaClientConfigBean);
    }

    private InstanceInfo create(EurekaInstanceConfig config) {
        LeaseInfo.Builder leaseInfoBuilder = LeaseInfo.Builder.newBuilder()
                .setRenewalIntervalInSecs(config.getLeaseRenewalIntervalInSeconds())
                .setDurationInSecs(config.getLeaseExpirationDurationInSeconds());

        // Builder the instance information to be registered with eureka
        // server
        InstanceInfo.Builder builder = InstanceInfo.Builder.newBuilder();

        String namespace = config.getNamespace();
        if (!namespace.endsWith(".")) {
            namespace = namespace + ".";
        }
        builder.setNamespace(namespace).setAppName(serviceName)
                .setInstanceId(String.join(":", config.getIpAddress(), serviceName, String.valueOf(port)))
                .setAppGroupName(config.getAppGroupName())
                .setDataCenterInfo(config.getDataCenterInfo())
                .setIPAddr(config.getIpAddress())
                .setHostName(config.getIpAddress())
                .setPort(port)
                .enablePort(InstanceInfo.PortType.UNSECURE, config.isNonSecurePortEnabled())
                .setSecurePort(config.getSecurePort())
                .enablePort(InstanceInfo.PortType.SECURE, config.getSecurePortEnabled())
//                .setVIPAddress(serviceName)
//                .setSecureVIPAddress(serviceName)
                .setHomePageUrl("/", null)
                .setStatusPageUrl(config.getStatusPageUrlPath(), config.getStatusPageUrl())
                .setHealthCheckUrls(config.getHealthCheckUrlPath(), config.getHealthCheckUrl(), config.getSecureHealthCheckUrl());
        builder.setStatus(InstanceInfo.InstanceStatus.UP);

        // Add any user-specific metadata information
        for (Map.Entry<String, String> mapEntry : config.getMetadataMap().entrySet()) {
            String key = mapEntry.getKey();
            String value = mapEntry.getValue();
            // only add the metadata if the value is present
            if (value != null && !value.isEmpty()) {
                builder.add(key, value);
            }
        }
        builder.add("management.port", port+"");
        InstanceInfo instanceInfo = builder.build();
        instanceInfo.setLeaseInfo(leaseInfoBuilder.build());
        return instanceInfo;
    }
}
