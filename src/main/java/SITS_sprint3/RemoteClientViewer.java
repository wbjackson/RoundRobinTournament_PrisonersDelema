package SITS_sprint3;

import org.springframework.web.client.RestClient;

import SITS_sprint1.MoveObserver;

public class RemoteClientViewer implements MoveObserver
{
    private final String viewerIP;
    private final String viewerPort;
    private final RestClient restClient;

    public RemoteClientViewer(String viewerIP, String viewerPort)
    {
        this.viewerIP = viewerIP;
        this.viewerPort = viewerPort;
        this.restClient = RestClient.create();
    }

    @Override
    public void updateMove(String moveMessage)
    {
        sendMoveToViewer(moveMessage);
    }

    public void sendMoveToViewer(String move)
    {
        try
        {
            String url = "http://" + viewerIP + ":" + viewerPort + "/receiveMove";

            restClient.post()
                    .uri(url)
                    .body(move)
                    .retrieve()
                    .body(String.class);
        }
        catch (Exception e)
        {
            System.out.println("Error sending move to viewer: " + e.getMessage());
        }
    }

    public String getViewerIP()
    {
        return viewerIP;
    }

    public String getViewerPort()
    {
        return viewerPort;
    }
}