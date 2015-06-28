import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Jason on 6/28/15.
 */
public class FixedLengthFrameDecoderTest {

    @Test
    public void testFramesDecoded() {
        int someFrameLength = 3;
        ByteBuf buf = Unpooled.buffer();
        for (int i = 0; i < 3 * someFrameLength; i++) {
            buf.writeByte(i);
        }
        ByteBuf input = buf.copy();

        EmbeddedChannel channel = new EmbeddedChannel(new FixedLengthFrameDecoder(someFrameLength));

        Assert.assertTrue(channel.writeInbound(input));
        Assert.assertTrue(channel.finish());

        Assert.assertEquals(buf.readBytes(someFrameLength), channel.readInbound());
        Assert.assertEquals(buf.readBytes(someFrameLength), channel.readInbound());
        Assert.assertEquals(buf.readBytes(someFrameLength), channel.readInbound());
        Assert.assertNull(channel.readInbound());
    }

    @Test
    public void testFramesDecoded2() {
        int someFrameLength = 3;
        ByteBuf buf = Unpooled.buffer();
        for (int i = 0; i < 3 * someFrameLength; i++) {
            buf.writeByte(i);
        }
        ByteBuf input = buf.copy();

        EmbeddedChannel channel = new EmbeddedChannel(new FixedLengthFrameDecoder(someFrameLength));

        Assert.assertFalse(channel.writeInbound(input.readBytes(someFrameLength - 1)));
        Assert.assertTrue(channel.writeInbound(input.readBytes(2 * someFrameLength + 1)));

        Assert.assertTrue(channel.finish());
        Assert.assertEquals(buf.readBytes(someFrameLength), channel.readInbound());
        Assert.assertEquals(buf.readBytes(someFrameLength), channel.readInbound());
        Assert.assertEquals(buf.readBytes(someFrameLength), channel.readInbound());
        Assert.assertNull(channel.readInbound());
    }
}
