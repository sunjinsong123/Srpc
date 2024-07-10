package com.sunjinsong.utils.net;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
@Slf4j
public class NetUtils {

    /**
     * 获取本机的一个非环回 IPv4 地址。
     * @return 本机的一个有效 IP 地址，如果没有找到则返回 null。
     */
    public static String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                // 忽略虚拟和未启用的网络接口
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    // 过滤IPv6地址，只获取IPv4地址
                    if (address.getHostAddress().indexOf(':') == -1) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            log.error("获取本机 IP 地址失败: " + e.getMessage());
        }
        return null;
    }

    public static void main(String[] args) {
        System.out.println(NetUtils.getLocalIpAddress());
    }
}

