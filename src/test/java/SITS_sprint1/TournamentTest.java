package SITS_sprint1;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TournamentTest
{
    private static final Path LOG_PATH = Paths.get(
        System.getProperty("user.dir"),
        "TournamentsResults.txt"
    );

    @AfterEach
    void cleanup() throws IOException
    {
        if (Files.exists(LOG_PATH)) {
            Files.delete(LOG_PATH);
        }
    }

    private static class FixedMoveRobot extends Robot
    {
        private final String move;
        private String remembered;

        public FixedMoveRobot(String name, String move)
        {
            super(name);
            this.move = move;
        }

        @Override
        public String makeMove()
        {
            return move;
        }

        @Override
        public void rememberOpponentMove(String move)
        {
            remembered = move;
        }

        public String getRemembered()
        {
            return remembered;
        }
    }

    // =========================
    // ROBOT TESTS
    // =========================

    @Test
    void testOnlyDefectRobotMove()
    {
        Robot robot = new OnlyDefectRobot("Defector");
        assertEquals("Defect", robot.makeMove());
    }

    @Test
    void testPrisonerSameRobotCopiesMove()
    {
        PrisonerSameRobot robot = new PrisonerSameRobot("CopyCat");
        robot.rememberOpponentMove("Defect");
        assertEquals("Defect", robot.makeMove());
    }

    @Test
    void testPrisonerOppositeRobotChangesMove()
    {
        PrisonerOppositeRobot robot = new PrisonerOppositeRobot("Opposite");

        robot.rememberOpponentMove("Cooperate");
        assertEquals("Defect", robot.makeMove());

        robot.rememberOpponentMove("Defect");
        assertEquals("Cooperate", robot.makeMove());
    }

    @Test
    void testRandomRobotCoversBothMoves()
    {
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
        }

        assertTrue(sawCooperate);
        assertTrue(sawDefect);
    }

    @Test
    void testRandomRobotRememberOpponentMoveDoesNothing()
    {
        RandomRobot robot = new RandomRobot("R");
        assertDoesNotThrow(() -> robot.rememberOpponentMove("Cooperate"));
    }

    @Test
    void testGetOpponentName()
    {
        Robot robot1 = new OnlyDefectRobot("Alpha");
        Robot robot2 = new OnlyDefectRobot("Beta");

        assertEquals("Beta", robot1.getOpponentName(robot2));
    }

    @Test
    void testGetRecord()
    {
        Robot robot = new Robot("TestRobot")
        {
            @Override
            public String makeMove()
            {
                return "Cooperate";
            }

            @Override
            public void rememberOpponentMove(String move)
            {
            }
        };

        assertEquals(0, robot.getRecord());

        robot.addRecord(5);
        robot.addRecord(10);

        assertEquals(15, robot.getRecord());
    }

    // =========================
    // GAME LOGIC TESTS
    // =========================

    @Test
    void testGameScoreUpdates()
    {
        Robot robot1 = new OnlyDefectRobot("D1");
        Robot robot2 = new PrisonerSameRobot("C1");

        AGame game = new PrisonerDelimmaGame(3);
        game.playGame(robot1, robot2);

        assertTrue(robot1.getScore() > robot2.getScore());
    }

    @Test
    void testGameReturnsHigherScoringRobot()
    {
        Robot r1 = new PrisonerSameRobot("CopyCat");
        Robot r2 = new OnlyDefectRobot("Defector");

        AGame game = new PrisonerDelimmaGame(3);

        Robot winner = game.playGame(r1, r2);

        assertEquals("Defector", winner.getName());
    }

    @Test
    void testApplyPayoffCooperateCooperate()
    {
        PrisonerDelimmaGame game = new PrisonerDelimmaGame(1);

        Robot p1 = new FixedMoveRobot("A", "Cooperate");
        Robot p2 = new FixedMoveRobot("B", "Cooperate");

        game.playGame(p1, p2);

        assertEquals(3, p1.getScore());
        assertEquals(3, p2.getScore());
        assertEquals("Cooperate", ((FixedMoveRobot) p1).getRemembered());
        assertEquals("Cooperate", ((FixedMoveRobot) p2).getRemembered());
    }

    @Test
    void testApplyPayoffDefectDefect()
    {
        PrisonerDelimmaGame game = new PrisonerDelimmaGame(1);

        Robot p1 = new FixedMoveRobot("A", "Defect");
        Robot p2 = new FixedMoveRobot("B", "Defect");

        game.playGame(p1, p2);

        assertEquals(1, p1.getScore());
        assertEquals(1, p2.getScore());
    }

    @Test
    void testApplyPayoffDefectCooperate()
    {
        PrisonerDelimmaGame game = new PrisonerDelimmaGame(1);

        Robot p1 = new FixedMoveRobot("A", "Defect");
        Robot p2 = new FixedMoveRobot("B", "Cooperate");

        game.playGame(p1, p2);

        assertEquals(5, p1.getScore());
        assertEquals(0, p2.getScore());
    }

    @Test
    void testApplyPayoffCooperateDefect()
    {
        PrisonerDelimmaGame game = new PrisonerDelimmaGame(1);

        Robot p1 = new FixedMoveRobot("A", "Cooperate");
        Robot p2 = new FixedMoveRobot("B", "Defect");

        game.playGame(p1, p2);

        assertEquals(0, p1.getScore());
        assertEquals(5, p2.getScore());
    }

    // =========================
    // OBSERVER / AGame TESTS
    // =========================

    @Test
    void testObserversAreNotifiedDuringGame()
    {
        class TestMoveObserver implements MoveObserver {
            int count = 0;
            @Override
            public void updateMove(String m) { count++; }
        }

        class TestScoreObserver implements ScoreObserver {
            int count = 0;
            @Override
            public void updateScore(String s) { count++; }
        }

        Robot robot1 = new OnlyDefectRobot("A");
        Robot robot2 = new OnlyDefectRobot("B");

        AGame game = new PrisonerDelimmaGame(3);
        TestMoveObserver moveObs = new TestMoveObserver();
        TestScoreObserver scoreObs = new TestScoreObserver();

        game.registerMoveObserver(moveObs);
        game.registerScoreObserver(scoreObs);

        game.playGame(robot1, robot2);

        assertTrue(moveObs.count > 0);
        assertTrue(scoreObs.count > 0);
    }

    @Test
    void testUnregisterMoveObserverOnly()
    {
        class TestMoveObserver implements MoveObserver {
            int count = 0;
            @Override
            public void updateMove(String m) { count++; }
        }

        AGame game = new PrisonerDelimmaGame(3);

        TestMoveObserver moveObs = new TestMoveObserver();

        game.registerMoveObserver(moveObs);
        game.unregisterMoveObserver(moveObs);

        Robot r1 = new OnlyDefectRobot("A");
        Robot r2 = new OnlyDefectRobot("B");

        game.playGame(r1, r2);

        assertEquals(0, moveObs.count);
    }

    @Test
    void testUnregisterScoreObserverOnly()
    {
        class TestScoreObserver implements ScoreObserver {
            int count = 0;
            @Override
            public void updateScore(String s) { count++; }
        }

        AGame game = new PrisonerDelimmaGame(2);

        TestScoreObserver scoreObs = new TestScoreObserver();

        game.registerScoreObserver(scoreObs);
        game.unregisterScoreObserver(scoreObs);

        Robot r1 = new OnlyDefectRobot("A");
        Robot r2 = new OnlyDefectRobot("B");

        game.playGame(r1, r2);

        assertEquals(0, scoreObs.count);
    }

    @Test
    void testUnregisterScoreAndMoveObserverDirectly()
    {
        class TestMoveObserver implements MoveObserver {
            int count = 0;
            @Override
            public void updateMove(String message) { count++; }
        }

        class TestScoreObserver implements ScoreObserver {
            int count = 0;
            @Override
            public void updateScore(String message) { count++; }
        }

        AGame game = new PrisonerDelimmaGame(1);

        TestMoveObserver moveObserver = new TestMoveObserver();
        TestScoreObserver scoreObserver = new TestScoreObserver();

        game.registerMoveObserver(moveObserver);
        game.registerScoreObserver(scoreObserver);

        game.notifyMoveObserver("first move");
        game.notifyScoreObserver("first score");

        assertEquals(1, moveObserver.count);
        assertEquals(1, scoreObserver.count);

        game.unregisterMoveObserver(moveObserver);
        game.unregisterScoreObserver(scoreObserver);

        game.notifyMoveObserver("second move");
        game.notifyScoreObserver("second score");

        assertEquals(1, moveObserver.count);
        assertEquals(1, scoreObserver.count);
    }

    // =========================
    // LOGGING SYSTEM TESTS
    // =========================

    @Test
    void testMoveLoggingSystemWritesToFile() throws IOException
    {
        MoveLoggingSystem log = new MoveLoggingSystem();

        log.updateMove("Robot A played Cooperate");

        assertTrue(Files.exists(LOG_PATH));
        String contents = Files.readString(LOG_PATH);
        assertTrue(contents.contains("Robot A played Cooperate"));
    }

    @Test
    void testScoreLoggingSystemWritesToFile() throws IOException
    {
        ScoreLoggingSystem log = new ScoreLoggingSystem();

        log.updateScore("Robot A score updated");

        assertTrue(Files.exists(LOG_PATH));
        String contents = Files.readString(LOG_PATH);
        assertTrue(contents.contains("Robot A score updated"));
    }

    @Test
    void testMoveLoggingSystemIOExceptionCatchCoverage() throws IOException
    {
        cleanup();
        Files.createDirectory(LOG_PATH);

        MoveLoggingSystem log = new MoveLoggingSystem();

        assertDoesNotThrow(() -> log.updateMove("Trigger IOException"));
        assertTrue(Files.isDirectory(LOG_PATH));

        cleanup();
    }

    @Test
    void testScoreLoggingSystemIOExceptionCatchCoverage() throws IOException
    {
        cleanup();
        Files.createDirectory(LOG_PATH);

        ScoreLoggingSystem log = new ScoreLoggingSystem();

        assertDoesNotThrow(() -> log.updateScore("Trigger IOException"));
        assertTrue(Files.isDirectory(LOG_PATH));

        cleanup();
    }

    // =========================
    // PRISONER DELIMMA GAME LOGGING TESTS
    // =========================

    @Test
    void testPrisonerDelimmaGameWritesFormattedGameLog() throws IOException
    {
        PrisonerDelimmaGame game = new PrisonerDelimmaGame(2);
        game.registerMoveObserver(new MoveLoggingSystem());
        game.registerScoreObserver(new ScoreLoggingSystem());

        Robot p1 = new FixedMoveRobot("Alpha", "Cooperate");
        Robot p2 = new FixedMoveRobot("Beta", "Defect");

        game.playGame(p1, p2);

        assertTrue(Files.exists(LOG_PATH));
        String contents = Files.readString(LOG_PATH);

        assertTrue(contents.contains("Round 1: Alpha -> Cooperate, Beta -> Defect"));
        assertTrue(contents.contains("After round 1: Alpha=0, Beta=5"));
        assertTrue(contents.contains("Round 2: Alpha -> Cooperate, Beta -> Defect"));
        assertTrue(contents.contains("After round 2: Alpha=0, Beta=10"));
    }

    // =========================
    // TOURNAMENT TESTS
    // =========================

    @Test
    void testTournamentWinner()
    {
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
    void testTournamentWinnerIsNotAlwaysFirstRobot()
    {
        ArrayList<Robot> robots = new ArrayList<>();
        robots.add(new PrisonerSameRobot("CopyCat"));
        robots.add(new OnlyDefectRobot("Defector"));
        robots.add(new PrisonerOppositeRobot("Opposite"));

        AGame game = new PrisonerDelimmaGame(5);
        Tournament tournament = new RoundRobinTournament(robots, game);

        Robot winner = tournament.runTournament();

        assertNotNull(winner);
    }

    @Test
    void testTournamentWithTooFewParticipants()
    {
        ArrayList<Robot> robots = new ArrayList<>();
        robots.add(new OnlyDefectRobot("Solo"));

        AGame game = new PrisonerDelimmaGame(3);
        Tournament tournament = new RoundRobinTournament(robots, game);

        assertThrows(IllegalArgumentException.class, tournament::runTournament);
    }

    @Test
    void testTournamentWithNullParticipants()
    {
        AGame game = new PrisonerDelimmaGame(3);
        Tournament tournament = new RoundRobinTournament(null, game);

        assertThrows(IllegalArgumentException.class, tournament::runTournament);
    }

    @Test
    void testGetParticipants()
    {
        ArrayList<Robot> robots = new ArrayList<>();
        robots.add(new OnlyDefectRobot("A"));
        robots.add(new OnlyDefectRobot("B"));

        AGame game = new PrisonerDelimmaGame(1);
        Tournament t = new RoundRobinTournament(robots, game);

        assertEquals(2, t.getParticipants().size());
    }

    @Test
    void testRunTournamentTieBreakerUsesScore()
    {
        class FixedRobot extends Robot {
            public FixedRobot(String name) {
                super(name);
            }

            @Override
            public String makeMove() {
                return "Cooperate";
            }

            @Override
            public void rememberOpponentMove(String move) {
            }
        }

        class FakeGame extends AGame {
            private int gameCount = 0;

            public FakeGame() {
                super(1);
            }

            @Override
            public Robot playGame(Robot p1, Robot p2) {
                p1.resetScore();
                p2.resetScore();
                gameCount++;

                if (gameCount == 1) {
                    p1.addScore(1);
                    p2.addScore(5);
                    return p2;
                }

                if (gameCount == 2) {
                    p1.addScore(0);
                    p2.addScore(2);
                    return p2;
                }

                if (gameCount == 3) {
                    p1.addScore(0);
                    p2.addScore(3);
                    return p2;
                }

                return p1;
            }
        }

        ArrayList<Robot> robots = new ArrayList<>();
        robots.add(new FixedRobot("A"));
        robots.add(new FixedRobot("B"));
        robots.add(new FixedRobot("C"));

        Tournament tournament = new RoundRobinTournament(robots, new FakeGame());
        Robot winner = tournament.runTournament();

        assertNotNull(winner);
        assertEquals(1, robots.get(0).record);
        assertEquals(5, robots.get(1).record);
        assertEquals(5, robots.get(2).record);
        assertEquals(0, robots.get(1).score);
        assertEquals(3, robots.get(2).score);
        assertEquals("C", winner.getName());
    }

    @Test
    void testRoundRobinTournamentTieKeepsFirstRobot()
    {
        class TieRobot extends Robot {
            public TieRobot(String name) {
                super(name);
            }

            @Override
            public String makeMove() {
                return "Cooperate";
            }

            @Override
            public void rememberOpponentMove(String move) {
            }
        }

        class TieGame extends AGame {
            public TieGame() {
                super(1);
            }

            @Override
            public Robot playGame(Robot p1, Robot p2) {
                p1.resetScore();
                p2.resetScore();
                return p1;
            }
        }

        ArrayList<Robot> robots = new ArrayList<>();
        robots.add(new TieRobot("A"));
        robots.add(new TieRobot("B"));

        Tournament tournament = new RoundRobinTournament(robots, new TieGame());
        Robot winner = tournament.runTournament();

        assertEquals("A", winner.getName());
    }

    @Test
    void testRoundRobinTournamentWritesFormattedTournamentLog() throws IOException
    {
        ArrayList<Robot> robots = new ArrayList<>();
        robots.add(new FixedMoveRobot("Alpha", "Defect"));
        robots.add(new FixedMoveRobot("Beta", "Cooperate"));
        robots.add(new FixedMoveRobot("Gamma", "Cooperate"));

        PrisonerDelimmaGame game = new PrisonerDelimmaGame(2);
        game.registerMoveObserver(new MoveLoggingSystem());
        game.registerScoreObserver(new ScoreLoggingSystem());

        Tournament tournament = new RoundRobinTournament(robots, game);
        Robot winner = tournament.runTournament();

        assertNotNull(winner);
        assertTrue(Files.exists(LOG_PATH));

        String contents = Files.readString(LOG_PATH);
        assertTrue(contents.contains("TOURNAMENT START: Round Robin Tournament"));
        assertTrue(contents.contains("MATCH: Alpha vs Beta"));
        assertTrue(contents.contains("TOURNAMENT RECORD UPDATE: Alpha=10, Beta=0"));
        assertTrue(contents.contains("TOURNAMENT RESULT: Winner = Alpha"));
    }

    // =========================
    // MAIN TEST
    // =========================

    @Test
    void testMainRuns()
    {
        assertDoesNotThrow(() -> Main.main(new String[0]));
    }
}
