package com.tsingtech.ws.websocket.discovery;

import com.netflix.appinfo.PropertiesInstanceConfig;

public class InstanceConfig extends PropertiesInstanceConfig {
    private Integer port = 8080;

    public InstanceConfig() { }

    public InstanceConfig(Integer port) {
        this.port = port;
    }

    @Override
    public int getNonSecurePort(){
        return this.port;
    }
}