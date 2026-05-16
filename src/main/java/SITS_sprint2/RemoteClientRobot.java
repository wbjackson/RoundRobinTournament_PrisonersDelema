package SITS_sprint2;

import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

import SITS_sprint1.Robot;

public class RemoteClientRobot extends Robot
{
    private String ip;
    private String port;
    private RestClient restClient;

    public RemoteClientRobot(String name, String ip, String port)
    {
        super(name);
        this.ip = ip;
        this.port = port;
        this.restClient = RestClient.create();
    }

    String normalizeResponse(String response)
    {
        if (response == null)
        {
            return "Defect";
        }

        response = response.trim();

        if (response.equalsIgnoreCase("Cooperate"))
        {
            return "Cooperate";
        }

        return "Defect";
    }

    private String getEncodedName()
    {
        return UriUtils.encodePathSegment(getName(), StandardCharsets.UTF_8);
    }

    @Override
    public boolean isHumanControlled()
    {
        try
        {
            String url = "http://" + ip + ":" + port + "/robot/isHuman/" + getEncodedName();

            String response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);

            return response != null && response.trim().equalsIgnoreCase("true");
        }
        catch (Exception e)
        {
            return false;
        }
    }

    @Override
    public String makeMove()
    {
        try
        {
            String url = "http://" + ip + ":" + port + "/move/" + getEncodedName();

            String response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);

            System.out.println(getName() + " (REMOTE) -> " + response);
            return normalizeResponse(response);
        }
        catch (Exception e)
        {
            System.out.println("Error contacting client for " + getName() + ": " + e.getMessage());
            return "Defect";
        }
    }

    @Override
    public void rememberOpponentMove(String move)
    {
        try
        {
            String encodedMove = UriUtils.encodePathSegment(move, StandardCharsets.UTF_8);
            String url = "http://" + ip + ":" + port + "/remember/" + getEncodedName() + "/" + encodedMove;

            restClient.post()
                    .uri(url)
                    .retrieve()
                    .body(String.class);
        }
        catch (Exception e)
        {
            System.out.println("Error sending opponent move to client for " + getName() + ": " + e.getMessage());
        }
    }
}