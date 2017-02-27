package com.hyx.monitor;

/**
 * Created by harvey on 2016/12/5.
 */
public class NsqBasicInfo {
    private String topic; // nsq topic
    private String channel; // nsq channel
    private NsqMessageThread nsqMessageThread;  // 消息处理的线程
    private String host; // host
    private int port; // port

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public NsqMessageThread getNsqMessageThread() {
        return nsqMessageThread;
    }

    public void setNsqMessageThread(NsqMessageThread nsqMessageThread) {
        this.nsqMessageThread = nsqMessageThread;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}
