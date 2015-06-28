import io.netty.channel.group.ChannelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.BogusSslContextFactory;

import javax.net.ssl.SSLContext;

/**
 * Created by Jason on 6/28/15.
 */
public class SecureChatServer extends ChatServer {
    private static final Logger logger = LoggerFactory.getLogger(SecureChatServer.class);
    private final SSLContext context;

    public SecureChatServer(SSLContext context, int port) {
        super(port);
        this.context = context;
    }

    @Override
    protected ChatServerInitializer createInitializer(ChannelGroup channelGroup) {
        return new SecureChatServerIntializer(channelGroup, context);
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            logger.error("Usage: " + SecureChatServer.class.getSimpleName() + " <port>");
            return;
        }

        SSLContext context = BogusSslContextFactory.getServerContext();
        int port = Integer.parseInt(args[0]);
        new SecureChatServer(context, port).start();
    }
}
