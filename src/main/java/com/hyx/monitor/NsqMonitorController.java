package com.hyx.monitor;

import com.hyx.nsqjava.core.NSQFrameDecoder;
import org.apache.commons.lang.StringUtils;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Created by harvey on 2016/12/5.
 */
public class NsqMonitorController {
    private NsqBasicInfo nsqBasicInfo;

    private NsqMonitorController() {
    }

    private NsqBasicInfo getNsqBasicInfo() {
        return nsqBasicInfo;
    }

    private void setNsqBasicInfo(NsqBasicInfo nsqBasicInfo) {
        this.nsqBasicInfo = nsqBasicInfo;
    }

    public static NsqMonitorController getInstance() {
        NsqMonitorController nsqMonitorController = new NsqMonitorController();
        nsqMonitorController.setNsqBasicInfo(new NsqBasicInfo());
        return nsqMonitorController;
    }

    public NsqMonitorController setNsqTopic(String topic) {
        this.getNsqBasicInfo().setTopic(topic);
        return this;
    }
    public NsqMonitorController setNsqChannel(String channel) {
        this.getNsqBasicInfo().setChannel(channel);
        return this;
    }
    public NsqMonitorController setNsqMessageThread(NsqMessageThread nsqMessageThread) {
        this.getNsqBasicInfo().setNsqMessageThread(nsqMessageThread);
        return this;
    }

    public NsqMonitorController setNsqHost(String host) {
        this.getNsqBasicInfo().setHost(host);
        return this;
    }

    public NsqMonitorController setNsqPort(int port) {
        this.getNsqBasicInfo().setPort(port);
        return this;
    }

    public void start() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                final NsqBasicInfo nsqBasicInfo = NsqMonitorController.this.getNsqBasicInfo();
                if (nsqBasicInfo.getNsqMessageThread() == null) {
                    System.err.println("nsqpool:监听程序初始化失败:消息处理组件没有被正确的初始化");
                    return;
                }
                if (StringUtils.isBlank(nsqBasicInfo.getHost())) {
                    System.err.println("nsqpool:监听程序初始化失败:host参数没有被正确的初始化");
                    return;
                }
                if (StringUtils.isBlank(nsqBasicInfo.getTopic()) || nsqBasicInfo.getTopic().contains(":")) {
                    System.err.println("nsqpool:监听程序初始化失败:topic参数没有被正确的初始化,也许它含有非法字符':'");
                    return;
                }
                if (StringUtils.isBlank(nsqBasicInfo.getChannel()) || nsqBasicInfo.getChannel().contains(":")) {
                    System.err.println("nsqpool:监听程序初始化失败:channel参数没有被正确的初始化,也许它含有非法字符':'");
                    return;
                }
                if (nsqBasicInfo.getPort() <= 0) {
                    System.err.println("nsqpool:监听程序初始化失败:port参数没有被正确的初始化");
                    return;
                }
                ChannelFactory factory = new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool(),10);

                final ClientBootstrap bootstrap = new ClientBootstrap(factory);
                bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
                    public ChannelPipeline getPipeline() {
                        return Channels.pipeline(new NSQFrameDecoder(),new DefaultNsqChannelHandler(bootstrap, nsqBasicInfo.getNsqMessageThread(),nsqBasicInfo));
                    }
                });

                bootstrap.setOption("tcpNoDelay", true);
                bootstrap.setOption("keepAlive", true);
                InetSocketAddress addr = new InetSocketAddress(nsqBasicInfo.getHost(), nsqBasicInfo.getPort());
                bootstrap.setOption("remoteAddress", addr);

                ChannelFuture future = bootstrap.connect(addr);

                if (!future.isSuccess()) {
                    if (future.getCause() != null)
                        future.getCause().printStackTrace();
                }
                future.getChannel().getCloseFuture().awaitUninterruptibly();
            }
        });
        thread.start();
    }
}
