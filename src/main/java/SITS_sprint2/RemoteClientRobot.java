package SITS_sprint2;

import org.springframework.web.client.RestClient;

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

        if (response.equalsIgnoreCase("cooperate"))
        {
            return "Cooperate";
        }

        return "Defect";
    }
    
    @Override
    public String makeMove()
    {
        try
        {
            String url = "http://" + ip + ":" + port + "/move";

            String response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);

            System.out.println(name + " (REMOTE) -> " + response);
            return normalizeResponse(response);
        }
        catch (Exception e)
        {
            System.out.println("Error contacting client: " + e.getMessage());
            return "Defect";
        }
    }

    @Override
    public void rememberOpponentMove(String move)
    {
    }
}