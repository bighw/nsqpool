package com.hyx.nsqjava.core;

import com.hyx.nsqjava.core.commands.Finish;
import com.hyx.nsqjava.core.commands.Magic;
import com.hyx.nsqjava.core.commands.NSQCommand;
import com.hyx.nsqjava.enums.ResponseType;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class NSQHandler extends SimpleChannelHandler {

    private static final Logger log = LoggerFactory.getLogger(NSQChannelHandler.class);
    private int reconnectDelay = 0;
    private Timer timer;

    private boolean authenticated = false;
    private boolean closing = false;
    private ClientBootstrap bootstrap;

    private final ChannelGroup allChannels = new DefaultChannelGroup("NSQ-channel-group");

    public NSQHandler(ClientBootstrap bootstrap) {
        log.debug("Created a new NSQChannelHandler");
        this.bootstrap = bootstrap;
        timer = new HashedWheelTimer();
    }

    protected InetSocketAddress getRemoteAddress() {
        return (InetSocketAddress) bootstrap.getOption("remoteAddress");
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        log.debug("Received message " + e.getMessage());
        Object o = e.getMessage();
        if (o instanceof ResponseType) {
            if (o == ResponseType.HEARTBEAT) {
                log.trace("Received heartbeat");
            } else {
                log.trace("Received " + o);
            }
        } else if (o instanceof NSQFrame) {
            log.debug("received nsqframe");
            NSQFrame frm = (NSQFrame) o;
            // do stuff with it.
            e.getChannel().write(new Finish(frm.getMsg().getMessageId()));
        } else {
            log.debug("something else " + o);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        log.error("Exception caught in NSQ channel", e.getCause());
        ctx.getChannel().close();
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        super.channelDisconnected(ctx, e);
        this.authenticated = false;
        log.debug("Disconnected from NSQ " + getRemoteAddress());
        log.debug(String.format("Channel state %s %s %s", e.getState(), e.getValue(), e.getClass()));

    }

    @Override
    public void closeRequested(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        log.debug("Close requested");
        log.debug(String.format("Channel state %s %s %s", e.getState(), e.getValue(), e.getClass()));
        super.closeRequested(ctx, e);
    }

    ;

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        log.debug(String.format("Channel state %s %s %s", e.getState(), e.getValue(), e.getClass()));
        log.debug("Sleeping for: " + reconnectDelay + "s");
        if (!closing) {
            allChannels.remove(e.getChannel());

            if (reconnectDelay > 0) {
                timer.newTimeout(new TimerTask() {
                    public void run(Timeout timeout) throws Exception {
                        log.debug("Reconnecting to: " + getRemoteAddress());
                        bootstrap.connect();

                    }
                }, reconnectDelay, TimeUnit.SECONDS);
                reconnectDelay = reconnectDelay * 2;
            } else {
                log.debug("Reconnecting to: " + getRemoteAddress());
                reconnectDelay = 1;
                bootstrap.connect();

            }
        }
    }

    @Override
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Object o = e.getMessage();
        if (o instanceof NSQFrame) {
            log.debug("NSQFrame writing");
            if (!isAuthenticated()) {
                // TODO - should probably queue this stuff up rather than throw exception.
                e.getFuture().setFailure(new Exception("Not authenticated yet"));
                return;
            }
            NSQFrame frame = (NSQFrame) e.getMessage();

            ChannelBuffer buf = ChannelBuffers.buffer(frame.getSize());
            buf.writeBytes(frame.getBytes());

            Channels.write(ctx, e.getFuture(), buf);

        } else if (o instanceof NSQCommand) {
            log.debug("NSQCommand writing " + o);
            if (!isAuthenticated() && !(o instanceof Magic)) {
                // TODO - should probably queue this stuff up rather than throw exception.
                e.getFuture().setFailure(new Exception("Not authenticated yet"));
                return;

            }
            NSQCommand cmd = (NSQCommand) e.getMessage();
            byte[] bytes = cmd.getCommandBytes();
            ChannelBuffer buf = ChannelBuffers.buffer(bytes.length);
            buf.writeBytes(bytes);

            Channels.write(ctx, e.getFuture(), buf);

        } else {
            log.error("Unknown message type " + o);
        }

    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        log.debug("Channel connected");
        reconnectDelay = 0;
        ChannelFuture future = e.getChannel().write(new Magic());
        future.addListener(new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    log.debug("Authenticated");
                    setAuthenticated(true);
                    nsqAuthenticated(future);
                } else if (future.isCancelled()) {
                    log.debug("CANCELLED");
                } else if (future.isDone()) {
                    log.debug("DONE");
                }
            }
        });

    }

    /**
     * Called when we have reauthenticated to nsqd. Override to add
     * (re)subscribe behaviour
     */
    protected void nsqAuthenticated(ChannelFuture future) {

    }


    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        log.debug("Opening a new channel");
        allChannels.add(e.getChannel());

    }

    /**
     * Used to close app gracefully, closes all channels and timer.
     *
     * @return
     */
    public ChannelGroupFuture close() {
        closing = true;
        timer.stop();
        return allChannels.close();
    }

    public ChannelGroup getAllChannels() {
        return allChannels;
    }

}

