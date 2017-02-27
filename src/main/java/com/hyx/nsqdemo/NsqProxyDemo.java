package com.hyx.nsqdemo;

import com.hyx.nsqpool.NsqClientProxy;

/**
 * Created by Administrator on 2016/7/6.
 */
public class NsqProxyDemo {
    public static void main(String[] args) {
        // 简单的一行代码即可压一条信息
        NsqClientProxy.publishMessage("SEAT_SCHEDULE_UPDATE", "3357");
    }
}
