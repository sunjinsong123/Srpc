package com.sunjinsong;

import com.discovery.Registry;
import com.proxy.handler.RpcConsumerInvocationHandler;
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

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReferenceConfig<T> {

    private Registry registry; // 服务注册中心
    private Class<T> interfaceRef; // 服务接口类

    public T get() throws Exception {
        if (registry == null) {
            log.error("Registry未初始化，无法使用代理。");
            throw new IllegalStateException("Registry未初始化，无法使用代理。");
        }

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class<?>[] interfaces = new Class<?>[]{interfaceRef};
        RpcConsumerInvocationHandler invocationHandler = new RpcConsumerInvocationHandler(interfaceRef, registry);
        @SuppressWarnings("unchecked")
        T proxyInstance = (T) Proxy.newProxyInstance(classLoader, interfaces, invocationHandler);
        return proxyInstance;
    }


}
