package com.srpc.impl;

import com.srpc.HelloSrpc;
//import com.sunjinsong.GreetingsService; // 未使用的import，应该去除
import com.sunjinsong.ProtocalConfig;
import com.sunjinsong.RegistryConfig;
import com.sunjinsong.ServiceConfig;
import com.sunjinsong.SrpcBootstrap;
import lombok.extern.slf4j.Slf4j;

/**
 * 主应用程序入口类，用于配置和启动SRPC服务提供者。
 */
@Slf4j
public class Application {
    public static void main(String[] args) {
        // 打印日志，表示服务配置开始
        log.info("开始配置服务...");
        // 创建服务配置对象，并设置服务接口和实现
        ServiceConfig<HelloSrpc> serviceConfig = new ServiceConfig<>();
        serviceConfig.setInterface(HelloSrpc.class); // 设置服务接口类
        serviceConfig.setRef(new HelloImpl()); // 设置服务的具体实现
        // 打印日志，表示服务配置完成
        log.info("服务配置完成。");

        // 打印日志，表示SrpcBootstrap配置开始
        log.info("配置SrpcBootstrap...");
        // 获取SrpcBootstrap实例，并进行配置
        SrpcBootstrap.getInstance()
                .applicationName("first-srpc-provider") // 设置应用名称
                .register(new RegistryConfig("zookeeper://127.0.0.1:2181")) // 注册服务到Zookeeper注册中心
                .protocal(new ProtocalConfig("jdk")) // 设置通信协议为JDK自带序列化
                .publish(serviceConfig); // 发布配置的服务
        // 打印日志，表示SrpcBootstrap配置完成
        log.info("SrpcBootstrap配置完成。");

        // 打印日志，表示服务启动过程开始
        log.info("启动服务...");
        SrpcBootstrap.getInstance().start(); // 启动服务
        // 打印日志，表示服务已成功启动
        log.info("服务已启动。");
    }
}
