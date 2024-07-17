package com.channelhandler.channel;

import com.fasterxml.jackson.core.util.RequestPayload;
import com.sunjinsong.ServiceConfig;
import com.sunjinsong.SrpcBootstrap;
import com.transport.message.SrpcRequest;
import com.transport.message.SrpcRequestPayload;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
@Slf4j
public class MethodCallHandler extends SimpleChannelInboundHandler<SrpcRequest> {


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, SrpcRequest srpcRequest) throws Exception {
            //获取负载内容
        SrpcRequestPayload payload = srpcRequest.getPayload();
//2.根据负载内容进行方法调用
        Object object = callTargetMethod(payload);

        //3.封装响应
        log.info("调用方法成功:"+object);

        //4.写出响应
        channelHandlerContext.writeAndFlush(object);
    }

    private Object callTargetMethod(SrpcRequestPayload payload) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String interfaceName = payload.getInterfaceName();
        String methodName = payload.getMethodName();
        Class<?>[] parameterTypes = payload.getParameterTypes();
        Object[] parameters = payload.getParameters();
        //寻找到匹配的暴露出去的具体的实现
        ServiceConfig<?> serviceConfig = SrpcBootstrap.SERVICES_LIST.get(interfaceName);

        Object refImpl = serviceConfig.getRef();
        //通过反射调用  1.获取方法对象  2.执行invoke
        Class<?> clazz = refImpl.getClass();
        Method method = clazz.getMethod(methodName, parameterTypes);
        return method.invoke(refImpl, parameters);
    }
}
