
/**
 * Created by Jason on 7/2/15.
 */
public class SpdyRequestHandler extends PlainTextHttpRequestHandler {
    @Override
    protected String getContent() {
        return "This content is transmitted via SPDY\r\n";
    }
}
