package SITS_sprint3;

import static org.junit.jupiter.api.Assertions.*;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.Test;

public class RemoteClientViewerTest
{
    @Test
    void testUpdateMoveSendsMoveToViewer() throws Exception
    {
        final String[] receivedMove = {null};
        CountDownLatch latch = new CountDownLatch(1);

        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);

        server.createContext("/receiveMove", exchange ->
        {
            receivedMove[0] = new String(exchange.getRequestBody().readAllBytes());
            String response = "Move received";
            exchange.sendResponseHeaders(200, response.getBytes().length);

            try (OutputStream os = exchange.getResponseBody())
            {
                os.write(response.getBytes());
            }

            latch.countDown();
        });

        server.start();
        int port = server.getAddress().getPort();

        try
        {
            RemoteClientViewer viewer =
                    new RemoteClientViewer("localhost", String.valueOf(port));

            viewer.updateMove("Round 1: A -> Defect");

            assertTrue(latch.await(2, TimeUnit.SECONDS));
            assertEquals("Round 1: A -> Defect", receivedMove[0]);
        }
        finally
        {
            server.stop(0);
        }
    }

    @Test
    void testSendMoveToViewerDoesNotThrowWhenViewerUnavailable()
    {
        RemoteClientViewer viewer =
                new RemoteClientViewer("localhost", "9999");

        assertDoesNotThrow(() -> viewer.sendMoveToViewer("Move"));
    }

    @Test
    void testGetters()
    {
        RemoteClientViewer viewer =
                new RemoteClientViewer("localhost", "8095");

        assertEquals("localhost", viewer.getViewerIP());
        assertEquals("8095", viewer.getViewerPort());
    }
}
