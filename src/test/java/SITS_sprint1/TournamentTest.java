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

    private static class CountingMoveObserver implements MoveObserver
    {
        int count = 0;
        String lastMessage = "";

        @Override
        public void updateMove(String moveMessage)
        {
            count++;
            lastMessage = moveMessage;
        }
    }

    private static class CountingScoreObserver implements ScoreObserver
    {
        int count = 0;
        String lastMessage = "";

        @Override
        public void updateScore(String scoreMessage)
        {
            count++;
            lastMessage = scoreMessage;
        }
    }

    // =========================
    // ROBOT TESTS
    // =========================

    @Test
    void testRobotStartsWithCorrectNameScoreAndRecord()
    {
        Robot robot = new OnlyDefectRobot("Defector");

        assertEquals("Defector", robot.getName());
        assertEquals(0, robot.getScore());
        assertEquals(0, robot.getRecord());
    }

    @Test
    void testRobotAddScoreAndResetScore()
    {
        Robot robot = new OnlyDefectRobot("Defector");

        robot.addScore(5);
        robot.addScore(3);

        assertEquals(8, robot.getScore());

        robot.resetScore();

        assertEquals(0, robot.getScore());
    }

    @Test
    void testRobotAddRecordAndResetRecord()
    {
        Robot robot = new OnlyDefectRobot("Defector");

        robot.addRecord(5);
        robot.addRecord(10);

        assertEquals(15, robot.getRecord());

        robot.resetRecord();

        assertEquals(0, robot.getRecord());
    }

    @Test
    void testGetOpponentName()
    {
        Robot robot1 = new OnlyDefectRobot("Alpha");
        Robot robot2 = new OnlyDefectRobot("Beta");

        assertEquals("Beta", robot1.getOpponentName(robot2));
    }

    @Test
    void testOnlyDefectRobotMove()
    {
        Robot robot = new OnlyDefectRobot("Defector");

        assertEquals("Defect", robot.makeMove());
    }

    @Test
    void testOnlyDefectRobotRememberOpponentMoveDoesNothing()
    {
        Robot robot = new OnlyDefectRobot("Defector");

        assertDoesNotThrow(() -> robot.rememberOpponentMove("Cooperate"));
        assertEquals("Defect", robot.makeMove());
    }

    @Test
    void testPrisonerSameRobotStartsWithCooperate()
    {
        PrisonerSameRobot robot = new PrisonerSameRobot("CopyCat");

        assertEquals("Cooperate", robot.makeMove());
    }

    @Test
    void testPrisonerSameRobotCopiesOpponentMove()
    {
        PrisonerSameRobot robot = new PrisonerSameRobot("CopyCat");

        robot.rememberOpponentMove("Defect");
        assertEquals("Defect", robot.makeMove());

        robot.rememberOpponentMove("Cooperate");
        assertEquals("Cooperate", robot.makeMove());
    }

    @Test
    void testPrisonerOppositeRobotStartsOppositeOfCooperate()
    {
        PrisonerOppositeRobot robot = new PrisonerOppositeRobot("Opposite");

        assertEquals("Defect", robot.makeMove());
    }

    @Test
    void testPrisonerOppositeRobotChoosesOppositeMove()
    {
        PrisonerOppositeRobot robot = new PrisonerOppositeRobot("Opposite");

        robot.rememberOpponentMove("Cooperate");
        assertEquals("Defect", robot.makeMove());

        robot.rememberOpponentMove("Defect");
        assertEquals("Cooperate", robot.makeMove());
    }

    @Test
    void testRandomRobotOnlyReturnsValidMoves()
    {
        RandomRobot robot = new RandomRobot("Random");

        for (int i = 0; i < 100; i++) {
            String move = robot.makeMove();
            assertTrue(move.equals("Cooperate") || move.equals("Defect"));
        }
    }

    @Test
    void testRandomRobotRememberOpponentMoveDoesNothing()
    {
        RandomRobot robot = new RandomRobot("Random");

        assertDoesNotThrow(() -> robot.rememberOpponentMove("Cooperate"));
    }

    // =========================
    // GAME LOGIC TESTS
    // =========================

    @Test
    void testPlayGameResetsScoresBeforePlaying()
    {
        Robot p1 = new FixedMoveRobot("A", "Cooperate");
        Robot p2 = new FixedMoveRobot("B", "Cooperate");

        p1.addScore(100);
        p2.addScore(100);

        AGame game = new PrisonerDelimmaGame(1);
        game.playGame(p1, p2);

        assertEquals(3, p1.getScore());
        assertEquals(3, p2.getScore());
    }

    @Test
    void testGameReturnsHigherScoringRobot()
    {
        Robot r1 = new FixedMoveRobot("CopyCat", "Cooperate");
        Robot r2 = new FixedMoveRobot("Defector", "Defect");

        AGame game = new PrisonerDelimmaGame(3);

        Robot winner = game.playGame(r1, r2);

        assertEquals("Defector", winner.getName());
    }

    @Test
    void testGameReturnsFirstRobotOnTie()
    {
        Robot r1 = new FixedMoveRobot("A", "Cooperate");
        Robot r2 = new FixedMoveRobot("B", "Cooperate");

        AGame game = new PrisonerDelimmaGame(1);

        Robot winner = game.playGame(r1, r2);

        assertEquals("A", winner.getName());
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

    @Test
    void testMultipleRoundsAccumulateScore()
    {
        PrisonerDelimmaGame game = new PrisonerDelimmaGame(3);

        Robot p1 = new FixedMoveRobot("A", "Defect");
        Robot p2 = new FixedMoveRobot("B", "Cooperate");

        game.playGame(p1, p2);

        assertEquals(15, p1.getScore());
        assertEquals(0, p2.getScore());
    }

    @Test
    void testZeroRoundGameLeavesScoresAtZero()
    {
        PrisonerDelimmaGame game = new PrisonerDelimmaGame(0);

        Robot p1 = new FixedMoveRobot("A", "Defect");
        Robot p2 = new FixedMoveRobot("B", "Cooperate");

        Robot winner = game.playGame(p1, p2);

        assertEquals(0, p1.getScore());
        assertEquals(0, p2.getScore());
        assertEquals("A", winner.getName());
    }

    // =========================
    // OBSERVER / AGAME TESTS
    // =========================

    @Test
    void testRegisterMoveObserver()
    {
        AGame game = new PrisonerDelimmaGame(1);
        CountingMoveObserver observer = new CountingMoveObserver();

        game.registerMoveObserver(observer);

        assertEquals(1, game.getMoveObservers().size());
        assertTrue(game.getMoveObservers().contains(observer));
    }

    @Test
    void testRegisterScoreObserver()
    {
        AGame game = new PrisonerDelimmaGame(1);
        CountingScoreObserver observer = new CountingScoreObserver();

        game.registerScoreObserver(observer);

        assertEquals(1, game.getScoreObservers().size());
        assertTrue(game.getScoreObservers().contains(observer));
    }

    @Test
    void testNotifyMoveObserverDirectly()
    {
        AGame game = new PrisonerDelimmaGame(1);
        CountingMoveObserver observer = new CountingMoveObserver();

        game.registerMoveObserver(observer);
        game.notifyMoveObserver("move message");

        assertEquals(1, observer.count);
        assertEquals("move message", observer.lastMessage);
    }

    @Test
    void testNotifyScoreObserverDirectly()
    {
        AGame game = new PrisonerDelimmaGame(1);
        CountingScoreObserver observer = new CountingScoreObserver();

        game.registerScoreObserver(observer);
        game.notifyScoreObserver("score message");

        assertEquals(1, observer.count);
        assertEquals("score message", observer.lastMessage);
    }

    @Test
    void testObserversAreNotifiedDuringGame()
    {
        Robot robot1 = new OnlyDefectRobot("A");
        Robot robot2 = new OnlyDefectRobot("B");

        AGame game = new PrisonerDelimmaGame(3);
        CountingMoveObserver moveObs = new CountingMoveObserver();
        CountingScoreObserver scoreObs = new CountingScoreObserver();

        game.registerMoveObserver(moveObs);
        game.registerScoreObserver(scoreObs);

        game.playGame(robot1, robot2);

        assertEquals(3, moveObs.count);
        assertEquals(3, scoreObs.count);
    }

    @Test
    void testUnregisterMoveObserverOnly()
    {
        AGame game = new PrisonerDelimmaGame(3);
        CountingMoveObserver moveObs = new CountingMoveObserver();

        game.registerMoveObserver(moveObs);
        game.unregisterMoveObserver(moveObs);

        Robot r1 = new OnlyDefectRobot("A");
        Robot r2 = new OnlyDefectRobot("B");

        game.playGame(r1, r2);

        assertEquals(0, moveObs.count);
        assertEquals(0, game.getMoveObservers().size());
    }

    @Test
    void testUnregisterScoreObserverOnly()
    {
        AGame game = new PrisonerDelimmaGame(2);
        CountingScoreObserver scoreObs = new CountingScoreObserver();

        game.registerScoreObserver(scoreObs);
        game.unregisterScoreObserver(scoreObs);

        Robot r1 = new OnlyDefectRobot("A");
        Robot r2 = new OnlyDefectRobot("B");

        game.playGame(r1, r2);

        assertEquals(0, scoreObs.count);
        assertEquals(0, game.getScoreObservers().size());
    }

    @Test
    void testUnregisterOneMoveObserverDoesNotRemoveOther()
    {
        AGame game = new PrisonerDelimmaGame(1);
        CountingMoveObserver observer1 = new CountingMoveObserver();
        CountingMoveObserver observer2 = new CountingMoveObserver();

        game.registerMoveObserver(observer1);
        game.registerMoveObserver(observer2);

        game.unregisterMoveObserver(observer1);

        game.notifyMoveObserver("test");

        assertEquals(0, observer1.count);
        assertEquals(1, observer2.count);
    }

    @Test
    void testUnregisterOneScoreObserverDoesNotRemoveOther()
    {
        AGame game = new PrisonerDelimmaGame(1);
        CountingScoreObserver observer1 = new CountingScoreObserver();
        CountingScoreObserver observer2 = new CountingScoreObserver();

        game.registerScoreObserver(observer1);
        game.registerScoreObserver(observer2);

        game.unregisterScoreObserver(observer1);

        game.notifyScoreObserver("test");

        assertEquals(0, observer1.count);
        assertEquals(1, observer2.count);
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
    void testTournamentStoresParticipants()
    {
        ArrayList<Robot> robots = new ArrayList<>();
        robots.add(new OnlyDefectRobot("A"));
        robots.add(new OnlyDefectRobot("B"));

        AGame game = new PrisonerDelimmaGame(1);
        Tournament tournament = new RoundRobinTournament(robots, game);

        assertEquals(2, tournament.getParticipants().size());
        assertSame(robots.get(0), tournament.getParticipants().get(0));
        assertSame(robots.get(1), tournament.getParticipants().get(1));
    }

    @Test
    void testTournamentStoresGame()
    {
        ArrayList<Robot> robots = new ArrayList<>();
        robots.add(new OnlyDefectRobot("A"));
        robots.add(new OnlyDefectRobot("B"));

        AGame game = new PrisonerDelimmaGame(1);
        Tournament tournament = new RoundRobinTournament(robots, game);

        assertSame(game, tournament.getGame());
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
    void testTournamentWithEmptyParticipants()
    {
        ArrayList<Robot> robots = new ArrayList<>();

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
        assertTrue(robots.contains(winner));
    }

    @Test
    void testRoundRobinRunsEveryPairExactlyOnce()
    {
        class CountingGame extends AGame {
            int gameCount = 0;

            public CountingGame() {
                super(1);
            }

            @Override
            public Robot playGame(Robot p1, Robot p2) {
                gameCount++;
                p1.resetScore();
                p2.resetScore();
                p1.addScore(1);
                p2.addScore(1);
                return p1;
            }
        }

        ArrayList<Robot> robots = new ArrayList<>();
        robots.add(new FixedMoveRobot("A", "Cooperate"));
        robots.add(new FixedMoveRobot("B", "Cooperate"));
        robots.add(new FixedMoveRobot("C", "Cooperate"));
        robots.add(new FixedMoveRobot("D", "Cooperate"));

        CountingGame game = new CountingGame();
        Tournament tournament = new RoundRobinTournament(robots, game);

        tournament.runTournament();

        assertEquals(6, game.gameCount);
    }

    @Test
    void testTournamentRecordsAccumulateAcrossGames()
    {
        ArrayList<Robot> robots = new ArrayList<>();
        robots.add(new FixedMoveRobot("A", "Defect"));
        robots.add(new FixedMoveRobot("B", "Cooperate"));
        robots.add(new FixedMoveRobot("C", "Cooperate"));

        AGame game = new PrisonerDelimmaGame(1);
        Tournament tournament = new RoundRobinTournament(robots, game);

        tournament.runTournament();

        assertEquals(10, robots.get(0).getRecord());
        assertEquals(3, robots.get(1).getRecord());
        assertEquals(3, robots.get(2).getRecord());
    }

    @Test
    void testTournamentResetsRecordsBeforeRunning()
    {
        ArrayList<Robot> robots = new ArrayList<>();
        robots.add(new FixedMoveRobot("A", "Cooperate"));
        robots.add(new FixedMoveRobot("B", "Cooperate"));

        robots.get(0).addRecord(100);
        robots.get(1).addRecord(100);

        AGame game = new PrisonerDelimmaGame(1);
        Tournament tournament = new RoundRobinTournament(robots, game);

        tournament.runTournament();

        assertEquals(3, robots.get(0).getRecord());
        assertEquals(3, robots.get(1).getRecord());
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

        assertEquals(1, robots.get(0).getRecord());
        assertEquals(5, robots.get(1).getRecord());
        assertEquals(5, robots.get(2).getRecord());
        assertEquals(0, robots.get(1).getScore());
        assertEquals(3, robots.get(2).getScore());
        assertEquals("C", winner.getName());
    }

    @Test
    void testRoundRobinTournamentTieKeepsFirstRobot()
    {
        class TieGame extends AGame {
            public TieGame() {
                super(1);
            }

            @Override
            public Robot playGame(Robot p1, Robot p2) {
                p1.resetScore();
                p2.resetScore();
                p1.addScore(3);
                p2.addScore(3);
                return p1;
            }
        }

        ArrayList<Robot> robots = new ArrayList<>();
        robots.add(new FixedMoveRobot("A", "Cooperate"));
        robots.add(new FixedMoveRobot("B", "Cooperate"));

        Tournament tournament = new RoundRobinTournament(robots, new TieGame());
        Robot winner = tournament.runTournament();

        assertEquals("A", winner.getName());
    }

    @Test
    void testRoundRobinTournamentNotifiesTournamentObservers()
    {
        ArrayList<Robot> robots = new ArrayList<>();
        robots.add(new FixedMoveRobot("A", "Cooperate"));
        robots.add(new FixedMoveRobot("B", "Cooperate"));

        AGame game = new PrisonerDelimmaGame(1);
        CountingMoveObserver moveObserver = new CountingMoveObserver();
        CountingScoreObserver scoreObserver = new CountingScoreObserver();

        game.registerMoveObserver(moveObserver);
        game.registerScoreObserver(scoreObserver);

        Tournament tournament = new RoundRobinTournament(robots, game);
        tournament.runTournament();

        assertTrue(moveObserver.count >= 5);
        assertTrue(scoreObserver.count >= 3);
        assertTrue(scoreObserver.lastMessage.contains("====================================="));
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
