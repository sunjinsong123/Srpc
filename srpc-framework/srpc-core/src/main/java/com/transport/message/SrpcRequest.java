package com.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SrpcRequest {



    private long requestId;
    //请求类型
    private byte requestType;
    //压缩的类型
    private byte compressType;
    //序列化类型
    private byte serializeType;

    private long timeStamp;


    private SrpcRequestPayload payload;



}
