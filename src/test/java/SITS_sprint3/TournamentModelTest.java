package SITS_sprint3;

import static org.junit.jupiter.api.Assertions.*;

import java.net.InetSocketAddress;

import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

public class TournamentModelTest
{
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
    }

    @Test
    void testIsConnectedFalseWithEmptyValues()
    {
        TournamentModel model = new TournamentModel();

        assertFalse(model.isConnected());
    }

    @Test
    void testFetchTournamentsWithoutConnectionThrows()
    {
        TournamentModel model = new TournamentModel();

        assertThrows(IllegalStateException.class, model::fetchTournaments);
    }

    @Test
    void testSelectTournamentStoresTournament()
    {
        TournamentModel model = new TournamentModel();
        TournamentInfo info = new TournamentInfo(1, "Test", true, false);

        model.selectTournament(info);

        assertEquals(info, model.getSelectedTournament());
    }

    @Test
    void testAddMoveAddsMove()
    {
        TournamentModel model = new TournamentModel();

        model.addMove("Round 1: A -> Defect");

        assertTrue(model.getObservableMoves().contains("Round 1: A -> Defect"));
    }

    @Test
    void testAddMoveIgnoresNullAndBlank()
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

        assertEquals(1, model.getObservableMoves().size());
    }

    @Test
    void testRegisterForUpdatesWithoutSelectedTournamentThrows()
    {
        TournamentModel model = new TournamentModel();
        model.connectToServer("localhost", "8081");

        assertThrows(IllegalStateException.class, model::registerForUpdates);
    }

    @Test
    void testUnregisterFromUpdatesWithoutSelectedTournamentDoesNotThrow()
    {
        TournamentModel model = new TournamentModel();
        model.connectToServer("localhost", "8081");

        assertDoesNotThrow(model::unregisterFromUpdates);
    }
    
    @Test
    void testConnectAndGetters()
    {
        TournamentModel model = new TournamentModel();

        model.connectToServer("localhost", "8081");

        assertEquals("localhost", model.getServerIP());
        assertEquals("8081", model.getServerPort());
        assertTrue(model.isConnected());
    }
    
    @Test
    void testClearMoves1()
    {
        TournamentModel model = new TournamentModel();

        model.addMove("Move1");
        model.clearMoves();

        assertEquals(0, model.getObservableMoves().size());
    }
    
    @Test
    void testSelectTournament()
    {
        TournamentModel model = new TournamentModel();

        TournamentInfo t = new TournamentInfo(1, "Test", true, false);
        model.selectTournament(t);

        assertEquals(t, model.getSelectedTournament());
    }
    
    @Test
    void testObservableTournamentsListExists()
    {
        TournamentModel model = new TournamentModel();

        assertNotNull(model.getObservableTournaments());
    }
    
    @Test
    void testFetchTournamentsSuccess() throws Exception
    {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);

        server.createContext("/server/tournaments", exchange ->
        {
            String response = "[1:REG, 2:ACTIVE]";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        });

        server.start();
        int port = server.getAddress().getPort();

        try
        {
            TournamentModel model = new TournamentModel();
            model.connectToServer("localhost", String.valueOf(port));

            model.fetchTournaments();

            assertEquals(2, model.getObservableTournaments().size());
        }
        finally
        {
            server.stop(0);
        }
    }
    
    @Test
    void testFetchMovesForSelectedTournament() throws Exception
    {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);

        server.createContext("/server/moves/1", exchange ->
        {
            String response = "[Move A, Move B]";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        });

        server.start();
        int port = server.getAddress().getPort();

        try
        {
            TournamentModel model = new TournamentModel();
            model.connectToServer("localhost", String.valueOf(port));

            model.selectTournament(new TournamentInfo(1, "Test", true, false));
            model.fetchMovesForSelectedTournament();

            assertEquals(2, model.getObservableMoves().size());
        }
        finally
        {
            server.stop(0);
        }
    }
    
    @Test
    void testRegisterForUpdatesDoesNotCrash()
    {
        TournamentModel model = new TournamentModel();
        model.connectToServer("localhost", "8081");

        model.selectTournament(new TournamentInfo(1, "Test", true, false));

        assertDoesNotThrow(model::registerForUpdates);
    }
    
    @Test
    void testUnregisterDoesNotCrash()
    {
        TournamentModel model = new TournamentModel();
        model.connectToServer("localhost", "8081");

        model.selectTournament(new TournamentInfo(1, "Test", true, false));

        assertDoesNotThrow(model::unregisterFromUpdates);
    }
    
    @Test
    void testStartAndStopViewerClient()
    {
        TournamentModel model = new TournamentModel();

        assertDoesNotThrow(model::startViewerClient);
        assertDoesNotThrow(model::stopViewerClient);
    }
    
    @Test
    void testGetName()
    {
        TournamentInfo info = new TournamentInfo(1, "TestName", true, false);
        assertEquals("TestName", info.getName());
    }

    @Test
    void testIsActiveAndRegistration()
    {
        TournamentInfo info = new TournamentInfo(1, "Test", true, false);

        assertTrue(info.isActive());
        assertFalse(info.isRegistrationOpen());
    }

    @Test
    void testStatusTextRegistration()
    {
        TournamentInfo info = new TournamentInfo(1, "Test", false, true);
        assertEquals("Registration", info.getStatusText());
    }

    @Test
    void testStatusTextActive()
    {
        TournamentInfo info = new TournamentInfo(1, "Test", true, false);
        assertEquals("Active", info.getStatusText());
    }

    @Test
    void testToStringContainsData()
    {
        TournamentInfo info = new TournamentInfo(2, "RR", true, false);

        String result = info.toString();

        assertTrue(result.contains("2"));
        assertTrue(result.contains("RR"));
    }
    
    
}

