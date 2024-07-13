package com.sunjinsong;

import com.discovery.Registry;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * ReferenceConfig类用于管理服务接口的配置。
 * 它提供了获取和设置服务接口类的方法，并可以创建该接口的动态代理实例。
 *
 * @param <T> 服务接口的类型参数。
 */
@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReferenceConfig<T> {

    private Registry registry;  // 修正成员变量名以与代码保持一致
    private Class<T> interfaceRef;

    /**
     * 使用动态代理生成并返回服务接口的代理实例。
     * 此代理在调用任何方法时将自动处理服务的查找和远程调用。
     * 使用动态代理的好处包括解耦服务使用和服务实现，提高了代码的灵活性和可维护性。
     *
     * @param <T> 服务接口类型
     * @return 通过代理生成的服务接口实例
     * @throws IllegalStateException 如果服务注册中心未初始化
     */
    public <T> T get() throws IllegalStateException {
        // 检查服务注册中心是否已初始化
        if (registry == null) {
            log.error("Registry未初始化，无法使用代理。");
            throw new IllegalStateException("Registry未初始化，无法使用代理。");
        }

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class<?>[] interfaces = new Class<?>[]{interfaceRef};

        // 创建动态代理实例，所有方法调用都会被中转至InvocationHandler
        @SuppressWarnings("unchecked")
        T proxyInstance = (T) Proxy.newProxyInstance(
                classLoader,
                interfaces,
                (proxy, method, args) -> {
                    log.info("正在调用方法：{}，参数：{}", method.getName(), args);

                    try {
                        InetSocketAddress address = registry.lookup(interfaceRef.getName());
                        log.info("找到服务地址：{}", address);

                        // 从全局缓存中获取一个Channel
                        Channel channel = SrpcBootstrap.CHANNEL_CACHE.get(address);

                        if (channel == null || !channel.isActive()) {
                            log.info("Channel未缓存或已关闭，正在尝试新的连接");

                            Bootstrap bootstrap = NettyBootstrapInitializer.getInstance();
                            ChannelFuture connectFuture = bootstrap.connect(address).sync();

                            if (connectFuture.isSuccess()) {
                                channel = connectFuture.channel();
                                SrpcBootstrap.CHANNEL_CACHE.put(address, channel);
                                log.info("新的Channel已经创建并缓存");
                            } else {
                                throw new RuntimeException("连接失败", connectFuture.cause());
                            }
                        }

                        // 发送请求并等待响应
                        ChannelFuture future = channel.writeAndFlush(Unpooled.copiedBuffer("我是中国人".getBytes())); // 实际请求对象需要替换为合适的协议对象
                        future.sync(); // 等待操作完成

                        if (!future.isSuccess()) {
                            log.error("远程调用出错", future.cause());
                            throw new RuntimeException("远程调用失败", future.cause());
                        }

                        return null; // 实际中应从服务器获取并返回结果
                    } catch (Exception e) {
                        log.error("远程调用出错", e);
                        throw new RuntimeException("远程调用失败", e);
                    }
                });


        return proxyInstance;
    }

}
