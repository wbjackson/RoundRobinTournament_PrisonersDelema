package SITS_sprint4;

import SITS_sprint3.TournamentModel;

public class RefreshTournamentsCommand implements Command
{
    private final TournamentModel model;

    public RefreshTournamentsCommand(TournamentModel model)
    {
        this.model = model;
    }

    @Override
    public void execute()
    {
        if (model == null)
        {
            return;
        }

        if (!model.isConnected())
        {
            return;
        }

        model.fetchTournaments();
    }
}