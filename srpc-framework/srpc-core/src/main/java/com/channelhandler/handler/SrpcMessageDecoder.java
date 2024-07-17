package com.channelhandler.handler;

import com.sunjinsong.common.MessageFormConstant;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import static com.sunjinsong.common.MessageFormConstant.*;

public class SrpcMessageDecoder extends LengthFieldBasedFrameDecoder {
    public SrpcMessageDecoder() {
        super(
                // 找到当前报文的总长度，截取报文，截取出来的报文我们可以去进行解析
                // 最大帧的长度，超过这个maxFrameLength值会直接丢弃
                MAX_FRAME_LENGTH,
                // 长度的字段的偏移量，
                MAGIC.length + VERSION_LENGTH + HEADER_FIELD_LENGTH,
                // 长度的字段的长度
               FULL_FIELD_LENGTH,
                // todo 负载的适配长度
                -(MAGIC.length + VERSION_LENGTH
                        + HEADER_FIELD_LENGTH + FULL_FIELD_LENGTH),
                0);
    }
}
