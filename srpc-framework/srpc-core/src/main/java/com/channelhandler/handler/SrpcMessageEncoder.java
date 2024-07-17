package com.channelhandler.handler;

import ch.qos.logback.core.rolling.helper.Compressor;
import com.Srpcenum.RequestType;
import com.caucho.hessian.io.Serializer;
import com.sunjinsong.common.MessageFormConstant;
import com.transport.message.SrpcRequest;
import com.transport.message.SrpcRequestPayload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * SrpcMessageEncoder 是一个Netty的消息编码器，负责将 SrpcRequest 对象转换为字节流以进行网络传输。
 * 继承自 Netty 的 MessageToMessageEncoder 类，专门处理出站消息。
 */
public class SrpcMessageEncoder extends MessageToMessageEncoder<SrpcRequest> {
    private static final Logger logger = LoggerFactory.getLogger(SrpcMessageEncoder.class);

    /**
     * 对 SrpcRequest 对象进行编码，将其转换为可以通过网络传输的 ByteBuf。
     *
     * @param ctx 通道处理器的上下文信息，用于操作管道和通道。
     * @param srpcRequest 待编码的请求对象。
     * @param out 存储编码后的数据的列表，Netty 会继续处理这些数据。
     * @throws Exception 如果编码过程中出现错误，会抛出异常。
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, SrpcRequest srpcRequest, List<Object> out) throws Exception {
        ByteBuf byteBuf = ctx.alloc().buffer(); // 从Netty的内存池中分配ByteBuf。
        byteBuf.writeBytes(MessageFormConstant.MAGIC); // 写入魔数
        byteBuf.writeByte(MessageFormConstant.VERSION); // 写入版本号
        byteBuf.writeShort(MessageFormConstant.HEADER_LENGTH); // 写入头部长度

        // 先占位，待确定body的实际长度后再填写
        int lengthIndex = byteBuf.writerIndex();
        byteBuf.writerIndex(lengthIndex + MessageFormConstant.FULL_FIELD_LENGTH);

        // 写入请求的类型、序列化类型和压缩类型
        byteBuf.writeByte(srpcRequest.getRequestType());
        byteBuf.writeByte(srpcRequest.getSerializeType());
        byteBuf.writeByte(srpcRequest.getCompressType());

        // 写入请求ID和时间戳
        byteBuf.writeLong(srpcRequest.getRequestId());
        byteBuf.writeLong(srpcRequest.getTimeStamp());
        //如果是心跳请求，就不处理请求体
        // 序列化和压缩请求体
        byte[] body = getBodyBytes(srpcRequest.getPayload());

        // 写入请求体数据
        if (body != null) {
            byteBuf.writeBytes(body);
        }
        int bodyLength = (body != null) ? body.length : 0;

        // 设置消息的总长度
        int writerIndex = byteBuf.writerIndex();
        byteBuf.writerIndex(lengthIndex);
        byteBuf.writeInt(MessageFormConstant.HEADER_LENGTH + bodyLength);
        byteBuf.writerIndex(writerIndex);

        // 将编码完成的ByteBuf添加到输出列表中，以供后续Netty处理
        out.add(byteBuf);

        if (logger.isDebugEnabled()) {
            logger.debug("请求{}已经完成报文的编码。", srpcRequest.getRequestId());
        }

    }

    /**
     * 序列化和压缩请求体
     *
     * @param srpcRequest 请求对象
     * @return 压缩和序列化后的字节数组
     * @throws Exception 如果序列化或压缩失败
     */
//    private byte[] serializeAndCompress(SrpcRequest srpcRequest) throws Exception {
//        if (srpcRequest.getPayload() == null) {
//            return null;
//        }
//
//        // 使用工厂模式获得序列化器和压缩器
////        Serializer serializer = SerializerFactory.getSerializer(srpcRequest.getSerializeType()).getImpl();
////
////        Compressor compressor = CompressorFactory.getCompressor(srpcRequest.getCompressType()).getImpl();
//
//        byte[] serializedData = serializer.serialize(srpcRequest.getPayload());
//        return compressor.compress(serializedData);
//    }

    /**
     * 辅助方法，用于序列化 SrpcRequest 的负载部分。
     * @param srpcRequestPayload 请求负载对象。
     * @return 序列化后的字节数组。
     * @throws IOException 如果序列化过程中发生错误，则抛出 IOException。
     */
    private byte[] getBodyBytes(SrpcRequestPayload srpcRequestPayload) throws IOException {
        // 使用 ByteArrayOutputStream 和 ObjectOutputStream 进行Java对象的序列化
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(srpcRequestPayload); // 序列化对象
            objectOutputStream.flush();
            byte[] serializedBytes = byteArrayOutputStream.toByteArray(); // 获取序列化后的字节数组
            logger.info("成功序列化SrpcRequestPayload, 字节流: {}", byteArrayToHex(serializedBytes));
            return serializedBytes;
        } catch (Exception e) {
            logger.error("序列化SrpcRequestPayload失败", e); // 记录错误日志
            throw new IOException("序列化SrpcRequestPayload失败", e); // 抛出异常
        }
    }

    /**
     * 将字节数组转换为十六进制字符串。
     * @param bytes 要转换的字节数组。
     * @return 十六进制字符串。
     */
    private String byteArrayToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }

}
