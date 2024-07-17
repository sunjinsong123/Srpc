package com.srpc.impl;

import com.srpc.HelloSrpc;
import com.discovery.RegistryConfig;
import com.sunjinsong.ProtocalConfig;
import com.sunjinsong.ServiceConfig;
import com.sunjinsong.SrpcBootstrap;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;

/**
 * 主应用程序入口类，用于配置和启动SRPC服务提供者。
 */
@Slf4j
public class ProviderApplication {
    public static void main(String[] args) {
        try {
            log.info("开始配置服务...");
            ServiceConfig<HelloSrpc> serviceConfig = new ServiceConfig<>();
            serviceConfig.setInterface(HelloSrpc.class);
            serviceConfig.setRef(new HelloImpl());
            log.info("服务配置完成。");

            log.info("配置SrpcBootstrap...");
            SrpcBootstrap srpcBootstrap = SrpcBootstrap.getInstance();
            srpcBootstrap.application("first-srpc-provider")
                    .register(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                    .protocal(new ProtocalConfig("jdk"))
                    .publish(serviceConfig);
            log.info("SrpcBootstrap配置完成。");

            log.info("启动服务...");
            srpcBootstrap.start();
            log.info("服务已启动。");
        } catch (IOException | InterruptedException | KeeperException e) {
            log.error("启动SRPC服务过程中发生错误：", e);
        }
    }
}
