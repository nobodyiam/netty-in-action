import com.google.common.base.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Jason on 6/14/15.
 */
public class PlainNioServer {
    private static final Logger logger = LoggerFactory.getLogger(PlainNioServer.class);

    public void serve(int port) throws IOException {
        logger.info("Listening for connections on port " + port);
        ServerSocketChannel serverSocketChannel;
        Selector selector;

        serverSocketChannel = ServerSocketChannel.open();
        ServerSocket serverSocket = serverSocketChannel.socket();
        serverSocket.bind(new InetSocketAddress(port)); // Bind server to port
        serverSocketChannel.configureBlocking(false);
        selector = Selector.open(); // Open selector that handles channels
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT); // Register serverSocket to selector and specify that it is interested in new accepted clients
        final ByteBuffer msg = ByteBuffer.wrap("Hi!\r\n".getBytes(Charsets.UTF_8));

        while (true) {
            try {
                selector.select(); // Wait for new events that are ready for process. This will block until something happens
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

            Set<SelectionKey> readyKeys = selector.selectedKeys(); // Obtain all SelectionKey instances that received events
            Iterator<SelectionKey> iterator = readyKeys.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                try {
                    if (key.isAcceptable()) { // Check if event was because new client ready to get accepted
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel client = server.accept();
                        logger.info("Accepted connection from " + client);
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ, msg.duplicate()); // Accept client and register it to selector
                    }
                    if (key.isWritable()) { // Check if event was because socket is ready to write data
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer buffer = (ByteBuffer) key.attachment();
                        while (buffer.hasRemaining()) {
                            // Write data to connected client. This may not write all the data if the network is saturated. If so it will pick up the not-written data and write it once the network is writable again.
                            if (client.write(buffer) == 0) {
                                break;
                            }
                        }
                        client.close(); // Close connection
                    }
                } catch (IOException ex) {
                    key.cancel();
                    try {
                        key.channel().close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            logger.error("Usage: " + PlainNioServer.class.getSimpleName() + " <port>");
            return;
        }
        int port = Integer.parseInt(args[0]);
        new PlainNioServer().serve(port);
    }
}
