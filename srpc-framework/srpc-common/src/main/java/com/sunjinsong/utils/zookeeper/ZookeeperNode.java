package com.sunjinsong.utils.zookeeper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZookeeperNode {
    private String Nodepath;
    private byte[] date;

}
