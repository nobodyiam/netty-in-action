import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Jason on 5/30/15.
 */
public class PlainNio2EchoServer {
    public void serve(int port) throws IOException {
        System.out.println("Listening for connections on port " + port);

        final AsynchronousServerSocketChannel serverChannel = AsynchronousServerSocketChannel.open();
        InetSocketAddress address = new InetSocketAddress(port);
        serverChannel.bind(address); // Bind Server to port
        final CountDownLatch latch = new CountDownLatch(1);
        serverChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() { // Start to accept new Client connections. Once one is accepted the CompletionHandler will get called.
            @Override
            public void completed(AsynchronousSocketChannel result, Object attachment) {
                serverChannel.accept(null, this); // Again accept new Client connections
                ByteBuffer buffer = ByteBuffer.allocate(100);
                result.read(buffer, buffer, new EchoCompletionHandler(result)); // Trigger a read operation on the Channel, the given CompletionHandler will be notified once something was read
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                try {
                    serverChannel.close(); // Close the socket on error
                } catch (IOException e) {
                    // ignore on close
                } finally {
                    latch.countDown();
                }
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private final class EchoCompletionHandler implements CompletionHandler<Integer, ByteBuffer> {

        private final AsynchronousSocketChannel channel;

        public EchoCompletionHandler(AsynchronousSocketChannel channel) {
            this.channel = channel;
        }

        @Override
        public void completed(Integer result, ByteBuffer buffer) {
            if (result == -1) {
                return;
            }
            buffer.flip();
            channel.write(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() { // Trigger a write operation on the Channel, the given CompletionHandler will be notified once something was written
                @Override
                public void completed(Integer result, ByteBuffer buffer) {
                    if (buffer.hasRemaining()) {
                        channel.write(buffer, buffer, this); // Trigger again a write operation if something is left in the ByteBuffer
                    } else {
                        buffer.compact();
                        channel.read(buffer, buffer, EchoCompletionHandler.this); // Trigger a read operation on the Channel, the given CompletionHandler will be notified once something was read
                    }
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    try {
                        channel.close();
                    } catch (IOException e) {
                        // ignore on close
                    }

                }
            });
        }

        @Override
        public void failed(Throwable exc, ByteBuffer attachment) {
            try {
                channel.close();
            } catch (IOException e) {
                // ignore on close
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new PlainNio2EchoServer().serve(4001);
    }
}
