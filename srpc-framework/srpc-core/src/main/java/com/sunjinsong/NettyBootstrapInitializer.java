package com.sunjinsong;

import com.channelhandler.ConsumerChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/*
 * 提供一个NettyBootstrapInitializer单例
 *   目的就是防止，创建多个对象存在堆里
 *   这个单例构造模式是懒汉
 * TODO:这里的扩张  ，会有一些问题
 * */
@Slf4j
public class NettyBootstrapInitializer {
    private static Bootstrap bootstrap = new Bootstrap();
    private final static NettyBootstrapInitializer nettyBootstrapInitializer = new NettyBootstrapInitializer();


    static {
        NioEventLoopGroup group = new NioEventLoopGroup();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ConsumerChannelInitializer());
    }

    private NettyBootstrapInitializer() {
    }

    public static Bootstrap getInstance() {
        // 使用Netty进行网络通信，异步发送请求并接收响应
        ;

        return bootstrap;
    }
}
