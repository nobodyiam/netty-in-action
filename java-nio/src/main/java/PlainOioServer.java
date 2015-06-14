import com.google.common.base.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Jason on 6/14/15.
 */
public class PlainOioServer {
    private static final Logger logger = LoggerFactory.getLogger(PlainOioServer.class);

    public void serve(int port) throws IOException {
        final ServerSocket serverSocket = new ServerSocket(port); // Bind server to port
        try {
            while (true) {
                final Socket clientSocket = serverSocket.accept(); // Accept connection
                logger.info("Accepted connection from " + clientSocket);

                new Thread(new Runnable() { // Create new thread to handle connection
                    @Override
                    public void run() {
                        OutputStream out;

                        try {
                            out = clientSocket.getOutputStream();
                            out.write("Hi!\r\n".getBytes(Charsets.UTF_8)); // Write message to connected client
                            out.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                clientSocket.close(); // Close connection
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start(); // Start thread to begin handling
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            logger.error("Usage: " + PlainOioServer.class.getSimpleName() + " <port>");
            return;
        }
        int port = Integer.parseInt(args[0]);
        new PlainOioServer().serve(port);
    }
}
