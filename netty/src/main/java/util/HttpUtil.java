package util;

import com.google.common.base.Charsets;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

/**
 * Created by Jason on 2/6/16.
 */
public class HttpUtil {

    public static void sendHttpResponse(HttpVersion httpVersion, HttpResponseStatus responseStatus, String contentType,
                                        boolean keepAlive, String content, ChannelHandlerContext ctx) {
        HttpResponse response = new DefaultHttpResponse(httpVersion, responseStatus);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, contentType);

        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, content.length());
        if (keepAlive) {
            response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }

        ctx.write(response);

        ctx.write(Unpooled.copiedBuffer(content, Charsets.UTF_8));

        ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT); // Write and flush the LastHttpContent to the client which marks the requests as complete.

        if (!keepAlive) {
            future.addListener(ChannelFutureListener.CLOSE); // Depending on if keepalive is used close the Channel after the write completes.
        }
    }

    public static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
        ctx.writeAndFlush(response);
    }
}
