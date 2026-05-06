package SITS_sprint3;

import static org.junit.jupiter.api.Assertions.*;

import java.io.OutputStream;
import java.net.InetSocketAddress;

import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

public class TournamentModelTest
{
    @Test
    void testNewModelStartsDisconnected()
    {
        TournamentModel model = new TournamentModel();

        assertEquals("", model.getServerIP());
        assertEquals("", model.getServerPort());
        assertFalse(model.isConnected());
    }

    @Test
    void testConnectToServerStoresIpAndPort()
    {
        TournamentModel model = new TournamentModel();

        model.connectToServer("localhost", "8081");

        assertEquals("localhost", model.getServerIP());
        assertEquals("8081", model.getServerPort());
        assertTrue(model.isConnected());
    }

    @Test
    void testConnectToServerTrimsInput()
    {
        TournamentModel model = new TournamentModel();

        model.connectToServer(" localhost ", " 8081 ");

        assertEquals("localhost", model.getServerIP());
        assertEquals("8081", model.getServerPort());
        assertTrue(model.isConnected());
    }

    @Test
    void testConnectToServerHandlesNullInput()
    {
        TournamentModel model = new TournamentModel();

        model.connectToServer(null, null);

        assertEquals("", model.getServerIP());
        assertEquals("", model.getServerPort());
        assertFalse(model.isConnected());
    }

    @Test
    void testIsConnectedFalseWhenOnlyIpExists()
    {
        TournamentModel model = new TournamentModel();

        model.connectToServer("localhost", "");

        assertFalse(model.isConnected());
    }

    @Test
    void testIsConnectedFalseWhenOnlyPortExists()
    {
        TournamentModel model = new TournamentModel();

        model.connectToServer("", "8081");

        assertFalse(model.isConnected());
    }

    @Test
    void testObservableListsExist()
    {
        TournamentModel model = new TournamentModel();

        assertNotNull(model.getObservableTournaments());
        assertNotNull(model.getObservableMoves());
        assertEquals(0, model.getObservableTournaments().size());
        assertEquals(0, model.getObservableMoves().size());
    }

    @Test
    void testFetchTournamentsWithoutConnectionThrows()
    {
        TournamentModel model = new TournamentModel();

        assertThrows(IllegalStateException.class, model::fetchTournaments);
    }

    @Test
    void testFetchTournamentsSuccess() throws Exception
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
            TournamentModel model = new TournamentModel();
            model.connectToServer("localhost", String.valueOf(port));

            model.fetchTournaments();

            assertEquals(3, model.getObservableTournaments().size());

            assertEquals(1, model.getObservableTournaments().get(0).getId());
            assertTrue(model.getObservableTournaments().get(0).isRegistrationOpen());

            assertEquals(2, model.getObservableTournaments().get(1).getId());
            assertTrue(model.getObservableTournaments().get(1).isActive());

