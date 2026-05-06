package SITS_sprint4;

import static org.junit.jupiter.api.Assertions.*;

import java.io.OutputStream;
import java.net.InetSocketAddress;

import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

import SITS_sprint3.TournamentInfo;
import SITS_sprint3.TournamentModel;

class ViewTournamentCommandTest
{
    @Test
    void testExecuteSelectsTournamentAndFetchesMoves() throws Exception
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

        try
        {
            TournamentModel model = new TournamentModel();
            TournamentInfo tournament = new TournamentInfo(1, "Test Tournament", true, false);

            model.connectToServer("localhost", String.valueOf(server.getAddress().getPort()));

            Command command = new ViewTournamentCommand(model, null, tournament);
            command.execute();

            assertSame(tournament, model.getSelectedTournament());
            assertEquals(2, model.getObservableMoves().size());
            assertTrue(model.getObservableMoves().contains("Move A"));
            assertTrue(model.getObservableMoves().contains("Move B"));
        }
        finally
        {
            server.stop(0);
        }
    }

    @Test
    void testExecuteClearsOldMovesBeforeFetching()
    {
        TournamentModel model = new TournamentModel();
        TournamentInfo tournament = new TournamentInfo(1, "Test Tournament", true, false);

        model.addMove("Old Move");

        Command command = new ViewTournamentCommand(model, null, tournament);
        command.execute();

        assertSame(tournament, model.getSelectedTournament());
        assertEquals(0, model.getObservableMoves().size());
    }

    @Test
    void testExecuteWithNullModelDoesNothing()
    {
        TournamentInfo tournament = new TournamentInfo(1, "Test Tournament", true, false);

        Command command = new ViewTournamentCommand(null, null, tournament);

        assertDoesNotThrow(command::execute);
    }

    @Test
    void testExecuteWithNullTournamentDoesNothing()
    {
        TournamentModel model = new TournamentModel();

        Command command = new ViewTournamentCommand(model, null, null);
        command.execute();

        assertNull(model.getSelectedTournament());
    }
}