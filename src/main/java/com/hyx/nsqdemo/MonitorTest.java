package com.hyx.nsqdemo;

import com.hyx.monitor.NsqMonitorController;

/**
 * Created by harvey on 2016/12/5.
 */
public class MonitorTest {
    public static void main(String[] args) {
        NsqMonitorController.getInstance().setNsqHost("127.0.0.1").setNsqPort(4150).setNsqTopic("MAIZUO_ORDER")
                .setNsqChannel("test").setNsqMessageThread(new MessageTest()).start();
    }
}
