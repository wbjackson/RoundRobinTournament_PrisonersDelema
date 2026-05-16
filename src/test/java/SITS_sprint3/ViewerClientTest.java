package SITS_sprint3;

import static org.junit.jupiter.api.Assertions.*;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.sun.net.httpserver.HttpServer;

import javafx.application.Platform;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ViewerClientTest
{
    private ViewerClient activeClient;

    @AfterEach
    void cleanupViewerClient()
    {
        if (activeClient != null)
        {
            activeClient.stopServer();
            activeClient = null;
        }
    }

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

    private static void waitForFx() throws Exception
    {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(latch::countDown);
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testReceiveMoveAddsToModel() throws Exception
    {
        TournamentModel model = new TournamentModel();
        ViewerClient client = new ViewerClient();
        client.setModel(model);

        client.receiveMove("Live Move");

        waitForFx();

        assertEquals(1, model.getObservableMoves().size());
        assertEquals("Live Move", model.getObservableMoves().get(0));
    }

    @Test
    void testFetchTournamentListParsesCorrectly() throws Exception
    {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);

        server.createContext("/server/tournaments", exchange ->
        {
            String response = "[1:REG, 2:ACTIVE, 3:FINISHED]";
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

            List<TournamentInfo> list =
                    client.fetchTournamentList("localhost", String.valueOf(port));

            assertEquals(3, list.size());

            assertTrue(list.get(0).isRegistrationOpen());
            assertTrue(list.get(1).isActive());
            assertFalse(list.get(2).isActive());
            assertFalse(list.get(2).isRegistrationOpen());
        }
        finally
        {
            server.stop(0);
        }
    }

    @Test
    void testFetchTournamentListHandlesEmptyResponse() throws Exception
    {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);

        server.createContext("/server/tournaments", exchange ->
        {
            String response = "[]";
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

            List<TournamentInfo> list =
                    client.fetchTournamentList("localhost", String.valueOf(port));

            assertEquals(0, list.size());
        }
        finally
        {
            server.stop(0);
        }
    }

    @Test
    void testFetchTournamentListMalformedDataIgnored() throws Exception
    {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);

        server.createContext("/server/tournaments", exchange ->
        {
            String response = "[bad data, 2:ACTIVE]";
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

            List<TournamentInfo> list =
                    client.fetchTournamentList("localhost", String.valueOf(port));

            assertEquals(1, list.size());
            assertEquals(2, list.get(0).getId());
            assertTrue(list.get(0).isActive());
        }
        finally
        {
            server.stop(0);
        }
    }

    @Test
    void testFetchTournamentListServerUnavailable()
    {
        ViewerClient client = new ViewerClient();

        List<TournamentInfo> list =
                client.fetchTournamentList("localhost", "9999");

        assertNotNull(list);
        assertEquals(0, list.size());
    }

    @Test
    void testFetchMovesParsesCorrectly() throws Exception
    {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);

        server.createContext("/server/moves/1", exchange ->
        {
            String response = "[MATCH: A vs B, Round 1: A -> Defect, B -> Cooperate]";
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
            assertEquals("MATCH: A vs B", moves.get(0));
            assertEquals("Round 1: A -> Defect, B -> Cooperate", moves.get(1));
        }
        finally
        {
            server.stop(0);
        }
    }

    @Test
    void testFetchMovesHandlesEmptyResponse() throws Exception
    {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);

        server.createContext("/server/moves/1", exchange ->
        {
            String response = "[]";
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

            assertEquals(0, moves.size());
        }
        finally
        {
            server.stop(0);
        }
    }

    @Test
    void testFetchMovesServerUnavailable()
    {
        ViewerClient client = new ViewerClient();

        List<String> moves =
                client.fetchTournamentMoves("localhost", "9999", 1);

        assertNotNull(moves);
        assertEquals(0, moves.size());
    }

    @Test
    void testStartAndStopServerDoesNotThrow()
    {
        activeClient = new ViewerClient(0);

        assertDoesNotThrow(activeClient::startServer);
        assertNotNull(activeClient.getServer());
        assertTrue(activeClient.getLocalPort() > 0);

        assertDoesNotThrow(activeClient::stopServer);
        assertNull(activeClient.getServer());

        activeClient = null;
    }

    @Test
    void testStopWithoutStartDoesNotThrow()
    {
        ViewerClient client = new ViewerClient();

        assertDoesNotThrow(client::stopServer);
    }

    @Test
    void testStartServerTwiceDoesNotThrow()
    {
        activeClient = new ViewerClient(0);

        activeClient.startServer();

        assertNotNull(activeClient.getServer());
        assertTrue(activeClient.getLocalPort() > 0);
        assertDoesNotThrow(activeClient::startServer);
    }

    @Test
    void testReceiveMoveEndpointRejectsGet() throws Exception
    {
        activeClient = new ViewerClient(0);

        activeClient.startServer();

        URL url = new URL("http://localhost:" + activeClient.getLocalPort() + "/receiveMove");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");

        assertEquals(405, connection.getResponseCode());

        connection.disconnect();
    }

    @Test
    void testReceiveMoveEndpointAcceptsPost() throws Exception
    {
        TournamentModel model = new TournamentModel();

        activeClient = new ViewerClient(0);
        activeClient.setModel(model);
        activeClient.startServer();

        URL url = new URL("http://localhost:" + activeClient.getLocalPort() + "/receiveMove");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream())
        {
            os.write("Endpoint Move".getBytes());
        }

        assertEquals(200, connection.getResponseCode());

        waitForFx();

        assertTrue(model.getObservableMoves().contains("Endpoint Move"));

        connection.disconnect();
    }

    @Test
    void testFetchTournamentListWithInvalidInput()
    {
        ViewerClient client = new ViewerClient();

        assertTrue(client.fetchTournamentList(null, "8081").isEmpty());
        assertTrue(client.fetchTournamentList("", "8081").isEmpty());
        assertTrue(client.fetchTournamentList("localhost", null).isEmpty());
        assertTrue(client.fetchTournamentList("localhost", "").isEmpty());
    }

    @Test
    void testFetchTournamentMovesWithInvalidInput()
    {
        ViewerClient client = new ViewerClient();

        assertTrue(client.fetchTournamentMoves(null, "8081", 1).isEmpty());
        assertTrue(client.fetchTournamentMoves("", "8081", 1).isEmpty());
        assertTrue(client.fetchTournamentMoves("localhost", null, 1).isEmpty());
        assertTrue(client.fetchTournamentMoves("localhost", "", 1).isEmpty());
    }

    @Test
    void testParseTournamentStatusListNull()
    {
        ViewerClient client = new ViewerClient();

        assertTrue(client.fetchTournamentList("invalid", "invalid").isEmpty());
    }

    @Test
    void testParseMoveHistoryNull()
    {
        ViewerClient client = new ViewerClient();

        assertTrue(client.fetchTournamentMoves("invalid", "invalid", 1).isEmpty());
    }

    @Test
    void testFetchMovesKeepsFullRoundLinesWithCommasTogether() throws Exception
    {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);

        server.createContext("/server/moves/1", exchange ->
        {
            String response = "[MATCH: A vs B, Round 1: A -> Defect, B -> Cooperate, Round 2: A -> Defect, B -> Defect, TOURNAMENT WINNER: A | Record: 10 | Score: 5]";
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

            assertEquals(4, moves.size());
            assertEquals("MATCH: A vs B", moves.get(0));
            assertEquals("Round 1: A -> Defect, B -> Cooperate", moves.get(1));
            assertEquals("Round 2: A -> Defect, B -> Defect", moves.get(2));
            assertEquals("TOURNAMENT WINNER: A | Record: 10 | Score: 5", moves.get(3));
        }
        finally
        {
            server.stop(0);
        }
    }
}