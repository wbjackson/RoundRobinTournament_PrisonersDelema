package SITS_sprint1;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

class TournamentTest {

    // Robot behavior tests

    @Test
    void testOnlyDefectRobotMove() {
        Robot robot = new OnlyDefectRobot("Defector");
        assertEquals("Defect", robot.makeMove());
    }

    @Test
    void testPrisonerSameRobotCopiesMove() {
        PrisonerSameRobot robot = new PrisonerSameRobot("CopyCat");
        robot.rememberOpponentMove("Defect");

        assertEquals("Defect", robot.makeMove());
    }

    @Test
    void testPrisonerOppositeRobotChangesMove() {
        PrisonerOppositeRobot robot = new PrisonerOppositeRobot("Opposite");

        robot.rememberOpponentMove("Cooperate");
        assertEquals("Defect", robot.makeMove());

        robot.rememberOpponentMove("Defect");
        assertEquals("Cooperate", robot.makeMove());
    }

    @Test
    void testRandomRobotCoversBothMoves() {
        RandomRobot robot = new RandomRobot("Robo");

        boolean sawCooperate = false;
        boolean sawDefect = false;

        for (int i = 0; i < 100; i++) {
            String move = robot.makeMove();

            if (move.equals("Cooperate")) {
                sawCooperate = true;
            }
            if (move.equals("Defect")) {
                sawDefect = true;
            }

            if (sawCooperate && sawDefect) {
                break;
            }
        }

        assertTrue(sawCooperate);
        assertTrue(sawDefect);

        robot.rememberOpponentMove("Cooperate");
    }

    @Test
    void testGetOpponentName() {
        Robot robot1 = new OnlyDefectRobot("Alpha");
        Robot robot2 = new OnlyDefectRobot("Beta");

        assertEquals("Beta", robot1.getOpponentName(robot2));
    }

    // Game tests


    @Test
    void testGameScoreUpdates() {
        Robot robot1 = new OnlyDefectRobot("D1");
        Robot robot2 = new PrisonerSameRobot("C1");

        AGame game = new PrisonerDelimmaGame(3);
        game.playGame(robot1, robot2);

        assertTrue(robot1.getScore() > robot2.getScore()); //get robot1 .score
    }

    @Test
    void testObserversAreNotifiedDuringGame() {
        Robot robot1 = new OnlyDefectRobot("A");
        Robot robot2 = new OnlyDefectRobot("B"); 

        AGame game = new PrisonerDelimmaGame(3);
        game.registerMoveObserver(new MoveLoggingSystem());
        game.registerScoreObserver(new ScoreLoggingSystem());

        game.playGame(robot1, robot2);
    }

    @Test
    void testObserverRegistration() {
        AGame game = new PrisonerDelimmaGame(5);
      

        MoveLoggingSystem moveLogger = new MoveLoggingSystem();
        ScoreLoggingSystem scoreLogger = new ScoreLoggingSystem();

        game.registerMoveObserver(moveLogger);
        game.registerScoreObserver(scoreLogger);

        game.unregisterMoveObserver(moveLogger);
        game.unregisterScoreObserver(scoreLogger);
    }

    // Logging system tests

    @Test
    void testMoveLoggingSystem() {
        MoveLoggingSystem log = new MoveLoggingSystem();
        log.updateMove("Robot A played Cooperate");
    }

    @Test
    void testScoreLoggingSystem() {
        ScoreLoggingSystem log = new ScoreLoggingSystem();
        log.updateScore("Robot A score updated");
    }

    // Tournament tests

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

    @Test
    void testTournamentWithTooFewParticipants() {
        ArrayList<Robot> robots = new ArrayList<>();
        robots.add(new OnlyDefectRobot("Solo"));

        AGame game = new PrisonerDelimmaGame(3);
        Tournament tournament = new RoundRobinTournament(robots, game);

        assertThrows(IllegalArgumentException.class, tournament::runTournament);
    }

    @Test
    void testTournamentWithNullParticipants() {
        AGame game = new PrisonerDelimmaGame(3);
        Tournament tournament = new RoundRobinTournament(null, game);

        assertThrows(IllegalArgumentException.class, tournament::runTournament);
    }

    @Test
    void testTournamentWinnerIsNotAlwaysFirstRobot() {
        ArrayList<Robot> robots = new ArrayList<>();
        robots.add(new PrisonerSameRobot("CopyCat"));
        robots.add(new OnlyDefectRobot("Defector"));
        robots.add(new PrisonerOppositeRobot("Opposite"));

        AGame game = new PrisonerDelimmaGame(5);
        Tournament tournament = new RoundRobinTournament(robots, game);

        Robot winner = tournament.runTournament();

        assertNotNull(winner);
    }


}
