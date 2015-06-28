import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.TooLongFrameException;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Created by Jason on 6/28/15.
 */
public class FrameChunkDecoderTest {

    @Test
    public void testFramesDecoded() {
        int someMaxFrameSize = 3;
        ByteBuf buf = Unpooled.buffer();
        for (int i = 0; i < 3 * someMaxFrameSize; i++) {
            buf.writeByte(i);
        }
        ByteBuf input = buf.copy();

        EmbeddedChannel channel = new EmbeddedChannel(new FrameChunkDecoder(someMaxFrameSize));

        Assert.assertTrue(channel.writeInbound(input.readBytes(someMaxFrameSize - 1)));
        try {
            channel.writeInbound(input.readBytes(someMaxFrameSize + 1)); // Write a frame which is bigger then the max frame size and check if this cause an TooLongFrameException
            Assert.fail();
        } catch (TooLongFrameException e) {
        }

        Assert.assertTrue(channel.writeInbound(input.readBytes(someMaxFrameSize)));
        Assert.assertTrue(channel.finish());

        Assert.assertEquals(buf.readBytes(someMaxFrameSize - 1), channel.readInbound());
        Assert.assertEquals(buf.skipBytes(someMaxFrameSize + 1).readBytes(someMaxFrameSize), channel.readInbound());
    }
}
