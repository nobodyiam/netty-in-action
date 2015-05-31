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
 * Created by Jason on 5/29/15.
 */
public class PlainNioEchoServer {
    public void serve(int port) throws IOException {
        System.out.println("Listening for connections on port " + port);

        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        ServerSocket ss = serverChannel.socket();
        InetSocketAddress address = new InetSocketAddress(port);
        ss.bind(address); // Bind server to port
//        serverChannel.bind(address); // since 1.7
        serverChannel.configureBlocking(false);

        Selector selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT); // Register the channel with the selector to be interested in new Client connections that get accepted

        while (true) {
            try {
                selector.select(); // Block until something is selected
            } catch (IOException ex) {
                ex.printStackTrace();
                break;
            }

            Set<SelectionKey> readyKeys = selector.selectedKeys(); // Get all SelectedKey instances
            Iterator<SelectionKey> iterator = readyKeys.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove(); // Remove the SelectedKey from the iterator
                try {
                    if (key.isAcceptable()) {
                        ServerSocketChannel server = (ServerSocketChannel)key.channel();
                        SocketChannel client = server.accept(); // Accept the client connection
                        System.out.println("Accepted connection from " + client);
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ, ByteBuffer.allocate(100)); // Register connection to selector and set ByteBuffer
                    }

                    if (key.isReadable()) { // Check for SelectedKey for read
                        SocketChannel client = (SocketChannel)key.channel();
                        ByteBuffer output = (ByteBuffer)key.attachment();
                        client.read(output); // Read data to ByteBuffer
                    }

                    if (key.isWritable()) { // Check for SelectedKey for write
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer output = (ByteBuffer)key.attachment();
                        output.flip();
                        client.write(output); // Write data from ByteBuffer to channel
                        output.compact();
                    }
                } catch (IOException ex) {
                    key.cancel();
                    try {
                        key.channel().close();
                    } catch (IOException ces) {}
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new PlainNioEchoServer().serve(4000);
    }
}
