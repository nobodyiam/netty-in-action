import io.netty.channel.group.ChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

/**
 * Created by Jason on 6/28/15.
 */
public class SecureChatServerIntializer extends ChatServerInitializer {
    private final SSLContext context;

    public SecureChatServerIntializer(ChannelGroup channelGroup, SSLContext context) {
        super(channelGroup);
        this.context = context;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        super.initChannel(socketChannel);
        SSLEngine engine = context.createSSLEngine();
        engine.setUseClientMode(false);
        socketChannel.pipeline().addFirst(new SslHandler(engine));
    }
}
