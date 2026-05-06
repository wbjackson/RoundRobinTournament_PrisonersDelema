package SITS_sprint4;

import static org.junit.jupiter.api.Assertions.*;

import java.io.OutputStream;
import java.net.InetSocketAddress;

import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

import SITS_sprint3.TournamentModel;

class RefreshTournamentsCommandTest
{
    @Test
    void testExecuteRefreshesTournaments() throws Exception
    {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);

        server.createContext("/server/tournaments", exchange ->
        {
            String response = "[2:ACTIVE]";
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
            model.connectToServer("localhost", String.valueOf(server.getAddress().getPort()));

            Command command = new RefreshTournamentsCommand(model);
            command.execute();

            assertEquals(1, model.getObservableTournaments().size());
            assertEquals(2, model.getObservableTournaments().get(0).getId());
        }
        finally
        {
            server.stop(0);
        }
    }

    @Test
    void testExecuteWithNullModelDoesNothing()
    {
        Command command = new RefreshTournamentsCommand(null);

        assertDoesNotThrow(command::execute);
    }

    @Test
    void testExecuteWhenNotConnectedDoesNothing()
    {
        TournamentModel model = new TournamentModel();

        Command command = new RefreshTournamentsCommand(model);
        command.execute();

        assertEquals(0, model.getObservableTournaments().size());
    }
}