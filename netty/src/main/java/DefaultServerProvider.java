import com.google.common.collect.Lists;
import org.eclipse.jetty.npn.NextProtoNego;

import java.util.Collections;
import java.util.List;

/**
 * Created by Jason on 7/2/15.
 */
public class DefaultServerProvider implements NextProtoNego.ServerProvider {
    private static final List<String> PROTOCOLS = Collections.unmodifiableList(Lists.newArrayList("spdy/3", "spdy/3.1", "http/1.1"));

    private String protocol;

    @Override
    public void unsupported() {
        protocol = "http/1.1";
    }

    @Override
    public List<String> protocols() {
        return PROTOCOLS;
    }

    @Override
    public void protocolSelected(String s) {
        this.protocol = s;
    }

    public String getSelectedProtocol() {
        return protocol;
    }
}
