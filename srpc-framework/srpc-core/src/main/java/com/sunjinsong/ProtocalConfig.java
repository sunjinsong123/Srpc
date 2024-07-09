package com.sunjinsong;

public class ProtocalConfig {
    public String protocalName;


    public ProtocalConfig() {
    }

    public ProtocalConfig(String protocalName) {
        this.protocalName = protocalName;
    }

    /**
     * 获取
     * @return protocalName
     */
    public String getProtocalName() {
        return protocalName;
    }

    /**
     * 设置
     * @param protocalName
     */
    public void setProtocalName(String protocalName) {
        this.protocalName = protocalName;
    }

    @Override
    public String toString() {
        return "ProtocalConfig{protocalName = " + protocalName + "}";
    }
}
