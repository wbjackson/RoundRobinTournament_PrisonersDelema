package SITS_sprint1;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {

        ArrayList<Robot> robots = new ArrayList<>();

        robots.add(new OnlyDefectRobot("Defector"));
        robots.add(new PrisonerSameRobot("CopyCat"));
        robots.add(new PrisonerOppositeRobot("Opposite"));
        robots.add(new RandomRobot("Random"));

        AGame game = new PrisonerDelimmaGame(5);
        
        game.registerMoveObserver(new MoveLoggingSystem());
        game.registerScoreObserver(new ScoreLoggingSystem());

        Tournament tournament = new RoundRobinTournament(robots, game);

        Robot winner = tournament.runTournament();

        //System.out.println();
        System.out.println("\nTournament Winner: " + winner.name);
        System.out.println("Score: " + winner.score);
        System.out.println("Record: " + winner.record);
    }
    
    
    
}
