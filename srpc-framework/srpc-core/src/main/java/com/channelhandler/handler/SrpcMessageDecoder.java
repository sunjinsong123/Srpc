package com.channelhandler.handler;


import com.Srpcenum.RequestType;
import com.transport.message.SrpcRequest;
import com.transport.message.SrpcRequestPayload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Random;

import static com.sunjinsong.common.MessageFormConstant.*;

/**
 * SrpcMessageDecoder 类用于处理从网络中接收到的SRPC消息帧，并进行解码。
 */
public class SrpcMessageDecoder extends LengthFieldBasedFrameDecoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(SrpcMessageDecoder.class);

    public SrpcMessageDecoder() {
        super(MAX_FRAME_LENGTH, // 最大帧的长度
                MAGIC.length + VERSION_LENGTH + HEADER_FIELD_LENGTH, // 长度字段的偏移量
                FULL_FIELD_LENGTH, // 长度字段的长度
                -(MAGIC.length + VERSION_LENGTH + HEADER_FIELD_LENGTH + FULL_FIELD_LENGTH), // 负载适配长度
                0); // 长度调整值
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        LOGGER.debug("Attempting to decode a frame");
        Thread.sleep(new Random().nextInt(50));

        Object decodedFrame = super.decode(ctx, in);
        if (decodedFrame instanceof ByteBuf byteBuf) {
            return decodeFrame(byteBuf);
        }
        return null;
    }

    /**
     * 对接收到的数据帧进行解码
     *
     * @param byteBuf 输入的ByteBuf数据
     * @return 解码后的SrpcRequest对象
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private Object decodeFrame(ByteBuf byteBuf) throws IOException, ClassNotFoundException {
        LOGGER.trace("Decoding the frame");

        // 解析魔数
        byte[] magic = new byte[MAGIC.length];
        byteBuf.readBytes(magic);
        for (int i = 0; i < magic.length; i++) {
            if (magic[i] != MAGIC[i]) {
                LOGGER.error("Invalid MAGIC number received");
                throw new RuntimeException("The request obtained is not legitimate.");
            }
        }

        // 解析版本号
        byte version = byteBuf.readByte();
        if (version > VERSION) {
            LOGGER.error("Unsupported version received: {}", version);
            throw new RuntimeException("获得的请求版本不被支持。");
        }

        // 解析头部长度和总长度
        short headLength = byteBuf.readShort();
        int fullLength = byteBuf.readInt();

        // 解析请求类型和序列化类型
        byte requestType = byteBuf.readByte();
        byte serializeType = byteBuf.readByte();
        byte compressType = byteBuf.readByte();
        long requestId = byteBuf.readLong();
        long timeStamp = byteBuf.readLong();

        SrpcRequest srpcRequest = new SrpcRequest();
        srpcRequest.setRequestType(requestType);
        srpcRequest.setCompressType(compressType);
        srpcRequest.setSerializeType(serializeType);
        srpcRequest.setRequestId(requestId);
        srpcRequest.setTimeStamp(timeStamp);

        // 对心跳请求进行处理
        if (requestType == RequestType.HEART_BEAT.getId()) {
            LOGGER.debug("Heartbeat request received");
            return srpcRequest;
        }

        int payloadLength = fullLength - headLength;
        byte[] payload = new byte[payloadLength];
        byteBuf.readBytes(payload);

        ByteArrayInputStream bis = new ByteArrayInputStream(payload);
        ObjectInputStream ois = new ObjectInputStream(bis);
        SrpcRequestPayload requestPayload = (SrpcRequestPayload) ois.readObject();
        srpcRequest.setPayload(requestPayload);

        LOGGER.debug("Successfully decoded SRPC Request: {}", srpcRequest);
        return srpcRequest;
    }
}
