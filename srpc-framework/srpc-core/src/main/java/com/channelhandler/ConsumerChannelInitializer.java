package com.channelhandler;

import com.channelhandler.handler.MySimpleChannelInboundHandler;
import com.channelhandler.handler.SrpcMessageEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * ConsumerChannelInitializer 是一个特殊的初始化器，用于设置新连接的管道。
 * 它继承自 Netty 的 ChannelInitializer 类，该类用于在某个 Channel 注册到 EventLoop 后对其进行初始化。
 */
public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {

    /**
     * 在此方法中，将不同的处理器按照业务需求链式添加到管道（pipeline）中。
     * 每个处理器都会对入站或出站的数据进行处理。
     * @param socketChannel 代表服务器与客户端建立的一个连接通道。
     * @throws Exception 如果在初始化通道时出现异常。
     */
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline()
                // 添加日志处理器，用于打印关于所有事件的信息，如连接、数据接收等。
                // LoggingHandler 是一个内置的助手类，用于记录 I/O 事件，非常有用于调试。
                .addLast(new LoggingHandler(LogLevel.DEBUG))

                // 添加自定义的消息编码器，用于将业务对象 SrpcRequest 转换成字节流，
                // 以便可以通过网络发送到服务端。
                //出栈
                .addLast(new SrpcMessageEncoder())
                //入栈
                // 添加自定义的处理器，用于处理接收到的消息。
                // MySimpleChannelInboundHandler 是继承自 SimpleChannelInboundHandler 的处理器，
                // 它会在接收到数据时被调用，具体的业务逻辑应该在这个处理器中实现。
                .addLast(new MySimpleChannelInboundHandler());
    }
}
