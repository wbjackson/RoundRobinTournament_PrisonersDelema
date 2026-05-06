package SITS_sprint4;

import SITS_sprint3.TournamentModel;

public class ConnectCommand implements Command
{
    private final TournamentModel model;
    private final String ip;
    private final String port;

    public ConnectCommand(TournamentModel model, String ip, String port)
    {
        this.model = model;
        this.ip = ip;
        this.port = port;
    }

    @Override
    public void execute()
    {
        if (model == null)
        {
            return;
        }

        if (ip == null || ip.isBlank() || port == null || port.isBlank())
        {
            return;
        }

        model.connectToServer(ip, port);
        model.startViewerClient();
        model.fetchTournaments();
    }
}