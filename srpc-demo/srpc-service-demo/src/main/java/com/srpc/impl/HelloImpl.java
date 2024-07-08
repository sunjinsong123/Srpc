package com.srpc.impl;

import com.srpc.HelloSrpc;

public class HelloImpl implements HelloSrpc {
    @Override
    public String sayHello(String name) {
        return "hi  "+name;
    }
}
