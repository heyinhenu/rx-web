package rxweb;

import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import rx.Observable;
import rxweb.engine.server.netty.NettyServer;
import rxweb.http.Status;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

/**
 * @author Sebastien Deleuze
 */
public class RxJavaServerTests {

    private Server server;

    @Before
    public void setup() throws ExecutionException, InterruptedException {
        server = new NettyServer();
        server.start().get();
    }

    @After
    public void tearDown() throws ExecutionException, InterruptedException {
        server.stop().get();
    }

    @Test
    public void writeBuffer() throws IOException {
        server.get("/test", (request, response) -> response.status(Status.OK).content(Observable.just(ByteBuffer.wrap("This is a test!".getBytes(StandardCharsets.UTF_8)))));
        String content = Request.Get("http://localhost:9090/test").execute().returnContent().asString();
        Assert.assertEquals("This is a test!", content);
    }

    @Test
    public void echo() throws IOException {
        server.post("/test", (request, response) -> response.content(request.getContent()));
        String content = Request.Post("http://localhost:9090/test").bodyString("This is a test!", ContentType.TEXT_PLAIN).execute().returnContent().asString();
        Assert.assertEquals("This is a test!", content);
    }

    @Test
    public void echoCapitalizedStream() throws IOException {
        server.post("/test", (request, response) -> response.content(request.getContent().map(data -> ByteBuffer.wrap(new String(data.array(), StandardCharsets.UTF_8).toUpperCase().getBytes(StandardCharsets.UTF_8)))));
        String content = Request.Post("http://localhost:9090/test").bodyString("This is a test!", ContentType.TEXT_PLAIN).execute().returnContent().asString();
        Assert.assertEquals("THIS IS A TEST!", content);
    }

}
