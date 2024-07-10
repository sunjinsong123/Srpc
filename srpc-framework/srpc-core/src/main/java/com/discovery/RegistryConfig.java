package com.discovery;

import com.discovery.Registry;
import com.discovery.impl.NacosrRegistry;
import com.discovery.impl.ZookeeperRegistry;
import com.sunjinsong.common.Constant;

import java.io.IOException;

/**
 * 配置注册中心，并根据连接字符串获取相应的注册中心实例。
 */
public class RegistryConfig {
    private String connectString;
    private String[] parts;

    /**
     * 构造函数，初始化连接字符串。
     *
     * @param connectString 连接字符串，格式应为 protocol://host:port
     */
    public RegistryConfig(String connectString) {
        if (connectString == null || connectString.isEmpty()) {
            throw new IllegalArgumentException("连接字符串不能为空");
        }
        this.connectString = connectString;
        this.parts = connectString.split("://");
        if (parts.length < 2) {
            throw new IllegalArgumentException("连接字符串格式错误: " + connectString);
        }
    }

    /**
     * 获取注册中心的实例。
     *
     * @return 返回对应的注册中心实例。
     * @throws IOException 如果发生IO异常。
     * @throws InterruptedException 如果线程被中断。
     */
    public Registry getRegistry() throws IOException, InterruptedException {
        String registryType = getRegistryType(true);
        if ("zookeeper".equals(registryType)) {
            return new ZookeeperRegistry(parts[1], Constant.TIME_OUT);
        } else if ("nacos".equals(registryType)) {
            String port = getRegistryType(false);
            return new NacosrRegistry(port, Constant.TIME_OUT);
        }
        throw new UnsupportedOperationException("不支持的注册中心类型: " + registryType);
    }

    /**
     * 根据连接字符串解析注册中心的类型或端口。
     *
     * @param returnProtocol 如果为true，返回协议类型；如果为false，返回端口号。
     * @return 返回协议类型或端口号。
     */
    private String getRegistryType(boolean returnProtocol) {
        if (returnProtocol) {
            return parts[0];
        } else {
            String[] addressParts = parts[1].split(":");
            if (addressParts.length < 2) {
                throw new IllegalArgumentException("连接字符串中缺少端口信息: " + connectString);
            }
            return addressParts[addressParts.length - 1];
        }
    }
}
