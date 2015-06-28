import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

/**
 * Created by Jason on 6/28/15.
 */
public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private final ChannelGroup group;

    public TextWebSocketFrameHandler(ChannelGroup group) {
        this.group = group;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception { // Override the userEventTriggered( ) method to handle custom events
        if (evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {
            ctx.pipeline().remove(HttpRequestHandler.class); // If the event is received that indicate that the handshake was successful remove the HttpRequestHandler from the ChannelPipeline as no further HTTP messages will be send.

            group.writeAndFlush(new TextWebSocketFrame("Client " + ctx.channel() + " joined")); // Write a message to all connected WebSocket clients about a new Channel that is now also connected

            group.add(ctx.channel());
            return;
        }

        super.userEventTriggered(ctx, evt);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        group.writeAndFlush(msg.retain()); // Retain the received message and write and flush it to all connected WebSocket clients. Calling retain() is needed because otherwise the TextWebSocketFrame would be released once the channelRead0(...) method returned. This is a problem as writeAndFlush(...) may complete later.
    }
}
