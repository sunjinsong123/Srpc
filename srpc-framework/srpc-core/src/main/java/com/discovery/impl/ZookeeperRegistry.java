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
import java.net.InetSocketAddress;
import java.time.chrono.AbstractChronology;
import java.util.List;

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
    @Override
    public InetSocketAddress lookup(String serviceName) throws InterruptedException, KeeperException {
        // 定义服务节点的完整ZooKeeper路径
        String serviceNodePath = Constant.BASE_PROVOKER_PATH + "/" + serviceName;

        // 获取服务节点下的所有子节点，这里没有使用Watcher
        List<String> children = ZookeeperUtil.getChildren(zooKeeper, serviceNodePath, null);

        if (children == null || children.isEmpty()) {
            // 如果子节点列表为空，则记录警告并返回null
            if (log.isWarnEnabled()) {
                log.warn("未找到服务 {} 的任何可用地址", serviceName);
            }
            return null;
        }
        //TODO 负载均衡策略    本地缓存+  watcher机制
        //我们每次调用相关方法的时候都需要去注册中心拉取服务列表
        //我们如何合理选择一个可用的服务，而不是只获取第一个
        // 取第一个子节点作为服务地址（建议在生产环境中使用更复杂的负载均衡逻辑）
        String firstChild = children.get(0);
        String[] ipAndPort = firstChild.split(":");
        if (ipAndPort.length < 2) {
            // 如果解析错误，则抛出具体的运行时异常
            throw new RuntimeException("服务地址格式错误: " + firstChild);
        }
        String ip = ipAndPort[0];
        int port = Integer.parseInt(ipAndPort[1]);

        // 记录获取到的IP和端口
        if (log.isDebugEnabled()) {
            log.debug("从ZooKeeper中获取到了服务 {} 的IP和端口号：{}", serviceName, firstChild);
        }

        return new InetSocketAddress(ip, port);
    }



}
