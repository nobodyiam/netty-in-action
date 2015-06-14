import com.google.common.base.Charsets;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by Jason on 6/14/15.
 */
public class NettyNioServer {
    private static final Logger logger = LoggerFactory.getLogger(NettyNioServer.class);

    public void serve(int port) throws InterruptedException {
        final ByteBuf buf = Unpooled.copiedBuffer("Hi!\r\n", Charsets.UTF_8);
        EventLoopGroup group = new NioEventLoopGroup(); // Use NioEventLoopGroup for nonblocking mode

        try {
            ServerBootstrap b = new ServerBootstrap(); // Create ServerBootstrap to allow bootstrap to server

            b.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(port)
                    .childHandler(new ChannelInitializer<SocketChannel>() { // Specify ChannelInitializer called for each accepted connection
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
            logger.info(NettyNioServer.class.getName() + " started and listen on " + f.channel().localAddress());
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync(); // Release all resources
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 1) {
            logger.error("Usage: " + NettyNioServer.class.getSimpleName() + " <port>");
            return;
        }
        int port = Integer.parseInt(args[0]);
        new NettyNioServer().serve(port);
    }
}
