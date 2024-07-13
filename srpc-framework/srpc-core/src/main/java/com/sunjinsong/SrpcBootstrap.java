package com.sunjinsong;

import com.discovery.Registry;
import com.discovery.RegistryConfig;
import com.discovery.impl.ZookeeperRegistry;
import com.sunjinsong.utils.zookeeper.ZookeeperUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        // 创建一个新的服务器端的EventLoopGroup来处理客户端事件

        //bossGroup只负责处理请求
        //worker负责具体的业务处理
        EventLoopGroup bossGroup = new NioEventLoopGroup(2);
        EventLoopGroup workerGroup = new NioEventLoopGroup(10);

        try {
            // 创建服务器端启动对象   需要一个服务器引导程序
            ServerBootstrap serverBootstrap  = new ServerBootstrap();

            // 设置使用的EventLoopGroup  配置服务器
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)  // 设置服务器通道类型
                    .option(ChannelOption.SO_BACKLOG, 100)  // 设置TCP连接的缓冲区等待队列长度
                    .handler(new LoggingHandler(LogLevel.INFO))  // 设置日志处理器
                    .childHandler(new ChannelInitializer<SocketChannel>() {  // 初始化处理器
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // 添加自定义处理器，例如编解码器和你的业务处理器
                            //核心是这里
                            ch.pipeline().addLast(new SimpleChannelInboundHandler<>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
                                    ByteBuf buf = (ByteBuf) msg;
                                    log.info("接收到客户端消息：" + buf.toString(Charset.defaultCharset()));
                                    channelHandlerContext.channel().writeAndFlush(Unpooled.copiedBuffer("收到".getBytes()));
                                    log.info("发送消息给客户端"+"收到");
                                }
                            });
                        }
                    });

            // 绑定端口并启动服务器
            ChannelFuture f = serverBootstrap.bind(8081).sync();

            // 等待服务器socket关闭
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 优雅关闭EventLoopGroup
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


