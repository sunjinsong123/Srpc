package com.sunjinsong;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * ReferenceConfig类用于管理服务接口的配置。
 * 它提供了获取和设置服务接口类的方法，并可以创建该接口的动态代理实例。
 *
 * @param <T> 服务接口的类型参数。
 */
public class ReferenceConfig<T> {
    // 接口引用的私有成员变量
    private Class<T> interfaceRef;

    /**
     * 获取当前配置的服务接口类。
     * @return 返回服务接口的Class类型。
     */
    public Class<T> getInterface() {
        return interfaceRef;
    }

    /**
     * 设置服务接口类。
     * @param interfaceRef 要设置的服务接口Class。
     */
    public void setInterface(Class<T> interfaceRef) {
        this.interfaceRef = interfaceRef;
    }

    /**
     * 使用动态代理生成并返回服务接口的代理实例。
     * 此代理在调用任何方法时会打印 "hello proxy"。
     *
     * @return 代理生成的服务接口实例。
     */
    public T get() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class<?>[] classes = new Class<?>[] { interfaceRef };

        // 创建动态代理实例
        @SuppressWarnings("unchecked")
        T proxyInstance = (T) Proxy.newProxyInstance(
                classLoader,
                classes,
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        System.out.println("hello proxy");
                        return null; // 根据需要调整返回值
                    }
                });

        return proxyInstance;
    }
}
