package SITS_sprint4;

import SITS_sprint3.TournamentModel;
import SITS_sprint3.RemoteClientViewerApp;

public class ExitMoveViewCommand implements Command
{
    private final TournamentModel model;
    private final RemoteClientViewerApp app;

    public ExitMoveViewCommand(TournamentModel model, RemoteClientViewerApp app)
    {
        this.model = model;
        this.app = app;
    }

    @Override
    public void execute()
    {
        if (model != null && model.isConnected())
        {
            model.fetchTournaments();
        }

        if (app != null)
        {
            app.switchToTournamentsView();
        }
    }
}