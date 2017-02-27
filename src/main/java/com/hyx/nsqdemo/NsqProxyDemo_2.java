package com.hyx.nsqdemo;

import com.hyx.nsqjava.core.commands.Publish;
import com.hyx.nsqpool.NsqClientProxy;
import org.jboss.netty.channel.Channel;

/**
 * Created by Administrator on 2016/7/6.
 */
public class NsqProxyDemo_2 {
    public static void main(String[] args) {
        // 获取通道
        Channel channel = NsqClientProxy.getChannel();
        // 构建发布信息
        Publish publish = new Publish("THIS_GAVINTEST", "hello".getBytes());
        // 发送信息
        channel.write(publish);
        // 继续发送
        channel.write(publish);
        // 回收通道资源
        NsqClientProxy.returnChannel(channel);

    }
}
