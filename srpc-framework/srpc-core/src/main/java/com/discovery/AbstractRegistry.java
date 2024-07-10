package com.discovery;

import com.sunjinsong.ServiceConfig;
import org.apache.zookeeper.KeeperException;

public class AbstractRegistry implements Registry{
    @Override
    public void register(ServiceConfig<?> serviceConfig) throws InterruptedException, KeeperException {

    }
}
