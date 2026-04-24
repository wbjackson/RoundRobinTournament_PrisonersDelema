package SITS_sprint2;

import java.util.ArrayList;

import SITS_sprint1.MoveLoggingSystem;
import SITS_sprint1.OnlyDefectRobot;
import SITS_sprint1.PrisonerDelimmaGame;
import SITS_sprint1.PrisonerSameRobot;
import SITS_sprint1.Robot;
import SITS_sprint1.ScoreLoggingSystem;

public class RunTournament
{
    public static void main(String[] args)
    {
        TournamentServer server = new TournamentServer();

        System.out.println(server.registerClient("Remote1", "localhost", "8081"));

        ArrayList<Robot> participants = new ArrayList<>();
        participants.add(new OnlyDefectRobot("Defector"));
        participants.add(new PrisonerSameRobot("CopyCat"));
        participants.add(new HumanRobot("HumanPlayer"));
        PrisonerDelimmaGame game = new PrisonerDelimmaGame(3);
        
        game.registerMoveObserver(new MoveLoggingSystem());
        game.registerScoreObserver(new ScoreLoggingSystem());

        int tournamentId = server.createTournament(participants, game);
        System.out.println("Tournament created: " + tournamentId);

        System.out.println(server.addClientToTournament("Remote1", tournamentId));
        System.out.println(server.closeRegistration(tournamentId));

        Robot winner = server.startTournament(tournamentId);
        System.out.println("Winner: " + winner.getName());
    }

    
    
}