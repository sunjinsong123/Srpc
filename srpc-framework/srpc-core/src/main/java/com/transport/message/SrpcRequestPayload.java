package com.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SrpcRequestPayload  implements Serializable {


    //接口的名字
    private String interfaceName;

    //方法名
    private String methodName;
    //参数
    private Class<?>[] parameterTypes;
//    参数
    private Object[] parameters;
    //返回值
    private Class<?> returnType;
}
