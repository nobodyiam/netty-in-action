import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

/**
 * Created by Jason on 7/3/15.
 */
public class LogEventDecoder extends MessageToMessageDecoder<DatagramPacket> {
    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) throws Exception {
        ByteBuf data = msg.content();
        int i = data.indexOf(0, data.readableBytes(), LogEvent.SEPARATOR);
        String fileName = data.slice(0, i).toString(Charsets.UTF_8);
        String logMsg = data.slice(i + 1, data.readableBytes()).toString(Charsets.UTF_8);

        LogEvent event = new LogEvent(msg.recipient(), System.currentTimeMillis(), fileName, logMsg);
        out.add(event);
    }
}
