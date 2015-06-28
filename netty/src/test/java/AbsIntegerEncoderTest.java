import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Created by Jason on 6/28/15.
 */
public class AbsIntegerEncoderTest {
    @Test
    public void testEncoded() {
        ByteBuf buf = Unpooled.buffer();
        for (int i = 1; i < 10; i++) {
            buf.writeInt(-i);
        }
        EmbeddedChannel channel = new EmbeddedChannel(new AbsIntegerEncoder());

        Assert.assertTrue(channel.writeOutbound(buf));
        Assert.assertTrue(channel.finish());

        for (int i = 1; i < 10; i++) {
            Assert.assertEquals(i, channel.readOutbound());
        }
        Assert.assertNull(channel.readOutbound());

    }
}
