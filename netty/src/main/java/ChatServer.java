import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.ImmediateEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Jason on 5/31/15.
 */
public class ChatServer {
    private static final Logger logger = LoggerFactory.getLogger(ChatServer.class);
    private final int port;

    public ChatServer(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        final ChannelGroup channelGroup =
                new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE); // Create DefaultChannelGroup which will hold all connected WebSocket clients

        try {
            ServerBootstrap b = new ServerBootstrap();// Bootstraps the server
            b.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(port) // Specifies NIO transport, local socket address
                    .childHandler(createInitializer(channelGroup));

            ChannelFuture f = b.bind().sync();// the call to the "sync()" method will cause this to block until the server is bound
            logger.info(ChatServer.class.getName() + " started and listen on " + f.channel().localAddress());
            f.channel().closeFuture().sync();
        } finally {
            channelGroup.close().sync();
            group.shutdownGracefully().sync();
        }
    }

    protected ChatServerInitializer createInitializer(ChannelGroup channelGroup) {
        return new ChatServerInitializer(channelGroup);
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            logger.error("Usage: " + ChatServer.class.getSimpleName() + " <port>");
            return;
        }
        int port = Integer.parseInt(args[0]);
        new ChatServer(port).start();
    }
}
