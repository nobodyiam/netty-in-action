import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.oio.OioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Created by Jason on 6/14/15.
 */
public class NettyOioServer {
    private static final Logger logger = LoggerFactory.getLogger(NettyOioServer.class);

    public void serve(int port) throws InterruptedException {
        final ByteBuf buf = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Hi!\r\n", StandardCharsets.UTF_8));
        EventLoopGroup group = new OioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap(); // Create ServerBootstrap to allow bootstrap to server instance
            b.group(group)
                    .channel(OioServerSocketChannel.class) // Use OioEventLoopGroup Ito allow blocking mode (Old-IO)
                    .localAddress(port)
                    .childHandler(new ChannelInitializer<SocketChannel>() { // Specify ChannelInitializer that will be called for each accepted connection
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() { // Add ChannelHandler to intercept events and allow to react on them
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    ctx.writeAndFlush(buf.duplicate())
                                            .addListener(ChannelFutureListener.CLOSE); // Write message to client and add ChannelFutureListener to close connection once message written
                                }
                            });
                        }
                    });
            ChannelFuture f = b.bind().sync(); // Bind server to accept connections
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync(); // Release all resources
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 1) {
            logger.error("Usage: " + NettyOioServer.class.getSimpleName() + " <port>");
            return;
        }
        int port = Integer.parseInt(args[0]);
        new NettyOioServer().serve(port);
    }
}
