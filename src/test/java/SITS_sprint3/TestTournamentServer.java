package SITS_sprint3;

import java.util.ArrayList;

import SITS_sprint1.AGame;
import SITS_sprint1.MoveLoggingSystem;
import SITS_sprint1.OnlyDefectRobot;
import SITS_sprint1.PrisonerDelimmaGame;
import SITS_sprint1.PrisonerSameRobot;
import SITS_sprint1.Robot;
import SITS_sprint1.ScoreLoggingSystem;
import SITS_sprint2.TournamentServer;

public class TestTournamentServer
{
    private final TournamentServer server;

    public TestTournamentServer()
    {
        this.server = new TournamentServer();
    }

    public TournamentServer getServer()
    {
        return server;
    }

    public int createTestTournament()
    {
        ArrayList<Robot> participants = new ArrayList<>();
        participants.add(new OnlyDefectRobot("Defector"));
        participants.add(new PrisonerSameRobot("CopyCat"));

        AGame game = new PrisonerDelimmaGame(3);
        game.registerMoveObserver(new MoveLoggingSystem());
        game.registerScoreObserver(new ScoreLoggingSystem());

        return server.createTournament(participants, game);
    }

    public void startTournament(int tournamentId)
    {
        server.closeRegistration(tournamentId);
        server.startTournament(tournamentId);
    }
}