package SITS_sprint4;

import static org.junit.jupiter.api.Assertions.*;

import java.io.OutputStream;
import java.net.InetSocketAddress;

import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

import SITS_sprint3.TournamentModel;

class ConnectCommandTest
{
    @Test
    void testExecuteConnectsAndFetchesTournaments() throws Exception
    {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);

        server.createContext("/server/tournaments", exchange ->
        {
            String response = "[1:REG]";
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

            Command command = new ConnectCommand(
                    model,
                    "localhost",
                    String.valueOf(server.getAddress().getPort())
            );

            command.execute();

            assertTrue(model.isConnected());
            assertEquals(1, model.getObservableTournaments().size());
        }
        finally
        {
            server.stop(0);
        }
    }

    @Test
    void testExecuteWithNullModelDoesNothing()
    {
        Command command = new ConnectCommand(null, "localhost", "8081");

        assertDoesNotThrow(command::execute);
    }

    @Test
    void testExecuteWithBlankIpDoesNothing()
    {
        TournamentModel model = new TournamentModel();

        Command command = new ConnectCommand(model, "", "8081");

        command.execute();

        assertFalse(model.isConnected());
    }

    @Test
    void testExecuteWithBlankPortDoesNothing()
    {
        TournamentModel model = new TournamentModel();

        Command command = new ConnectCommand(model, "localhost", "");

        command.execute();

        assertFalse(model.isConnected());
    }
}