            assertEquals(3, model.getObservableTournaments().get(2).getId());
            assertFalse(model.getObservableTournaments().get(2).isActive());
            assertFalse(model.getObservableTournaments().get(2).isRegistrationOpen());
        }
        finally
        {
            server.stop(0);
        }
    }

    @Test
    void testFetchTournamentsClearsOldListBeforeAddingNewItems() throws Exception
    {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);

        server.createContext("/server/tournaments", exchange ->
        {
            String response = "[5:ACTIVE]";
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
            TournamentModel model = new TournamentModel();
            model.getObservableTournaments().add(new TournamentInfo(99, "Old", true, false));
            model.connectToServer("localhost", String.valueOf(port));

            model.fetchTournaments();

            assertEquals(1, model.getObservableTournaments().size());
            assertEquals(5, model.getObservableTournaments().get(0).getId());
        }
        finally
        {
            server.stop(0);
        }
    }

    @Test
    void testFetchTournamentsBadServerLeavesEmptyList()
    {
        TournamentModel model = new TournamentModel();

        model.connectToServer("localhost", "9999");

        assertDoesNotThrow(model::fetchTournaments);
        assertEquals(0, model.getObservableTournaments().size());
    }

    @Test
    void testSelectTournamentStoresTournament()
    {
        TournamentModel model = new TournamentModel();
        TournamentInfo info = new TournamentInfo(1, "Test", true, false);

        model.selectTournament(info);

        assertSame(info, model.getSelectedTournament());
    }

    @Test
    void testSelectTournamentCanBeClearedWithNull()
    {
        TournamentModel model = new TournamentModel();
        TournamentInfo info = new TournamentInfo(1, "Test", true, false);

        model.selectTournament(info);
        assertSame(info, model.getSelectedTournament());

        model.selectTournament(null);
        assertNull(model.getSelectedTournament());
    }

    @Test
    void testFetchMovesWithoutConnectionDoesNothing()
    {
        TournamentModel model = new TournamentModel();
        model.selectTournament(new TournamentInfo(1, "Test", true, false));

        assertDoesNotThrow(model::fetchMovesForSelectedTournament);
        assertEquals(0, model.getObservableMoves().size());
    }

    @Test
    void testFetchMovesWithoutSelectedTournamentDoesNothing()
    {
        TournamentModel model = new TournamentModel();

        model.connectToServer("localhost", "8081");

        assertDoesNotThrow(model::fetchMovesForSelectedTournament);
        assertEquals(0, model.getObservableMoves().size());
    }

    @Test
    void testFetchMovesForSelectedTournamentSuccess() throws Exception
    {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);

        server.createContext("/server/moves/1", exchange ->
        {
            String response = "[Move A, Move B, Move C]";
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
            TournamentModel model = new TournamentModel();
            model.connectToServer("localhost", String.valueOf(port));
            model.selectTournament(new TournamentInfo(1, "Test", true, false));

            model.fetchMovesForSelectedTournament();

            assertEquals(3, model.getObservableMoves().size());
            assertEquals("Move A", model.getObservableMoves().get(0));
            assertEquals("Move B", model.getObservableMoves().get(1));
            assertEquals("Move C", model.getObservableMoves().get(2));
        }
        finally
        {
            server.stop(0);
        }
    }

    @Test
    void testFetchMovesClearsOldMovesBeforeAddingFetchedMoves() throws Exception
    {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);

        server.createContext("/server/moves/2", exchange ->
        {
            String response = "[Fresh Move]";
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
            TournamentModel model = new TournamentModel();
            model.addMove("Old Move");
            model.connectToServer("localhost", String.valueOf(port));
            model.selectTournament(new TournamentInfo(2, "Test", true, false));

            model.fetchMovesForSelectedTournament();

            assertEquals(1, model.getObservableMoves().size());
            assertEquals("Fresh Move", model.getObservableMoves().get(0));
        }
        finally
        {
            server.stop(0);
        }
    }

    @Test
    void testFetchMovesBadServerLeavesEmptyList()
    {
        TournamentModel model = new TournamentModel();

        model.connectToServer("localhost", "9999");
        model.selectTournament(new TournamentInfo(1, "Test", true, false));

        assertDoesNotThrow(model::fetchMovesForSelectedTournament);
        assertEquals(0, model.getObservableMoves().size());
    }

    @Test
    void testAddMoveAddsTrimmedMove()
    {
        TournamentModel model = new TournamentModel();

        model.addMove("   Round 1: A -> Defect   ");

        assertEquals(1, model.getObservableMoves().size());
        assertEquals("Round 1: A -> Defect", model.getObservableMoves().get(0));
    }

    @Test
    void testAddMoveIgnoresNullEmptyAndBlank()
    {
        TournamentModel model = new TournamentModel();

        model.addMove(null);
        model.addMove("");
        model.addMove("   ");

        assertEquals(0, model.getObservableMoves().size());
    }

    @Test
    void testAddMovePreventsDuplicateMoves()
    {
        TournamentModel model = new TournamentModel();

        model.addMove("Move A");
        model.addMove("Move A");
        model.addMove("  Move A  ");

        assertEquals(1, model.getObservableMoves().size());
    }

    @Test
    void testClearMoves()
    {
        TournamentModel model = new TournamentModel();

        model.addMove("Move 1");
        model.addMove("Move 2");

        model.clearMoves();

        assertEquals(0, model.getObservableMoves().size());
    }

    @Test
    void testStartAndStopViewerClientDoesNotThrow()
    {
        TournamentModel model = new TournamentModel();

        assertDoesNotThrow(model::startViewerClient);
        assertDoesNotThrow(model::stopViewerClient);
    }

    @Test
    void testStopViewerClientWithoutStartingDoesNotThrow()
    {
        TournamentModel model = new TournamentModel();

        assertDoesNotThrow(model::stopViewerClient);
    }
}
