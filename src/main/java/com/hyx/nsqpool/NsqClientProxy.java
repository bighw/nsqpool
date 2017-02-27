package com.hyx.nsqpool;

import com.hyx.nsqjava.core.commands.Publish;
import com.maizuo.api3.commons.util.LogUtils;
import com.maizuo.api3.commons.util.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.jboss.netty.channel.Channel;

/**
 * Created by Administrator on 2016/7/5.
 */
public class NsqClientProxy {
    private static GenericObjectPool<Channel> pool;

    static {
        NsqClientPoolFactory clientPoolFactory = new NsqClientPoolFactory();
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        if (!StringUtils.isEmpty(NsqpoolPros.getString("nsqpool.maxActive"))) {
        	// 最大空闲数量,鉴于为了保持写信息的高有效性，设定最大空闲数量和最大连接数保持一致，并且在配置文件里不暴露这个参数
            config.setMaxIdle(Integer.parseInt(NsqpoolPros.getString("nsqpool.maxActive"))); 
        }
        if (!StringUtils.isEmpty(NsqpoolPros.getString("nsqpool.maxActive"))) {
            config.setMaxTotal(Integer.parseInt(NsqpoolPros.getString("nsqpool.maxActive")));// 最大连接数量
        }
        config.setTestOnBorrow("true".equals(NsqpoolPros.getString("nsqpool.testOnBorrow")));
        if (!StringUtils.isEmpty(NsqpoolPros.getString("nsqpool.maxWaitMillis"))) {
            config.setMaxWaitMillis(Long.parseLong(NsqpoolPros.getString("nsqpool.maxWaitMillis")));// 最大等待时间
        }
        pool = new GenericObjectPool<Channel>(clientPoolFactory, config);
    }

    /**
     * 获取通道
     *
     * @return
     */
    public static Channel getChannel() {
        if (pool == null) {
            LogUtils.info("获取通道异常");
            return null;
        } else {
            try {
                return pool.borrowObject();
            } catch (Exception e) {
                LogUtils.info("获取通道异常");
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 发布信息
     *
     * @param topic
     * @param message
     */
    public static void publishMessage(String topic, String message) {
        Channel channel = getChannel();
        Publish publish = new Publish(topic, message.getBytes());
        channel.write(publish);
        NsqClientProxy.returnChannel(channel);
    }

    /**
     * 回收通道资源
     *
     * @param channel
     */
    public static void returnChannel(Channel channel) {
        pool.returnObject(channel);
    }

    /**
     * 获取连接池当前活跃连接
     *
     * @return
     */
    public static int getPoolObjectNum() {
        return pool.getNumIdle();
    }
}
