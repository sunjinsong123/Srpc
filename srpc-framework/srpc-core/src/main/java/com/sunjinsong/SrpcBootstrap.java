package com.sunjinsong;

import com.discovery.Registry;
import com.discovery.RegistryConfig;
import com.discovery.impl.ZookeeperRegistry;
import com.sunjinsong.utils.zookeeper.ZookeeperUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.List;


/**
 * SrpcBootstrap类用于启动、停止和管理RPC服务。
 * 它实现了单例模式以确保全局只有一个实例。
 */
@Slf4j
public class SrpcBootstrap {
    // 单例实例
    private static volatile SrpcBootstrap instance;
    private String applicationName = "default";

    private Registry registry= new ZookeeperRegistry();
    private RegistryConfig registryConfig;

    private ProtocalConfig protocalConfig;

    private int port=8080;
    private ZooKeeper zooKeeper;

    /**
     * 私有构造函数，避免外部直接创建实例。
     */
    private SrpcBootstrap() throws IOException, InterruptedException {
        zooKeeper = ZookeeperUtil.createZooKeeper();
        // 构造方法逻辑
    }




    /**
     * 获取SrpcBootstrap类的单例实例。
     * 如果实例不存在，则创建一个新的实例。
     *
     * @return 返回SrpcBootstrap的单例实例。
     */
    public static SrpcBootstrap getInstance() throws IOException, InterruptedException {
        if (instance == null) {
            synchronized (SrpcBootstrap.class) {
                if (instance == null) {
                    instance = new SrpcBootstrap();
                }
            }
        }
        return instance;
    }

    /**
     * 启动RPC服务。
     */
    public SrpcBootstrap start() {
        //睡眠5秒
        try {
            Thread.sleep(25000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        log.debug("开始了");

        // 启动服务的逻辑
        return this;
    }


    /**
     * 注册服务到注册中心。
     *
     * @param registryConfig 注册中心配置
     */
    public SrpcBootstrap register(RegistryConfig registryConfig) throws IOException, InterruptedException {


        this.registry = registryConfig.getRegistry();
        // 注册服务的逻辑
        return this;
    }

    /**
     * 发布单个服务。
     *
     * @param service 服务配置
     * @return 返回 SrpcBootstrap 实例以支持链式调用
     */
    public SrpcBootstrap publish(ServiceConfig<?> service) throws InterruptedException, KeeperException, IOException {
        //抽象了注册中心的概念，使用注册中心的一个实现完成了注册


      registry.register(service);

        // 发布服务的逻辑
        return this;
    }

    /**
     * 发布多个服务。
     *
     * @param services 服务列表
     * @return 返回 SrpcBootstrap 实例以支持链式调用
     */
    public SrpcBootstrap publish(List<ServiceConfig<?>> services) {
        if (log.isDebugEnabled()) {
            for (ServiceConfig<?> service : services) {
                log.debug("当前工程发布了 {} 服务", service.getInterface().getName());
            }
        }

        // 发布服务的逻辑
        return this; // 支持链式调用
    }


    /**
     * 设置应用名称。
     *
     * @param application 应用名
     * @return 返回SrpcBootstrap实例以支持链式调用。
     */
    public SrpcBootstrap application(String application) {
        this.applicationName = application;
        if (log.isDebugEnabled()) {
            log.debug("应用的名称为:" + application);
        }
        // 设置应用名的逻辑
        return this;
    }


    /**
     * 设置通信协议配置。
     * 此方法接收一个 ProtocalConfig 对象，并设置当前 SrpcBootstrap 实例的通信协议。
     * 如果开启了调试模式，会输出当前设置的协议信息。
     *
     * @param protocalConfig 协议配置对象
     * @return 返回当前 SrpcBootstrap 实例以支持链式调用
     */
    /**
     * 设置通信协议配置。
     * 此方法接收一个 ProtocalConfig 对象，并设置当前 SrpcBootstrap 实例的通信协议。
     * 如果开启了调试模式，会输出当前设置的协议信息。
     *
     * @param protocalConfig 协议配置对象
     * @return 返回当前 SrpcBootstrap 实例以支持链式调用
     */
    public SrpcBootstrap protocal(ProtocalConfig protocalConfig) {
        this.protocalConfig = protocalConfig;

        if (protocalConfig == null) {
            throw new IllegalArgumentException("ProtocalConfig must not be null");
        }
        // 设置通信协议的逻辑
        if (log.isDebugEnabled()) {
            log.debug("当前工程使用了 {} 协议", protocalConfig);
        }
        return this;
    }


    /**
     * 设置服务引用配置。
     *
     * @param referenceConfig 引用配置
     * @return 返回SrpcBootstrap实例以支持链式调用。
     */
    public SrpcBootstrap reference(ReferenceConfig<?> referenceConfig) {
        // 设置服务引用的逻辑
        return this;
    }


}


