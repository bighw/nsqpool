package com.hyx.monitor;

import com.hyx.nsqjava.core.NSQChannelHandler;
import com.hyx.nsqjava.core.NSQFrame;
import com.hyx.nsqjava.core.commands.Finish;
import com.hyx.nsqjava.core.commands.Ready;
import com.hyx.nsqjava.core.commands.Subscribe;
import com.hyx.nsqjava.enums.ResponseType;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;


/**
 * Created by harvey on 2016/12/5.
 */
public class DefaultNsqChannelHandler extends NSQChannelHandler {
    // 用来处理收到信息之后的业务
    private NsqMessageThread nsqMessageThread;
    private NsqBasicInfo nsqBasicInfo;
    public DefaultNsqChannelHandler(ClientBootstrap bootstrap, NsqMessageThread nsqMessageThread,NsqBasicInfo nsqBasicInfo) {
        super(bootstrap);
        this.nsqMessageThread = nsqMessageThread;
        this.nsqBasicInfo = nsqBasicInfo;
    }
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        Object o = e.getMessage();
        if (o instanceof NSQFrame) {
            NSQFrame frm = (NSQFrame) o;
            try {
                // do stuff with it.
                String message = new String(frm.getMsg().getBody(), "UTF-8");
//				Log.info("sms Received message\n" + message);
                // once done, confirm with server
                nsqMessageThread.handlerMessage(message);
                ChannelFuture future = e.getChannel().write(new Finish(frm.getMsg().getMessageId()));
                future.sync();
                e.getChannel().write(new Ready(100)).sync();
            } catch (Exception ex) {
                ex.printStackTrace();
                ChannelFuture future = e.getChannel().write(new Finish(frm.getMsg().getMessageId()));
                try {
                    String message = new String(frm.getMsg().getBody(), "UTF-8");
                    future.sync();
                    e.getChannel().write(new Ready(100)).sync();
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

            }
        } else if (o instanceof ResponseType) {
            // don't care
        } else {
            //
        }
    }

    @Override
    protected void nsqAuthenticated(ChannelFuture future) {
        Channel chan = future.getChannel();
        Subscribe sub = new Subscribe(nsqBasicInfo.getTopic(), nsqBasicInfo.getChannel(), "SUBSCRIBERSHORT", "SUBSCRIBERLONG");
//		Subscribe sub = new Subscribe("smsChannelTest", "CHANNELFOO", "SUBSCRIBERSHORT", "SUBSCRIBERLONG");
        chan.write(sub);
        Ready rdy = new Ready(100);
        chan.write(rdy);
    }
}
