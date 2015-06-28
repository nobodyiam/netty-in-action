import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedNioFile;

import java.io.RandomAccessFile;

/**
 * Created by Jason on 6/28/15.
 */
public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> { // Extend SimpleChannelInboundHandler and handle FullHttpRequest messages
    private final String wsUri;

    public HttpRequestHandler(String wsUri) {
        this.wsUri = wsUri;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if (wsUri.equalsIgnoreCase(request.getUri())) {
            ctx.fireChannelRead(request.retain()); // Check if the request is an WebSocket Upgrade request and if so retain it and pass it to the next ChannelInboundHandler in the ChannelPipeline. The call of retain() is needed as after channelRead0(...) completes it will call release() on the FullHttpRequest and so release the resources of it. Remember this is how SimpleChannelInboundHandler works.
            return;
        }

        if (HttpHeaders.is100ContinueExpected(request)) {
            send100Continue(ctx); // Handle 100 Continue requests to conform HTTP 1.1
            return;
        }

        RandomAccessFile file = new RandomAccessFile("netty/src/main/resources/index.html", "r"); // Open the index.html file which should be written back to the client

        HttpResponse response = new DefaultHttpResponse(request.getProtocolVersion(), HttpResponseStatus.OK);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");

        boolean keepAlive = HttpHeaders.isKeepAlive(request);

        if (keepAlive) {
            response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, file.length());
            response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }

        ctx.write(response); // Write the HttpRequest to the client. Be aware that we use a HttpRequest and not a FullHttpRequest as it is only the first part of the request. Also we not use writeAndFlush(..) as this should be done later.

        if (ctx.pipeline().get(SslHandler.class) == null) { // Write the index.html to the client. Depending on if SslHandler is in the ChannelPipeline use DefaultFileRegion or ChunkedNioFile
            ctx.write(new DefaultFileRegion(file.getChannel(), 0, file.length())); // zero-copy is archived which gives the best performance when transfer files over the network, but only when there is no encryption / compression.
        } else {
            ctx.write(new ChunkedNioFile(file.getChannel()));
        }

        ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT); // Write and flush the LastHttpContent to the client which marks the requests as complete.

        if (!keepAlive) {
            future.addListener(ChannelFutureListener.CLOSE); // Depending on if keepalive is used close the Channel after the write completes.
        }
    }

    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
        ctx.writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
