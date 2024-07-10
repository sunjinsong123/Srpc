package com.sunjinsong;

import com.sunjinsong.utils.zookeeper.ZookeeperNode;
import com.sunjinsong.utils.zookeeper.ZookeeperUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

@Slf4j
public class Application {
    private static final String BASE_PATH = "/srpc-sunjinsong";

    public static void main(String[] args) {
        try {
            // 创建与 ZooKeeper 的连接
            log.info("尝试连接到ZooKeeper服务");
            ZooKeeper zooKeeper = ZookeeperUtil.createZooKeeper();

            // 创建基础节点和其子节点
            createNodes(zooKeeper);

            // 可以在这里继续编写后续逻辑，例如订阅节点的变化等

            // 关闭ZooKeeper连接
            ZookeeperUtil.close(zooKeeper);
            log.info("ZooKeeper连接已关闭");
        } catch (IOException | InterruptedException | KeeperException e) {
            log.error("处理ZooKeeper时发生错误：", e);
        }
    }

    private static void createNodes(ZooKeeper zooKeeper) throws InterruptedException, KeeperException {
        // 创建基础节点
        log.info("正在创建基础节点：{}", BASE_PATH);
        ZookeeperNode baseNode = new ZookeeperNode(BASE_PATH, null);
        if (ZookeeperUtil.createNode(zooKeeper, baseNode, null, CreateMode.PERSISTENT)) {
            log.info("基础节点创建成功");
        } else {
            log.warn("基础节点已存在，未重新创建");
        }

        // 创建providers子节点
        String providersPath = BASE_PATH + "/providers";
        log.info("正在创建子节点：{}", providersPath);
        ZookeeperNode providersNode = new ZookeeperNode(providersPath, null);
        if (ZookeeperUtil.createNode(zooKeeper, providersNode, null, CreateMode.PERSISTENT)) {
            log.info("Providers子节点创建成功");
        } else {
            log.warn("Providers子节点已存在，未重新创建");
        }

        // 创建consumers子节点
        String consumersPath = BASE_PATH + "/consumers";
        log.info("正在创建子节点：{}", consumersPath);
        ZookeeperNode consumersNode = new ZookeeperNode(consumersPath, null);
        if (ZookeeperUtil.createNode(zooKeeper, consumersNode, null, CreateMode.PERSISTENT)) {
            log.info("Consumers子节点创建成功");
        } else {
            log.warn("Consumers子节点已存在，未重新创建");
        }
    }
}
