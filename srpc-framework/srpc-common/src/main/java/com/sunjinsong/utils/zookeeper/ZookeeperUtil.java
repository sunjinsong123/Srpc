package com.sunjinsong.utils.zookeeper;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class ZookeeperUtil {
    private static final String DEFAULT_CONNECT_STRING = "127.0.0.1:2181";
    private static final int DEFAULT_SESSION_TIMEOUT = 5000;

    // 创建ZooKeeper实例
    public static ZooKeeper createZooKeeper() throws IOException, InterruptedException {
        return createZooKeeper(DEFAULT_CONNECT_STRING, DEFAULT_SESSION_TIMEOUT);
    }

    // 支持自定义连接字符串和会话超时的ZooKeeper实例创建
    public static ZooKeeper createZooKeeper(String connectString, int sessionTimeout) throws IOException, InterruptedException {
        CountDownLatch connectedSignal = new CountDownLatch(1);
        ZooKeeper zooKeeper = new ZooKeeper(connectString, sessionTimeout, event -> {
            if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                log.info("已成功建立ZooKeeper连接，连接字符串：{}，会话超时：{}", connectString, sessionTimeout);
                connectedSignal.countDown();
            }
        });

        try {
            connectedSignal.await();
            log.info("ZooKeeper客户端已准备就绪");
        } catch (InterruptedException e) {
            log.error("ZooKeeper连接中断：{}", e.getMessage());
            Thread.currentThread().interrupt(); // 重新设置中断状态
            throw e;
        }

        return zooKeeper;
    }

    // 创建节点
    public static Boolean createNode(ZooKeeper zooKeeper, ZookeeperNode zookeeperNode, Watcher watcher, CreateMode persistent) throws InterruptedException, KeeperException {
        try {
            if (zooKeeper.exists(zookeeperNode.getNodepath(), watcher) == null) {
                List<ACL> acls = ZooDefs.Ids.OPEN_ACL_UNSAFE;
                zooKeeper.create(zookeeperNode.getNodepath(), zookeeperNode.getDate(), acls, persistent);
                log.info("节点创建成功：{}", zookeeperNode.getNodepath());
                return true;
            } else {
                log.info("节点已存在，无法创建：{}", zookeeperNode.getNodepath());
                return false;
            }
        } catch (KeeperException e) {
            log.error("创建节点失败：{}，原因：{}", zookeeperNode.getNodepath(), e.getMessage());
            throw e;
        }
    }

    public static void close(ZooKeeper zooKeeper){
        try {
            zooKeeper.close();
            log.info("ZooKeeper连接已关闭");
        } catch (InterruptedException e) {
            log.error("关闭ZooKeeper连接失败：{}", e.getMessage());
            Thread.currentThread().interrupt(); // 重新设置中断状态
        }
    }

    /**
     * 检查指定路径的节点是否存在于ZooKeeper中。
     *
     * @param zooKeeper ZooKeeper的客户端实例
     * @param path 需要检查的节点路径
     * @param watcher 可选的Watcher实例，如果指定，当节点的状态变化时会接收通知
     * @return 如果节点存在返回true，否则返回false
     * @throws InterruptedException 如果线程被中断
     * @throws KeeperException 如果ZooKeeper服务报告错误
     */
    public static boolean exists(ZooKeeper zooKeeper, String path, Watcher watcher) throws InterruptedException, KeeperException {
        try {
            // 调用ZooKeeper的exists方法检查节点是否存在，如果返回值不为null，则节点存在
            return zooKeeper.exists(path, watcher) != null;
        } catch (KeeperException | InterruptedException e) {
            // 记录错误日志
            log.error("检查节点存在性时发生错误：路径={}，错误={}", path, e.getMessage());
            // 中断异常应该重新设置中断状态，并且向上抛出，让调用者处理中断情况
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            // 将捕获的异常包装成运行时异常抛出，简化调用者的错误处理需求
            throw new RuntimeException("检查节点存在性失败：路径=" + path, e);
        }
    }
}
