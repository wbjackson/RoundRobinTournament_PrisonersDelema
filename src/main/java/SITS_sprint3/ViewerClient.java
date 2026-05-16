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
    private final int localPort;
    private TournamentModel model;
    private HttpServer server;
    private final RestClient restClient;

   
    
    
    public ViewerClient()
    {
        this("localhost", 8082);
    }

    ViewerClient(int localPort)
    {
        this("localhost", localPort);
    }

    ViewerClient(String localIP, int localPort)
    {
        this.localIP = localIP;
        this.localPort = localPort;
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
            //System.out.println(localPort);

            server = HttpServer.create(new InetSocketAddress(localPort), 0);
            registerEndpoints();
            server.start();
        }
        catch (IOException e)
        {
            //System.out.println(localPort);
            //System.out.println(e);

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
        if (serverIP == null || serverIP.isBlank() || serverPort == null || serverPort.isBlank())
        {
            return new ArrayList<>();
        }

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
            return new ArrayList<>();
        }
    }

    public List<String> fetchTournamentMoves(String serverIP, String serverPort, int tournamentId)
    {
        if (serverIP == null || serverIP.isBlank() || serverPort == null || serverPort.isBlank())
        {
            return new ArrayList<>();
        }

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
                        "RoundRobin Tournament " + id,
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

        String cleaned = raw.trim();

        if (cleaned.startsWith("["))
        {
            cleaned = cleaned.substring(1);
        }

        if (cleaned.endsWith("]"))
        {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }

        if (cleaned.isBlank())
        {
            return results;
        }

        String[] parts = cleaned.split(", (?=MATCH:|Round |TOURNAMENT|=)");

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

    public String getLocalIP()
    {
        return localIP;
    }

    public int getLocalPort()
    {
        if (server != null)
        {
            return server.getAddress().getPort();
        }

        return localPort;
    }

    public HttpServer getServer()
    {
        return server;
    }
}