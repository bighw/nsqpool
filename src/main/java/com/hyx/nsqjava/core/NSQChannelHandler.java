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

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class NSQChannelHandler extends SimpleChannelHandler {

    //    private static final Logger log = LoggerFactory.getLogger(NSQChannelHandler.class);
    private int reconnectDelay = 0;
    private Timer timer;

    private boolean authenticated = false;
    private boolean closing = false;
    private ClientBootstrap bootstrap;

    private final ChannelGroup allChannels = new DefaultChannelGroup("NSQ-channel-group");

    public NSQChannelHandler(ClientBootstrap bootstrap) {
        this.bootstrap = bootstrap;
        timer = new HashedWheelTimer();
    }

    protected InetSocketAddress getRemoteAddress() {
        return (InetSocketAddress) bootstrap.getOption("remoteAddress");
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        Object o = e.getMessage();
        if (o instanceof ResponseType) {
            if (o == ResponseType.HEARTBEAT) {
            } else {
            }
        } else if (o instanceof NSQFrame) {
            NSQFrame frm = (NSQFrame) o;
            // do stuff with it.
            e.getChannel().write(new Finish(frm.getMsg().getMessageId()));
        } else {
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        ctx.getChannel().close();
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        super.channelDisconnected(ctx, e);
        this.authenticated = false;

    }

    @Override
    public void closeRequested(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        super.closeRequested(ctx, e);
    }

    ;

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        if (!closing) {
            allChannels.remove(e.getChannel());

            if (reconnectDelay > 0) {
                timer.newTimeout(new TimerTask() {
                    public void run(Timeout timeout) throws Exception {
                        bootstrap.connect();

                    }
                }, reconnectDelay, TimeUnit.SECONDS);
                reconnectDelay = reconnectDelay * 2;
            } else {
                reconnectDelay = 1;
                bootstrap.connect();

            }
        }
    }

    @Override
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Object o = e.getMessage();
        if (o instanceof NSQFrame) {
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
        }

    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        reconnectDelay = 0;
        ChannelFuture future = e.getChannel().write(new Magic());
        future.addListener(new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    setAuthenticated(true);
                    nsqAuthenticated(future);
                } else if (future.isCancelled()) {
                } else if (future.isDone()) {
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
