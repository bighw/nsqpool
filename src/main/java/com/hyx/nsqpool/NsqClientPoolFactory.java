package com.hyx.nsqpool;

import com.hyx.nsqjava.core.NSQChannelHandler;
import com.hyx.nsqjava.core.NSQFrameDecoder;
import com.maizuo.api3.commons.util.LogUtils;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2016/7/5.
 */
public class NsqClientPoolFactory extends BasePooledObjectFactory<Channel> {
    private static ClientBootstrap bootstrap = null;

    static {
        init();
    }

    /**
     * 初始化bootstrap 对象
     */
    private static void init() {
        String host = NsqpoolPros.getString("nsqpool.ip"); 
        int port = Integer.parseInt(NsqpoolPros.getString("nsqpool.port")); 
        LogUtils.info("Connecting to " + host + ":" + port);

        // connect to nsqd TODO add step for lookup via nsqlookupd
        ChannelFactory factory = new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),Executors.newCachedThreadPool());

        bootstrap = new ClientBootstrap(factory);
        final NSQChannelHandler nsqhndl = new NSQChannelHandler(bootstrap);
        nsqhndl.setAuthenticated(true);
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

            public ChannelPipeline getPipeline() {
                return Channels.pipeline(new NSQFrameDecoder(), nsqhndl);
            }
        });
        bootstrap.setOption("tcpNoDelay", true);
        bootstrap.setOption("keepAlive", true);
        bootstrap.setOption("remoteAddress", new InetSocketAddress(host, port));
    }

    /**
     * 创建
     * @return Channel
     * @throws Exception
     */
    @Override
    public Channel create() throws Exception {
        if (bootstrap != null) {
            ChannelFuture future = bootstrap.connect();
            future.awaitUninterruptibly();
            if (!future.isSuccess()) {
                future.getCause().printStackTrace();
                return null;
            }
            return future.getChannel();
        }
        return null;
    }

    /**
     * 封装一个对象
     * @param obj
     * @return
     */
    @Override
    public PooledObject<Channel> wrap(Channel obj) {
        return new DefaultPooledObject<Channel>(obj);
    }

    /**
     * 验证对象是否正常
     * @param p
     * @return
     */
    @Override
    public boolean validateObject(PooledObject<Channel> p) {
//        return super.validateObject(p);
        Channel channel = p.getObject();
        return channel.isOpen();
    }

    /**
     * 销毁回收对象
     * @param p
     * @throws Exception
     */
    @Override
    public void destroyObject(PooledObject<Channel> p) throws Exception {
//        super.destroyObject(p);
        if (p.getObject().isOpen()) {
            p.getObject().close();
        }
    }
}
