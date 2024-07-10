package com.discovery;
//注册中心应该具有哪些能力

import com.sunjinsong.ServiceConfig;
import org.apache.zookeeper.KeeperException;

public interface Registry {
    public void register(ServiceConfig<?> serviceConfig) throws InterruptedException, KeeperException;
}
