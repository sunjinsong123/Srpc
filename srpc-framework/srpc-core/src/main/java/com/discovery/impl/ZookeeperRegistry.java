package com.discovery.impl;

import com.discovery.AbstractRegistry;
import com.discovery.Registry;
import com.sunjinsong.ServiceConfig;
import com.sunjinsong.common.Constant;
import com.sunjinsong.utils.net.NetUtils;
import com.sunjinsong.utils.zookeeper.ZookeeperNode;
import com.sunjinsong.utils.zookeeper.ZookeeperUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.time.chrono.AbstractChronology;

@Slf4j
public class ZookeeperRegistry extends AbstractRegistry {

    private ZooKeeper zooKeeper;
    public ZookeeperRegistry() throws IOException, InterruptedException {
        zooKeeper=ZookeeperUtil.createZooKeeper();
    }

    public ZookeeperRegistry(String connectString, int sessionTimeout) throws IOException, InterruptedException {
        zooKeeper=ZookeeperUtil.createZooKeeper(connectString, sessionTimeout);
    }

    @Override
    public void register(ServiceConfig<?> serviceConfig) throws InterruptedException, KeeperException {
        //服务名称的节点，是一个持久的节点
        String providerNode = Constant.BASE_PROVOKER_PATH;
        if (!ZookeeperUtil.exists(zooKeeper, providerNode, null)) {
            ZookeeperNode zookeeperNode = new ZookeeperNode(providerNode, null);
            ZookeeperUtil.createNode(zooKeeper, zookeeperNode, null , CreateMode.PERSISTENT);
        }

        String parentNode = Constant.BASE_PROVOKER_PATH + "/" + serviceConfig.getInterface().getName();
        if (!ZookeeperUtil.exists(zooKeeper, parentNode, null)) {
            ZookeeperNode zookeeperNode = new ZookeeperNode(parentNode, null);
            ZookeeperUtil.createNode(zooKeeper, zookeeperNode, null , CreateMode.PERSISTENT);
        }
        String node=parentNode+"/"+ NetUtils.getLocalIpAddress()+":"+Constant.PORT;

        if (!ZookeeperUtil.exists(zooKeeper, node, null)) {
            ZookeeperNode zookeeperNode = new ZookeeperNode(node, null);
            ZookeeperUtil.createNode(zooKeeper, zookeeperNode, null , CreateMode.EPHEMERAL);
        }
        if (log.isDebugEnabled()) {
            log.debug("当前工程发布了 {} 服务", serviceConfig.getInterface().getName());
        }
    }
}
