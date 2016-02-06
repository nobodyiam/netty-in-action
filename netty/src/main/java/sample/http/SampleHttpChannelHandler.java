package sample.http;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import util.HttpUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Jason on 2/6/16.
 */
public class SampleHttpChannelHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final AtomicInteger counter;
    private final ExecutorService executorService;
    private final int POOL_SIZE = 30;

    public SampleHttpChannelHandler() {
        counter = new AtomicInteger();
        executorService = Executors.newFixedThreadPool(POOL_SIZE, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, SampleHttpChannelHandler.class.getName() + counter.incrementAndGet());
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //can do some customized exception handling here
        super.exceptionCaught(ctx, cause);
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final FullHttpRequest request) throws Exception {
        if (HttpHeaders.is100ContinueExpected(request)) {
            HttpUtil.send100Continue(ctx); // Handle 100 Continue requests to conform HTTP 1.1
            return;
        }

        final ByteBuf requestContent =  request.content().copy(); //request.content will be released when this method is done
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                HttpMethod method = request.getMethod();
                if (!checkMethodAllowed(method)) {
                    return;
                }

                String name = parseParameter("name", "World");
                String path = parsePath(request.getUri());

                HttpUtil.sendHttpResponse(request.getProtocolVersion(), HttpResponseStatus.OK, "text/plain; charset=UTF-8",
                        HttpHeaders.isKeepAlive(request), String.format("Hello %s from %s!", name, path), ctx);
            }

            private boolean checkMethodAllowed(HttpMethod method) {
                if (method.equals(HttpMethod.GET) || method.equals(HttpMethod.POST)) {
                    return true;
                }
                HttpUtil.sendHttpResponse(request.getProtocolVersion(), HttpResponseStatus.METHOD_NOT_ALLOWED,
                        "text/plain; charset=UTF-8", HttpHeaders.isKeepAlive(request), method.name() + " not allowed.", ctx);
                return false;
            }

            private String parseParameter(String paramName, String defaultValue) {
                HttpMethod method = request.getMethod();
                if (method.equals(HttpMethod.GET)) {
                    QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri(), true);
                    Map<String, List<String>> params = queryStringDecoder.parameters();
                    if (params.containsKey(paramName)) {
                        return Joiner.on(",").join(params.get(paramName));
                    }
                }

                if (method.equals(HttpMethod.POST)) {
                    //Only handle application/x-www-form-urlencoded post body
                    QueryStringDecoder bodyDecoder = new QueryStringDecoder(requestContent.toString(Charsets.UTF_8), false);
                    Map<String, List<String>> params = bodyDecoder.parameters();
                    if (params.containsKey(paramName)) {
                        return Joiner.on(",").join(params.get(paramName));
                    }
                }

                return defaultValue;
            }

            private String parsePath(String uri) {
                int pathEndPos = uri.indexOf('?');
                if (pathEndPos < 0) {
                    return uri;
                }

                return uri.substring(0, pathEndPos);
            }
        });
    }
}
