package com.sunjinsong;
import com.discovery.RegistryConfig;
import com.srpc.HelloSrpc; // 导入远程服务接口
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsumerApplication {

    public static void main(String[] args) throws Exception {

        // 想尽一切办法获取代理对象
        ReferenceConfig<HelloSrpc> referenceConfig = new ReferenceConfig<>();
        // 设置远程服务接口，HelloSrpc是一个服务接口，定义了远程调用的方法
        referenceConfig.setInterfaceRef(HelloSrpc.class);
        // 代理做了些什么:
        // 1.连接注册中心：通过设置的注册中心地址连接注册中心，用于发现服务
        // 2.拉取服务列表：从注册中心获取所有可用的服务列表
        // 3.根据负载均衡策略选择一个服务：从服务列表中选择一个合适的服务提供者，依据设定的负载均衡策略（如轮询、随机等）
        // 4.发送请求，携带一些信息（接口名、参数列表、方法的名字）：构建服务请求，包括接口名称、调用方法和传递的参数，然后通过网络发送到选定的服务端

        // 以下代码启动客户端应用，配置应用名、注册中心信息并获取服务引用，最终启动服务
        SrpcBootstrap.getInstance()
                .application("first-srpc-consumer") // 配置应用名称，具体名称根据实际应用修改
                .register(new RegistryConfig("zookeeper://127.0.0.1:2181")) // 设置注册中心地址，需要根据实际使用填写
                .reference(referenceConfig);
            // 获取服务的引用，继续配置服务的相关参数
        HelloSrpc helloSrpc = referenceConfig.get();
        String result= helloSrpc.sayHello("sunjinsong");

        log.info("result:{}",result);
    }





}
