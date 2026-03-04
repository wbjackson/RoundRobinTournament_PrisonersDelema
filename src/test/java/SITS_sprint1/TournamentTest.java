package SITS_sprint1;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

class TournamentTest
{
    @Test
    void testOnlyDefectRobotMove() {
        Robot r = new OnlyDefectRobot("Defector");
        assertEquals("Defect", r.makeMove());
    }

    @Test
    void testPrisonerSameRobotCopiesMove() {
        PrisonerSameRobot r = new PrisonerSameRobot("CopyCat");
        r.rememberOpponentMove("Defect");
        assertEquals("Defect", r.makeMove());
    }

    @Test
    void testOppositeRobot() {
        PrisonerOppositeRobot r = new PrisonerOppositeRobot("Opposite");

        r.rememberOpponentMove("Cooperate");
        assertEquals("Defect", r.makeMove());

        r.rememberOpponentMove("Defect");
        assertEquals("Cooperate", r.makeMove());
    }

    @Test
    void testGameScoreUpdates() {
        Robot r1 = new OnlyDefectRobot("D1");
        Robot r2 = new PrisonerSameRobot("C1");

        AGame game = new PrisonerDelimmaGame(3);

        game.playGame(r1, r2);

        assertTrue(r1.getScore() > r2.getScore());
    }

    @Test
    void testTournamentWinner() {

        ArrayList<Robot> robots = new ArrayList<>();

        robots.add(new OnlyDefectRobot("D1"));
        robots.add(new PrisonerSameRobot("C1"));
        robots.add(new PrisonerOppositeRobot("O1"));

        AGame game = new PrisonerDelimmaGame(5);

        Tournament tournament = new RoundRobinTournament(robots, game);

        Robot winner = tournament.runTournament();

        assertNotNull(winner);
    }


}
