package com.sunjinsong;

import com.channelhandler.channel.MethodCallHandler;
import com.channelhandler.handler.SrpcMessageDecoder;
import com.discovery.Registry;
import com.discovery.RegistryConfig;
import com.discovery.impl.ZookeeperRegistry;
import com.sunjinsong.common.Constant;
import com.sunjinsong.utils.zookeeper.ZookeeperUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;


/**
 * SrpcBootstrap类用于启动、停止和管理RPC服务。
 * 它实现了单例模式以确保全局只有一个实例。
 */
@Slf4j
public class SrpcBootstrap {
    // 单例实例
    private static volatile SrpcBootstrap instance;
    private String applicationName = "default";

    //维护一个已经发布的服务，放在缓存中，防止频繁访问注册中心
    public static  Map<String, ServiceConfig<?>> SERVICES_LIST = new ConcurrentHashMap<>();
    //定义全局的对外挂起的请求
    public static final Map<Long, CompletableFuture<Object>> PENDING_REQUEST = new ConcurrentHashMap<>();

    private Registry registry= new ZookeeperRegistry();
    private RegistryConfig registryConfig;

    private ProtocalConfig protocalConfig;

    private int port=8080;
    private ZooKeeper zooKeeper;

    //netty连接的缓存，如果使用InetSocketAddress这个类型的key。一定要看他有没有重写tostring和 equals方法
    public final  static Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>();

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
        EventLoopGroup bossGroup = new NioEventLoopGroup(2);
        EventLoopGroup workerGroup = new NioEventLoopGroup(10);

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    .addLast(new LoggingHandler())
                                    .addLast(new SrpcMessageDecoder())
                                    .addLast(new MethodCallHandler());

                        }
                    });

            ChannelFuture f = serverBootstrap.bind(Constant.PRVOIDER_PORT).sync();
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

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
      SERVICES_LIST.put(service.getInterface().getName(), service);

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
//     * @param referenceConfig 引用配置
     * @return 返回SrpcBootstrap实例以支持链式调用。
     */
    public SrpcBootstrap  reference(ReferenceConfig<?> reference) {
        /*
        * 在这个方法里我们是否可以拿到相关的配置项-注册中心
        * 配置reference，将来调用get方法时，方便生成代理对象
        * 1.reference需要一个注册中心
        *
        *
        *
        * */
        reference.setRegistry(registry);
        // 设置服务引用的逻辑
        return this;
    }


}


