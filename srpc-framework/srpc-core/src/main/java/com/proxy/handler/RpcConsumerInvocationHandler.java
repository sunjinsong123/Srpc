package com.proxy.handler;

import com.discovery.Registry;
import com.sunjinsong.NettyBootstrapInitializer;
import com.sunjinsong.SrpcBootstrap;
import com.sunjinsong.common.SrpcRequestConstant;
import com.transport.message.SrpcRequest;
import com.transport.message.SrpcRequestPayload;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 该类实现了InvocationHandler接口，封装了客户端的RPC调用逻辑。
 * 它包括服务的查找、连接重连以及远程方法的调用等功能。
 */
@Slf4j
public class RpcConsumerInvocationHandler implements InvocationHandler {

    private static final long DEFAULT_TIMEOUT = 10L; // 设置默认的超时时间为10秒

    private Class<?> interfaceRef; // 服务接口类的Class对象
    private Registry registry; // 服务注册中心

    // 构造函数，注入服务接口和注册中心
    public RpcConsumerInvocationHandler(Class<?> interfaceRef, Registry registry) {
        this.interfaceRef = interfaceRef;
        this.registry = registry;
    }

    /**
     * 处理代理实例上方法的调用。
     *
     * @param proxy 代理类实例
     * @param method 被调用的方法
     * @param args 方法参数
     * @return 方法调用的结果
     * @throws Throwable 如果发生异常
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 记录日志：正在调用的方法
        log.info("正在调用服务：{}", method.getName());
        // 记录日志：正在查找的服务
        log.info("正在查找服务：{}", interfaceRef.getName());
        // 通过注册中心查找服务地址
        InetSocketAddress address = registry.lookup(interfaceRef.getName());
        log.info("已找到服务地址：{}", address);
        // 获取缓存中的Channel，如果不活跃则重连
        Channel channel = SrpcBootstrap.CHANNEL_CACHE.get(address);
        if (channel == null || !channel.isActive()) {
            channel = reconnect(address);
        }
        SrpcRequestPayload requestPayload = SrpcRequestPayload.builder()
                .interfaceName(interfaceRef.getName())
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .parameters(args)
                .returnType(method.getReturnType())
                .build();
        SrpcRequest request = SrpcRequest.builder()
                .requestId(1L)
                .requestType(SrpcRequestConstant.REQUEST_TYPE_REQUEST)
                .serializeType(SrpcRequestConstant.SERIALIZE_TYPE_PROTOBUF)
                .compressType(SrpcRequestConstant.COMPRESS_TYPE_NONE)
                .payload(requestPayload)
                .build();
        // 发送请求并获取异步结果
        CompletableFuture<Object> futureResponse = sendRequest(channel, method, args, request);
        SrpcBootstrap.PENDING_REQUEST.put(1L, futureResponse);
        // 阻塞等待结果，直到超时
        return futureResponse.get(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
    }

    // 处理连接重连
    private Channel reconnect(InetSocketAddress address) throws InterruptedException {
        Bootstrap bootstrap = NettyBootstrapInitializer.getInstance();
        ChannelFuture connectFuture = bootstrap.connect(address).sync();
        if (connectFuture.isSuccess()) {
            Channel newChannel = connectFuture.channel();
            SrpcBootstrap.CHANNEL_CACHE.put(address, newChannel);
            log.info("新的Channel已经创建并缓存");
            return newChannel;
        } else {
            throw new RuntimeException("连接失败", connectFuture.cause());
        }
    }

    // 发送RPC请求
    private CompletableFuture<Object> sendRequest(Channel channel, Method method, Object[] args,Object message) {
        CompletableFuture<Object> responseFuture = new CompletableFuture<>();
        // 发送请求数据
        ChannelFuture writeFuture = channel.writeAndFlush(message);
        writeFuture.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("请求已成功发送");
                responseFuture.complete(message);
            } else {
                log.error("请求发送失败", future.cause());
                responseFuture.completeExceptionally(future.cause());
            }
        });
        return responseFuture;
    }
}
