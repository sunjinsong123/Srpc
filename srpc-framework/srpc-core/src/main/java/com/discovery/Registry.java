package com.discovery;
//注册中心应该具有哪些能力

import com.sunjinsong.ServiceConfig;
import org.apache.zookeeper.KeeperException;

import java.net.InetSocketAddress;

public interface Registry {
    public void register(ServiceConfig<?> serviceConfig) throws InterruptedException, KeeperException;

    /*从注册中心拉取一个可用的服务
     *@param name 服务的名字
     * @return
     *@
     * */
    InetSocketAddress lookup(String name) throws InterruptedException, KeeperException;
}
