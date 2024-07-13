package com.sunjinsong;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

/*
 * 提供一个NettyBootstrapInitializer单例
 *   目的就是防止，创建多个对象存在堆里
 *   这个单例构造模式是懒汉
 * TODO:这里的扩张  ，会有一些问题
 * */
@Slf4j
public class NettyBootstrapInitializer {
    private  static Bootstrap bootstrap=new Bootstrap();
    private final static NettyBootstrapInitializer nettyBootstrapInitializer = new NettyBootstrapInitializer();


    static {
        NioEventLoopGroup group = new NioEventLoopGroup();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        // 初始化通道，设置编解码器和业务处理器
                        ch.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {

                            @Override
                            protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
                                     log.info("receive response: {}", byteBuf.toString(CharsetUtil.UTF_8));
                            }
                        });
                    }
                });

    }

    private NettyBootstrapInitializer() {
    }

    public static Bootstrap getInstance() {
        // 使用Netty进行网络通信，异步发送请求并接收响应
        ;

        return bootstrap;
    }
}
