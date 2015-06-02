import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

/**
 * Created by Jason on 6/2/15.
 */
@Sharable // Annotate with @Sharable as it can be shared between channels
public class EchoClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close(); // Log exception and close channel
    }

    /**
     * The channelActive() method is called once the connection is established.
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.copiedBuffer("Netty rocks", CharsetUtil.UTF_8)); // Write message now that channel is connected
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        System.out.println("Client received: " + ByteBufUtil.hexDump(in.readBytes(in.readableBytes()))); // Log received message as hexdump

    }
}
