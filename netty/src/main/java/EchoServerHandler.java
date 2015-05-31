import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by Jason on 5/31/15.
 */
@ChannelHandler.Sharable // Annotate with @Sharable to share between channels
public class EchoServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("Server received: " + msg);

        ctx.write(msg); // Write the received messages back . Be aware that this will not “flush” the messages to the remote peer yet.
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
                .addListener(ChannelFutureListener.CLOSE); // Flush all previous written messages (that are pending) to the remote peer, and close the channel after the operation is complete.
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace(); // Log exception
        ctx.close(); // Close channel on exception
    }
}
