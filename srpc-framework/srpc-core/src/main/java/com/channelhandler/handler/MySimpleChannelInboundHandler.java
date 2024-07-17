package com.channelhandler.handler;

import com.sunjinsong.SrpcBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
@Slf4j
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<ByteBuf> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
        String response = byteBuf.toString(CharsetUtil.UTF_8);
        log.info("receive response: {}", byteBuf.toString(CharsetUtil.UTF_8));
        CompletableFuture future =  SrpcBootstrap.PENDING_REQUEST.get(1L);
        future.complete(response);
    }
}
