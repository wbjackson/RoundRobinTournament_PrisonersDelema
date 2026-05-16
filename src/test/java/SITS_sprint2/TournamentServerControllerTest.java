package SITS_sprint2;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import SITS_sprint1.OnlyDefectRobot;
import SITS_sprint1.Robot;

class TournamentServerControllerTest
{
    @Test
    void testViewTournamentsInitiallyEmpty()
    {
        TournamentServerController controller = new TournamentServerController();

        String result = controller.viewTournaments();

        assertTrue(result.contains("["));
        assertTrue(result.contains("]"));
    }

    @Test
    void testRegisterClientThroughController()
    {
        TournamentServerController controller = new TournamentServerController();

        String result = controller.registerClient("Remote1", "localhost", "8081");

        assertTrue(result.contains("Client registered successfully"));
    }

    @Test
    void testCreateTournamentThroughController()
    {
        TournamentServerController controller = new TournamentServerController();

        String result = controller.createTournament();

        assertTrue(result.contains("Tournament created:"));
    }

    @Test
    void testJoinCloseAndStartTournamentThroughController() throws InterruptedException
    {
        TournamentServerController controller = new TournamentServerController();

        controller.registerClient("Remote1", "localhost", "8081");

        String createResult = controller.createTournament();
        int tournamentId = Integer.parseInt(createResult.replace("Tournament created: ", "").trim());

        String joinResult = controller.addClientToTournament("Remote1", tournamentId);
        String closeResult = controller.closeRegistration(tournamentId);
        String startResult = controller.startTournament(tournamentId);

        assertTrue(joinResult.contains("Client added"));
        assertTrue(closeResult.contains("Registration closed"));
        assertTrue(startResult.contains("Tournament started"));

        Thread.sleep(2000);

        String tournaments = controller.viewTournaments();
        assertTrue(tournaments.contains(tournamentId + ":FINISHED"));
    }

    @Test
    void testGetTournamentMovesThroughController() throws InterruptedException
    {
        TournamentServerController controller = new TournamentServerController();

        String createResult = controller.createTournament();
        int tournamentId = Integer.parseInt(createResult.replace("Tournament created: ", "").trim());

        controller.closeRegistration(tournamentId);
        controller.startTournament(tournamentId);

        Thread.sleep(2000);

        String moves = controller.getTournamentMoves(tournamentId);

        assertTrue(moves.contains("TOURNAMENT START"));
        assertTrue(moves.contains("MATCH"));
    }
    
    @Test
    void testRobotClientControllerDefaultConstructorCreatesDefaultNamedRobot()
    {
        RobotClientController controller = new RobotClientController();

        assertNotNull(controller.getHostedRobot("RemoteHostedRobot"));
        assertEquals("Defect", controller.makeDecision("RemoteHostedRobot"));
    }

    @Test
    void testRobotClientControllerSetAndGetNamedHostedRobot()
    {
        RobotClientController controller = new RobotClientController();
        Robot robot = new OnlyDefectRobot("Test");

        controller.setHostedRobot("Remote1", robot);

        assertEquals(robot, controller.getHostedRobot("Remote1"));
    }

    @Test
    void testRobotClientControllerTwoRemoteRobotsCanHaveDifferentTypes()
    {
        RobotClientController controller = new RobotClientController();

        controller.setRobotType("Remote1", "defector");
        controller.setRobotType("Remote2", "copycat");

        assertEquals("Defect", controller.makeDecision("Remote1"));
        assertEquals("Cooperate", controller.makeDecision("Remote2"));
    }

    @Test
    void testRobotClientControllerNamedRememberOpponentMove()
    {
        RobotClientController controller = new RobotClientController();

        controller.setRobotType("Remote1", "copycat");

        assertEquals("Cooperate", controller.makeDecision("Remote1"));

        controller.rememberOpponentMove("Remote1", "Defect");

        assertEquals("Defect", controller.makeDecision("Remote1"));
    }

    @Test
    void testRobotClientControllerMakeDecisionWithMissingRobotDefaultsToDefect()
    {
        RobotClientController controller = new RobotClientController();

        String result = controller.makeDecision("MissingRobot");

        assertEquals("Defect", result);
    }

