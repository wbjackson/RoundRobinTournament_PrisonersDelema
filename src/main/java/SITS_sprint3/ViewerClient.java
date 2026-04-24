package SITS_sprint3;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.client.RestClient;

import com.sun.net.httpserver.HttpServer;

import javafx.application.Platform;

public class ViewerClient
{
    private final String localIP;
    private int localPort;
    private TournamentModel model;
    private HttpServer server;
    private final RestClient restClient;

    public ViewerClient()
    {
        this.localIP = "localhost";
        this.localPort = 8095;
        this.restClient = RestClient.create();
    }

    public void setModel(TournamentModel model)
    {
        this.model = model;
    }

    public void startServer()
    {
        if (server != null)
        {
            return;
        }

        try
        {
            server = HttpServer.create(new InetSocketAddress(localPort), 0);
            registerEndpoints();
            server.start();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to start ViewerClient server.", e);
        }
    }

    public void stopServer()
    {
        if (server != null)
        {
            server.stop(0);
            server = null;
        }
    }

    private void registerEndpoints()
    {
        server.createContext("/receiveMove", exchange ->
        {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod()))
            {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String move = new String(exchange.getRequestBody().readAllBytes());
            receiveMove(move);

            String response = "Move received";
            exchange.sendResponseHeaders(200, response.getBytes().length);

            try (OutputStream os = exchange.getResponseBody())
            {
                os.write(response.getBytes());
            }
        });
    }

    public void receiveMove(String move)
    {
        if (model != null)
        {
            Platform.runLater(() -> model.addMove(move));
        }
    }

    public List<TournamentInfo> fetchTournamentList(String serverIP, String serverPort)
    {
        String baseUrl = "http://" + serverIP + ":" + serverPort;

        try
        {
            String raw = restClient.get()
                    .uri(baseUrl + "/server/tournaments")
                    .retrieve()
                    .body(String.class);

            return parseTournamentStatusList(raw);
        }
        catch (Exception e)
        {
            System.out.println("Fetch tournaments fallback used: " + e.getMessage());

            List<TournamentInfo> test = new ArrayList<>();
            test.add(new TournamentInfo(1, "Test Tournament 1", true, false));
            test.add(new TournamentInfo(2, "Test Tournament 2", false, true));

            return test;
        }
    }

    public List<String> fetchTournamentMoves(String serverIP, String serverPort, int tournamentId)
    {
        String baseUrl = "http://" + serverIP + ":" + serverPort;

        try
        {
            String raw = restClient.get()
                    .uri(baseUrl + "/server/moves/" + tournamentId)
                    .retrieve()
                    .body(String.class);

            return parseMoveHistory(raw);
        }
        catch (Exception e)
        {
            System.out.println("Fetch moves failed: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<TournamentInfo> parseTournamentStatusList(String raw)
    {
        List<TournamentInfo> results = new ArrayList<>();

        if (raw == null)
        {
            return results;
        }

        String cleaned = raw.replace("[", "").replace("]", "").trim();

        if (cleaned.isBlank())
        {
            return results;
        }

        String[] parts = cleaned.split(",");

        for (String part : parts)
        {
            String trimmed = part.trim();

            if (trimmed.isBlank())
            {
                continue;
            }

            String[] pieces = trimmed.split(":");

            if (pieces.length != 2)
            {
                continue;
            }

            try
            {
                int id = Integer.parseInt(pieces[0].trim());
                String status = pieces[1].trim();

                boolean active = status.equalsIgnoreCase("ACTIVE");
                boolean registrationOpen = status.equalsIgnoreCase("REG");

                results.add(new TournamentInfo(
                        id,
                        "RoundRobin Tournament" + id,
                        active,
                        registrationOpen
                ));
            }
            catch (NumberFormatException ignored)
            {
            }
        }

        return results;
    }

    private List<String> parseMoveHistory(String raw)
    {
        List<String> results = new ArrayList<>();

        if (raw == null)
        {
            return results;
        }

        String cleaned = raw.replace("[", "").replace("]", "").trim();

        if (cleaned.isBlank())
        {
            return results;
        }

        String[] parts = cleaned.split(",");

        for (String part : parts)
        {
            String trimmed = part.trim();

            if (!trimmed.isBlank())
            {
                results.add(trimmed);
            }
        }

        return results;
    }

    public void registerViewerWithServer(String serverIP, String serverPort, int tournamentId)
    {
        String baseUrl = "http://" + serverIP + ":" + serverPort;

        try
        {
            restClient.get()
                    .uri(baseUrl + "/server/viewer/register/"
                            + tournamentId + "/" + localIP + "/" + localPort)
                    .retrieve()
                    .body(String.class);
        }
        catch (Exception e)
        {
            System.out.println("Viewer registration failed: " + e.getMessage());
        }
    }

    public void unregisterViewerFromServer(String serverIP, String serverPort, int tournamentId)
    {
        String baseUrl = "http://" + serverIP + ":" + serverPort;

        try
        {
            restClient.get()
                    .uri(baseUrl + "/server/viewer/unregister/"
                            + tournamentId + "/" + localIP + "/" + localPort)
                    .retrieve()
                    .body(String.class);
        }
        catch (Exception e)
        {
            System.out.println("Viewer unregistration failed: " + e.getMessage());
        }
    }

    public String getLocalIP()
    {
        return localIP;
    }

    public int getLocalPort()
    {
        return localPort;
    }

    public void setLocalPort(int localPort)
    {
        this.localPort = localPort;
    }
}