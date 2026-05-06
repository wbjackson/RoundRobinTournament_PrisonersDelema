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
    void testRobotClientController_DefaultConstructor()
    {
        RobotClientController controller = new RobotClientController();

        assertNotNull(controller.getHostedRobot());
    }

    @Test
    void testRobotClientController_SetAndGetHostedRobot()
    {
        RobotClientController controller = new RobotClientController();
        Robot robot = new OnlyDefectRobot("Test");
        
        controller.setHostedRobot(robot);

        assertEquals(robot, controller.getHostedRobot());
    }

    @Test
    void testRobotClientController_MakeDecisionWithNullRobot()
    {
        RobotClientController controller = new RobotClientController();
        controller.setHostedRobot(null);

        String result = controller.makeDecision();

        assertTrue(result.contains("ERROR"));
    }

    @Test
    void testRobotClientController_RememberOpponentMove_Invalid()
    {
        RobotClientController controller = new RobotClientController();

        String result = controller.rememberOpponentMove("");

        assertTrue(result.contains("ERROR"));
    }

    @Test
    void testRobotClientController_RememberOpponentMove_NullRobot()
    {
        RobotClientController controller = new RobotClientController();
        controller.setHostedRobot(null);

        String result = controller.rememberOpponentMove("Cooperate");

        assertTrue(result.contains("ERROR"));
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