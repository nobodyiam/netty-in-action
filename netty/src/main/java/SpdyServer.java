import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.ImmediateEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.BogusSslContextFactory;

import javax.net.ssl.SSLContext;

/**
 * Created by Jason on 6/28/15.
 */
public class SpdyServer {
    private static final Logger logger = LoggerFactory.getLogger(SpdyServer.class);
    private final SSLContext context;
    private final int port;

    public SpdyServer(SSLContext context, int port) {
        this.port = port;
        this.context = context;
    }

    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();// Bootstraps the server
            b.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(port) // Specifies NIO transport, local socket address
                    .childHandler(new SpdyChannelInitializer(context));

            ChannelFuture f = b.bind().sync();// the call to the "sync()" method will cause this to block until the server is bound
            logger.info(SpdyServer.class.getName() + " started and listen on " + f.channel().localAddress());
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) throws Exception {
        // npn doesn't support jdk 1.8
        if (args.length != 1) {
            logger.error("Usage: " + SpdyServer.class.getSimpleName() + " <port>");
            return;
        }

        SSLContext context = BogusSslContextFactory.getServerContext();
        int port = Integer.parseInt(args[0]);
        new SpdyServer(context, port).start();
    }
}
