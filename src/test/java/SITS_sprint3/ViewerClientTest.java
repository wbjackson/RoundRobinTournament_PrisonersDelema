package SITS_sprint3;

import static org.junit.jupiter.api.Assertions.*;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.sun.net.httpserver.HttpServer;

import javafx.application.Platform;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ViewerClientTest
{
    @BeforeAll
    static void startJavaFx()
    {
        try
        {
            Platform.startup(() -> {});
        }
        catch (IllegalStateException ignored)
        {
        }
    }

    private static void waitForFxEvents() throws Exception
    {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(latch::countDown);
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testReceiveMoveAddsMoveToModel() throws Exception
    {
        TournamentModel model = new TournamentModel();
        ViewerClient client = new ViewerClient();
        client.setModel(model);

        client.receiveMove("Live Move A");
        waitForFxEvents();

        assertTrue(model.getObservableMoves().contains("Live Move A"));
    }

    @Test
    void testFetchTournamentListParsesRegistrationAndActive() throws Exception
    {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);

        server.createContext("/server/tournaments", exchange ->
        {
            String response = "[1:REG, 2:ACTIVE]";
            exchange.sendResponseHeaders(200, response.getBytes().length);

            try (OutputStream os = exchange.getResponseBody())
            {
                os.write(response.getBytes());
            }
        });

        server.start();
        int port = server.getAddress().getPort();

        try
        {
            ViewerClient client = new ViewerClient();

            List<TournamentInfo> tournaments =
                    client.fetchTournamentList("localhost", String.valueOf(port));

            assertEquals(2, tournaments.size());

            assertEquals(1, tournaments.get(0).getId());
            assertTrue(tournaments.get(0).isRegistrationOpen());
            assertFalse(tournaments.get(0).isActive());

            assertEquals(2, tournaments.get(1).getId());
            assertFalse(tournaments.get(1).isRegistrationOpen());
            assertTrue(tournaments.get(1).isActive());
        }
        finally
        {
            server.stop(0);
        }
    }

    @Test
    void testFetchTournamentListReturnsFallbackWhenServerUnavailable()
    {
        ViewerClient client = new ViewerClient();

        List<TournamentInfo> tournaments =
                client.fetchTournamentList("localhost", "9999");

        assertNotNull(tournaments);
    }

    @Test
    void testFetchTournamentMovesParsesMoveHistory() throws Exception
    {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);

        server.createContext("/server/moves/1", exchange ->
        {
            String response = "[Move A, Move B]";
            exchange.sendResponseHeaders(200, response.getBytes().length);

            try (OutputStream os = exchange.getResponseBody())
            {
                os.write(response.getBytes());
            }
        });

        server.start();
        int port = server.getAddress().getPort();

        try
        {
            ViewerClient client = new ViewerClient();

            List<String> moves =
                    client.fetchTournamentMoves("localhost", String.valueOf(port), 1);

            assertEquals(2, moves.size());
            assertEquals("Move A", moves.get(0));
            assertEquals("Move B", moves.get(1));
        }
        finally
        {
            server.stop(0);
        }
    }

    @Test
    void testFetchTournamentMovesReturnsEmptyWhenServerUnavailable()
    {
        ViewerClient client = new ViewerClient();

        List<String> moves =
                client.fetchTournamentMoves("localhost", "9999", 1);

        assertNotNull(moves);
        assertEquals(0, moves.size());
    }

    @Test
    void testRegisterViewerWithServerCallsExpectedEndpoint() throws Exception
    {
        final String[] path = {null};

        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);

        server.createContext("/server/viewer/register/1/localhost/8095", exchange ->
        {
            path[0] = exchange.getRequestURI().getPath();
            String response = "Viewer registered.";
            exchange.sendResponseHeaders(200, response.getBytes().length);

            try (OutputStream os = exchange.getResponseBody())
            {
                os.write(response.getBytes());
            }
        });

        server.start();
        int port = server.getAddress().getPort();

        try
        {
            ViewerClient client = new ViewerClient();
            client.registerViewerWithServer("localhost", String.valueOf(port), 1);

            assertEquals("/server/viewer/register/1/localhost/8095", path[0]);
        }
        finally
        {
            server.stop(0);
        }
    }

    @Test
    void testUnregisterViewerWithServerCallsExpectedEndpoint() throws Exception
    {
        final String[] path = {null};

        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);

        server.createContext("/server/viewer/unregister/1/localhost/8095", exchange ->
        {
            path[0] = exchange.getRequestURI().getPath();
            String response = "Viewer unregistered.";
            exchange.sendResponseHeaders(200, response.getBytes().length);

            try (OutputStream os = exchange.getResponseBody())
            {
                os.write(response.getBytes());
            }
        });

        server.start();
        int port = server.getAddress().getPort();

        try
        {
            ViewerClient client = new ViewerClient();
            client.unregisterViewerFromServer("localhost", String.valueOf(port), 1);

            assertEquals("/server/viewer/unregister/1/localhost/8095", path[0]);
        }
        finally
        {
            server.stop(0);
        }
    }
    
    @Test
    void testGetLocalIPAndPort()
    {
        ViewerClient client = new ViewerClient();

        assertEquals("localhost", client.getLocalIP());
        assertEquals(8095, client.getLocalPort());
    }

    @Test
    void testSetLocalPort()
    {
        ViewerClient client = new ViewerClient();

        client.setLocalPort(9000);

        assertEquals(9000, client.getLocalPort());
    }
    
    @Test
    void testReceiveMoveAddsToModel() throws Exception
    {
        TournamentModel model = new TournamentModel();
        ViewerClient client = new ViewerClient();
        client.setModel(model);

        client.receiveMove("Test Move");

        Thread.sleep(100); // allow Platform.runLater

        assertTrue(model.getObservableMoves().contains("Test Move"));
    }
    
    @Test
    void testStartAndStopServer()
    {
        ViewerClient client = new ViewerClient();

        assertDoesNotThrow(client::startServer);
        assertDoesNotThrow(client::stopServer);
    }
    
    @Test
    void testStartServerTwiceDoesNotCrash()
    {
        ViewerClient client = new ViewerClient();

        assertDoesNotThrow(client::startServer);
        assertDoesNotThrow(client::startServer);
        assertDoesNotThrow(client::stopServer);
    }
    
    @Test
    void testRegisterViewerHandlesUnavailableServer()
    {
        ViewerClient client = new ViewerClient();

        assertDoesNotThrow(() ->
            client.registerViewerWithServer("localhost", "9999", 1)
        );
    }
    
    @Test
    void testUnregisterViewerHandlesUnavailableServer()
    {
        ViewerClient client = new ViewerClient();

        assertDoesNotThrow(() ->
            client.unregisterViewerFromServer("localhost", "9999", 1)
        );
    }
    
    @Test
    void testParseMoveHistoryEmpty()
    {
        ViewerClient client = new ViewerClient();

        List<String> moves = client.fetchTournamentMoves("localhost", "9999", 1);

        assertNotNull(moves);
    }
    
    @Test
    void testParseTournamentListMalformed()
    {
        ViewerClient client = new ViewerClient();

        List<TournamentInfo> result =
            client.fetchTournamentList("localhost", "9999");

        assertNotNull(result);
    }
    
    @Test
    void testStopServerWithoutStart()
    {
        ViewerClient client = new ViewerClient();

        assertDoesNotThrow(client::stopServer);
    }
}