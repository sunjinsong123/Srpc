package com.discovery;

import com.sunjinsong.ServiceConfig;
import org.apache.zookeeper.KeeperException;

import java.net.InetSocketAddress;

public class AbstractRegistry implements Registry{
    @Override
    public void register(ServiceConfig<?> serviceConfig) throws InterruptedException, KeeperException {

    }

    @Override
    public InetSocketAddress lookup(String ServicrName) throws InterruptedException, KeeperException {
        return null;
    }
}
