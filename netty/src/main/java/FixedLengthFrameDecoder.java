import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Created by Jason on 6/28/15.
 */
public class FixedLengthFrameDecoder extends ByteToMessageDecoder { // Extend ByteToMessageDecoder and so handle inbound bytes and decode them to messages
    private final int frameLength;

    public FixedLengthFrameDecoder(int frameLength) { // Specify the length of frames that should be produced
        Preconditions.checkArgument(frameLength > 0, "frame length must be a positive integer: " + frameLength);
        this.frameLength = frameLength;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        while (in.readableBytes() >= frameLength) { // Check if enough bytes are ready to read for process the next frame
            ByteBuf buf = in.readBytes(frameLength); // Read a new frame out of the ByteBuf
            out.add(buf); // Add the frame to the List of decoded messages.
        }
    }
}
