package com.hyx.nsqdemo;

import com.hyx.monitor.NsqMessageThread;

/**
 * Created by harvey on 2016/12/5.
 */
public class MessageTest extends NsqMessageThread {
    @Override
    public void handlerMessage(String message) {
        System.out.println("监听测试---》"+message);
    }
}
