package SITS_sprint4;

import SITS_sprint3.TournamentModel;
import SITS_sprint3.RemoteClientViewerApp;
import SITS_sprint3.TournamentInfo;

public class ViewTournamentCommand implements Command
{
    private final TournamentModel model;
    private final RemoteClientViewerApp app;
    private final TournamentInfo tournament;

    public ViewTournamentCommand(TournamentModel model,
                                 RemoteClientViewerApp app,
                                 TournamentInfo tournament)
    {
        this.model = model;
        this.app = app;
        this.tournament = tournament;
    }

    @Override
    public void execute()
    {
        if (model == null || tournament == null)
        {
            return;
        }

        model.selectTournament(tournament);
        model.clearMoves();
        model.fetchMovesForSelectedTournament();

        if (app != null)
        {
            app.switchToMoveView();
        }
    }
}