    @Test
    void testRobotClientControllerRememberOpponentMoveInvalidName()
    {
        RobotClientController controller = new RobotClientController();

        String result = controller.rememberOpponentMove("", "Cooperate");

        assertTrue(result.contains("ERROR"));
    }

    @Test
    void testRobotClientControllerRememberOpponentMoveInvalidMove()
    {
        RobotClientController controller = new RobotClientController();

        controller.setRobotType("Remote1", "copycat");

        String result = controller.rememberOpponentMove("Remote1", "");

        assertTrue(result.contains("ERROR"));
    }

    @Test
    void testRobotClientControllerRememberOpponentMoveMissingRobot()
    {
        RobotClientController controller = new RobotClientController();

        String result = controller.rememberOpponentMove("MissingRobot", "Cooperate");

        assertTrue(result.contains("ERROR"));
    }

    @Test
    void testRobotClientControllerSetHumanAndHumanMoveByName()
    {
        RobotClientController controller = new RobotClientController();

        controller.setRobotType("Remote1", "human");

        String setResult = controller.setHumanMove("Remote1", "Defect");
        String currentResult = controller.getHumanMove("Remote1");

        assertTrue(setResult.contains("Defect"));
        assertTrue(currentResult.contains("Defect"));
    }

    @Test
    void testRobotClientControllerIsHumanByName()
    {
        RobotClientController controller = new RobotClientController();

        controller.setRobotType("Remote1", "human");
        controller.setRobotType("Remote2", "random");

        assertEquals("true", controller.isHumanRobot("Remote1"));
        assertEquals("false", controller.isHumanRobot("Remote2"));
    }
    
    @Test
    void testRegisterClientInvalidName()
    {
        TournamentServerController controller = new TournamentServerController();

        String result = controller.registerClient("", "localhost", "8081");

        assertEquals("Invalid client name.", result);
    }

    @Test
    void testRegisterClientInvalidIp()
    {
        TournamentServerController controller = new TournamentServerController();

        String result = controller.registerClient("Remote1", "", "8081");

        assertEquals("Invalid IP address.", result);
    }

    @Test
    void testRegisterClientInvalidPort()
    {
        TournamentServerController controller = new TournamentServerController();

        String result = controller.registerClient("Remote1", "localhost", "");

        assertEquals("Invalid port.", result);
    }

    @Test
    void testAddClientToTournamentInvalidClientName()
    {
        TournamentServerController controller = new TournamentServerController();

        String result = controller.addClientToTournament("", 1);

        assertEquals("Invalid client name.", result);
    }

    @Test
    void testStartTournamentMissingTournament()
    {
        TournamentServerController controller = new TournamentServerController();

        String result = controller.startTournament(999);

        assertEquals("Tournament not found.", result);
    }

    @Test
    void testStartTournamentWhileRegistrationOpen()
    {
        TournamentServerController controller = new TournamentServerController();

        String createResult = controller.createTournament();
        int tournamentId = Integer.parseInt(createResult.replace("Tournament created: ", "").trim());

        String result = controller.startTournament(tournamentId);

        assertEquals("Registration is still open.", result);
    }

    @Test
    void testGetAndSetServer()
    {
        TournamentServerController controller = new TournamentServerController();
        TournamentServer server = new TournamentServer();

        controller.setServer(server);

        assertEquals(server, controller.getServer());
    }
    
    @Test
    void testControllerConstructorWithServer()
    {
        TournamentServer server = new TournamentServer();

        TournamentServerController controller = new TournamentServerController(server);

        assertEquals(server, controller.getServer());
    }
    
    @Test
    void testStartTournamentThrowsException()
    {
        TournamentServerController controller = new TournamentServerController();

        String result = controller.startTournament(-1);

        assertTrue(result.contains("not found") || result.contains("Invalid"));
    }
    
    @Test
    void testStartTournamentReturnsWinner()
    {
        TournamentServer fakeServer = new TournamentServer()
        {
            @Override
            public Robot startTournament(int id)
            {
                return new OnlyDefectRobot("TestWinner");
            }
        };

        TournamentServerController controller = new TournamentServerController(fakeServer);

        String result = controller.startTournament(1);

        assertEquals("Winner: TestWinner", result);
    }
}