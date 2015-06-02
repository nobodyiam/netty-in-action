import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;


/**
 * Created by Jason on 6/2/15.
 */
public class EchoClient {
    private final String host;
    private final int port;

    public EchoClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap(); // Create bootstrap for client
            b.group(group) // Specify EventLoopGroup to handle client events. NioEventLoopGroup is used, as the NIO-Transport should be used
                    .channel(NioSocketChannel.class) // Specify channel type; use correct one for NIO-Transport
                    .remoteAddress(this.host, this.port) // Set InetSocketAddress to which client connects
                    .handler(new ChannelInitializer<SocketChannel>() { // Specify ChannelHandler, using ChannelInitializer, called once connection established and channel created
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new EchoClientHandler()); // Add EchoClientHandler to ChannelPipeline that belongs to channel. ChannelPipeline holds all ChannelHandlers of channel
                        }
                    });
            ChannelFuture f = b.connect().sync(); // Connect client to remote peer; wait until sync() completes connect completes
            System.out.println(this.getClass().getName() + " started and connected to " + f.channel().remoteAddress() + " local address: " + f.channel().localAddress());
            f.channel().closeFuture().sync(); // Wait until ClientChannel closes. This will block.
        } finally {
            group.shutdownGracefully().sync(); // Shut down bootstrap and thread pools; release all resources
        }
    }

    public static void main(String[] args) throws InterruptedException {
        if (args.length != 2) {
            System.err.print("Usage: " + EchoClient.class.getSimpleName() + " <host> <port>");
            return;
        }

        final String host = args[0];
        final int port = Integer.parseInt(args[1]);
        new EchoClient(host, port).start();
    }
}

