
package SITS_sprint2;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Scanner;

import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.Test;

import SITS_sprint1.OnlyDefectRobot;
import SITS_sprint1.PrisonerDelimmaGame;
import SITS_sprint1.PrisonerSameRobot;
import SITS_sprint1.Robot;
import SITS_sprint2.UrlHumanRobot;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class TournamentServerTest
{

	@Test
	void testRobotClientControllerDefaultConstructorCreatesDefaultRobot()
	{
	    RobotClientController controller = new RobotClientController();

	    assertNotNull(controller.getHostedRobot("RemoteHostedRobot"));
	    assertEquals("Defect", controller.makeDecision("RemoteHostedRobot"));
	}

	@Test
	void testRobotClientControllerCanHostTwoNamedRobotsSeparately()
	{
	    RobotClientController controller = new RobotClientController();

	    String remote1Result = controller.setRobotType("Remote1", "defector");
	    String remote2Result = controller.setRobotType("Remote2", "copycat");

	    assertTrue(remote1Result.contains("Remote1"));
	    assertTrue(remote2Result.contains("Remote2"));

	    assertEquals("Defect", controller.makeDecision("Remote1"));
	    assertEquals("Cooperate", controller.makeDecision("Remote2"));
	}

	@Test
	void testRobotClientControllerRememberOpponentMoveUsesCorrectNamedRobot()
	{
	    RobotClientController controller = new RobotClientController();

	    controller.setRobotType("Remote1", "copycat");
	    controller.setRobotType("Remote2", "copycat");

	    controller.rememberOpponentMove("Remote1", "Defect");
	    controller.rememberOpponentMove("Remote2", "Cooperate");

	    assertEquals("Defect", controller.makeDecision("Remote1"));
	    assertEquals("Cooperate", controller.makeDecision("Remote2"));
	}

	@Test
	void testRobotClientControllerSetRobotTypeRejectsInvalidRobotName()
	{
	    RobotClientController controller = new RobotClientController();

	    String result = controller.setRobotType("", "defector");

	    assertTrue(result.contains("ERROR"));
	}

	@Test
	void testRobotClientControllerSetRobotTypeRejectsInvalidType()
	{
	    RobotClientController controller = new RobotClientController();

	    String result = controller.setRobotType("Remote1", "banana");

	    assertTrue(result.contains("ERROR"));
	}

	@Test
	void testRobotClientControllerMakeDecisionMissingRobotDefaultsToDefect()
	{
	    RobotClientController controller = new RobotClientController();

	    String result = controller.makeDecision("MissingRobot");

	    assertEquals("Defect", result);
	}

	@Test
	void testRobotClientControllerRememberOpponentMoveMissingRobotReturnsError()
	{
	    RobotClientController controller = new RobotClientController();

	    String result = controller.rememberOpponentMove("MissingRobot", "Cooperate");

	    assertTrue(result.contains("ERROR"));
	}

	@Test
	void testRobotClientControllerRememberOpponentMoveInvalidMoveReturnsError()
	{
	    RobotClientController controller = new RobotClientController();

	    controller.setRobotType("Remote1", "copycat");

	    String result = controller.rememberOpponentMove("Remote1", "");

	    assertTrue(result.contains("ERROR"));
	}

	@Test
	void testRobotClientControllerCurrentRobotShowsCorrectNamedRobot()
	{
	    RobotClientController controller = new RobotClientController();

	    controller.setRobotType("Remote1", "random");

	    String result = controller.getCurrentRobot("Remote1");

	    assertTrue(result.contains("Remote1"));
	    assertTrue(result.contains("RandomRobot"));
	}

	@Test
	void testRobotClientControllerCurrentRobotMissingRobot()
	{
	    RobotClientController controller = new RobotClientController();

	    String result = controller.getCurrentRobot("MissingRobot");

	    assertTrue(result.contains("No robot found"));
	}

	@Test
	void testRobotClientControllerGetAllRobotsIncludesNamedRobots()
	{
	    RobotClientController controller = new RobotClientController();

	    controller.setRobotType("Remote1", "defector");
	    controller.setRobotType("Remote2", "copycat");

	    String result = controller.getAllRobots();

	    assertTrue(result.contains("Remote1"));
	    assertTrue(result.contains("Remote2"));
	}

	@Test
	void testRobotClientControllerCanSetHumanMoveForNamedHumanRobot()
	{
	    RobotClientController controller = new RobotClientController();

	    controller.setRobotType("Remote1", "human");

	    String setResult = controller.setHumanMove("Remote1", "Cooperate");
	    String currentResult = controller.getHumanMove("Remote1");

	    assertTrue(setResult.contains("Cooperate"));
	    assertTrue(currentResult.contains("Cooperate"));
	}

	@Test
	void testRobotClientControllerSetHumanMoveFailsForNonHumanRobot()
	{
	    RobotClientController controller = new RobotClientController();

	    controller.setRobotType("Remote1", "defector");

	    String result = controller.setHumanMove("Remote1", "Cooperate");

	    assertTrue(result.contains("ERROR"));
	}

	@Test
	void testRobotClientControllerHumanCurrentFailsForNonHumanRobot()
	{
	    RobotClientController controller = new RobotClientController();

	    controller.setRobotType("Remote1", "defector");

	    String result = controller.getHumanMove("Remote1");

	    assertTrue(result.contains("ERROR"));
	}

	@Test
	void testRobotClientControllerHumanOpponentMove()
	{
	    RobotClientController controller = new RobotClientController();

	    controller.setRobotType("Remote1", "human");
	    controller.rememberOpponentMove("Remote1", "Defect");

	    String result = controller.getHumanOpponentMove("Remote1");

	    assertTrue(result.contains("Remote1"));
	    assertTrue(result.contains("Defect"));
	}

	@Test
	void testRobotClientControllerIsHumanRobot()
	{
	    RobotClientController controller = new RobotClientController();

	    controller.setRobotType("Remote1", "human");
	    controller.setRobotType("Remote2", "defector");

	    assertEquals("true", controller.isHumanRobot("Remote1"));
	    assertEquals("false", controller.isHumanRobot("Remote2"));
	    assertEquals("false", controller.isHumanRobot("MissingRobot"));
	}

	@Test
	void testUrlHumanRobotWaitsUntilMoveIsSubmitted() throws Exception
	{
	    UrlHumanRobot robot = new UrlHumanRobot("Remote1");

	    ExecutorService executor = Executors.newSingleThreadExecutor();
	    Future<String> future = executor.submit(() -> robot.makeMove());

	    try
	    {
	        assertThrows(TimeoutException.class, () -> future.get(300, TimeUnit.MILLISECONDS));

	        String result = robot.setNextMove("Cooperate");

	        assertTrue(result.contains("Cooperate"));
	        assertEquals("Cooperate", future.get(2, TimeUnit.SECONDS));
	        assertEquals("No move currently submitted.", robot.getNextMove());
	    }
	    finally
	    {
	        executor.shutdownNow();
	    }
	}

	@Test
	void testUrlHumanRobotRejectsInvalidMove()
	{
	    UrlHumanRobot robot = new UrlHumanRobot("Remote1");

	    String result = robot.setNextMove("banana");

	    assertTrue(result.contains("ERROR"));
	    assertEquals("No move currently submitted.", robot.getNextMove());
	}

	@Test
	void testUrlHumanRobotRemembersOpponentMove()
	{
	    UrlHumanRobot robot = new UrlHumanRobot("Remote1");

	    robot.rememberOpponentMove("Defect");

	    assertEquals("Defect", robot.getLastOpponentMove());
	}

	@Test
	void testRemoteClientRobotMakeMoveUsesRobotNameInUrl() throws Exception
	{
	    HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);

	    server.createContext("/move/Remote", exchange ->
	    {
	        String response = "Cooperate";
	        exchange.sendResponseHeaders(200, response.getBytes().length);

	        try (OutputStream os = exchange.getResponseBody())
	        {
	            os.write(response.getBytes());
	        }
	    });

	    server.start();
	    int port = server.getAddress().getPort();

	    try
	    {
	        RemoteClientRobot robot = new RemoteClientRobot("Remote1", "localhost", String.valueOf(port));

	        assertEquals("Cooperate", robot.makeMove());
	    }
	    finally
	    {
	        server.stop(0);
	    }
	}

	@Test
	void testRemoteClientRobotRememberOpponentMoveUsesRobotNameInUrl() throws Exception
	{
	    final String[] remembered = {null};

	    HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);

	    server.createContext("/remember/Remote1/Defect", exchange ->
	    {
	        remembered[0] = "Remote1 remembered Defect";

	        String response = "Remembered";
	        exchange.sendResponseHeaders(200, response.getBytes().length);

	        try (OutputStream os = exchange.getResponseBody())
	        {
	            os.write(response.getBytes());
	        }
	    });

	    server.start();
	    int port = server.getAddress().getPort();

	    try
	    {
	        RemoteClientRobot robot = new RemoteClientRobot("Remote1", "localhost", String.valueOf(port));

	        robot.rememberOpponentMove("Defect");

	        assertEquals("Remote1 remembered Defect", remembered[0]);
	    }
	    finally
	    {
	        server.stop(0);
	    }
	}

	@Test
	void testRemoteClientRobotIsHumanControlledUsesNamedEndpoint() throws Exception
	{
	    HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);

	    server.createContext("/robot/isHuman/Remote", exchange ->
	    {
	        String response = "true";
	        exchange.sendResponseHeaders(200, response.getBytes().length);

	        try (OutputStream os = exchange.getResponseBody())
	        {
	            os.write(response.getBytes());
	        }
	    });

	    server.start();
	    int port = server.getAddress().getPort();

	    try
	    {
	        RemoteClientRobot robot = new RemoteClientRobot("Remote1", "localhost", String.valueOf(port));

	        assertTrue(robot.isHumanControlled());
	    }
	    finally
	    {
	        server.stop(0);
	    }
	}

	@Test
	void testRemoteClientRobotIsHumanControlledReturnsFalseWhenUnavailable()
	{
	    RemoteClientRobot robot = new RemoteClientRobot("Remote1", "localhost", "9999");

	    assertFalse(robot.isHumanControlled());
	}

    @Test
    void testNormalizeResponseWithNull()
    {
        RemoteClientRobot robot = new RemoteClientRobot("Remote", "localhost", "8080");
        assertEquals("Defect", robot.normalizeResponse(null));
    }

    @Test
    void testNormalizeResponseWithCooperate()
    {
        RemoteClientRobot robot = new RemoteClientRobot("Remote", "localhost", "8080");

        assertEquals("Cooperate", robot.normalizeResponse("Cooperate"));
        assertEquals("Cooperate", robot.normalizeResponse("cooperate"));
        assertEquals("Cooperate", robot.normalizeResponse("  Cooperate  "));
    }

    @Test
    void testNormalizeResponseWithAnythingElse()
    {
        RemoteClientRobot robot = new RemoteClientRobot("Remote", "localhost", "8080");

        assertEquals("Defect", robot.normalizeResponse("Defect"));
        assertEquals("Defect", robot.normalizeResponse("random"));
        assertEquals("Defect", robot.normalizeResponse(""));
    }

    @Test
    void testRemoteClientRobotMakeMoveFallsBackToDefectWhenServerUnavailable()
    {
        RemoteClientRobot robot = new RemoteClientRobot("Remote", "localhost", "9999");
        assertEquals("Defect", robot.makeMove());
    }

    @Test
    void testRemoteClientRobotRememberOpponentMoveHandlesException()
    {
        RemoteClientRobot robot = new RemoteClientRobot("Remote", "localhost", "9999");
        assertDoesNotThrow(() -> robot.rememberOpponentMove("Defect"));
    }

    @Test
    void testRemoteClientRobotMakeMoveSuccessCooperate() throws Exception
    {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);

        server.createContext("/move/Remote", exchange -> {
            String response = "Cooperate";
            exchange.sendResponseHeaders(200, response.getBytes().length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });

        server.start();
        int port = server.getAddress().getPort();

        try {
            RemoteClientRobot robot = new RemoteClientRobot("Remote", "localhost", String.valueOf(port));
            assertEquals("Cooperate", robot.makeMove());
        }
        finally {
            server.stop(0);
        }
    }

    @Test
    void testRemoteClientRobotMakeMoveSuccessDefectFromOtherResponse() throws Exception
    {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);

        server.createContext("/move/Remote1", exchange -> {
            String response = "somethingElse";
            exchange.sendResponseHeaders(200, response.getBytes().length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });

        server.start();
        int port = server.getAddress().getPort();

        try {
            RemoteClientRobot robot = new RemoteClientRobot("Remote", "localhost", String.valueOf(port));
            assertEquals("Defect", robot.makeMove());
        }
        finally {
            server.stop(0);
        }
    }

    @Test
    void testRemoteClientRobotRememberOpponentMoveSendsMoveOverHttp() throws Exception
    {
        final String[] rememberedMove = {null};

        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);

        server.createContext("/remember/Remote/Defect", exchange -> {
            rememberedMove[0] = "Defect";
            String response = "Remembered: Defect";
            exchange.sendResponseHeaders(200, response.getBytes().length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });

        server.start();
        int port = server.getAddress().getPort();

        try {
            RemoteClientRobot robot = new RemoteClientRobot("Remote", "localhost", String.valueOf(port));
            robot.rememberOpponentMove("Defect");
            assertEquals("Defect", rememberedMove[0]);
        }
        finally {
            server.stop(0);
        }
    }

    @Test
    void testRemoteClientRobotUsesPreviousMoveThroughHttp() throws Exception
    {
        final String[] previousMove = {"Cooperate"};

        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);

        server.createContext("/move/Remote1", exchange -> {
            String response = previousMove[0];
            exchange.sendResponseHeaders(200, response.getBytes().length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });

        server.createContext("/remember/Remote1/Defect", exchange -> {
            previousMove[0] = "Defect";
            String response = "Remembered: Defect";
            exchange.sendResponseHeaders(200, response.getBytes().length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });

        server.start();
        int port = server.getAddress().getPort();

        try {
            RemoteClientRobot robot = new RemoteClientRobot("Remote1", "localhost", String.valueOf(port));

            assertEquals("Cooperate", robot.makeMove());

            robot.rememberOpponentMove("Defect");

            assertEquals("Defect", robot.makeMove());
        }
        finally {
            server.stop(0);
        }
    }

    @Test
    void testRegisterClientSuccess()
    {
        TournamentServer server = new TournamentServer();

        String result = server.registerClient("Remote1", "localhost", "8080");

        assertEquals("Client registered successfully: Remote1", result);
    }

    @Test
    void testRegisterClientDuplicate()
    {
        TournamentServer server = new TournamentServer();

        server.registerClient("Remote1", "localhost", "8080");
        String result = server.registerClient("Remote1", "localhost", "8080");

        assertEquals("Client already registered.", result);
    }

    @Test
    void testCreateTournamentReturnsPositiveId()
    {
        TournamentServer server = new TournamentServer();

        int id = server.createTournament(twoRobots(), new PrisonerDelimmaGame(3));

        assertTrue(id > 0);
    }

    @Test
    void testCreateTournamentIdsIncrease()
    {
        TournamentServer server = new TournamentServer();

        int id1 = server.createTournament(twoRobots(), new PrisonerDelimmaGame(3));
        int id2 = server.createTournament(twoRobots(), new PrisonerDelimmaGame(3));

        assertEquals(id1 + 1, id2);
    }

    @Test
    void testAddClientToTournamentSuccess()
    {
        TournamentServer server = new TournamentServer();

        server.registerClient("Remote1", "localhost", "8080");
        int id = server.createTournament(twoRobots(), new PrisonerDelimmaGame(3));

        String result = server.addClientToTournament("Remote1", id);

        assertEquals("Client added to tournament.", result);
    }

    @Test
    void testAddClientToTournamentWhenTournamentMissing()
    {
        TournamentServer server = new TournamentServer();

        server.registerClient("Remote1", "localhost", "8080");

        String result = server.addClientToTournament("Remote1", 999);

        assertEquals("Tournament not found.", result);
    }

    @Test
    void testAddClientToTournamentWhenClientMissing()
    {
        TournamentServer server = new TournamentServer();

        int id = server.createTournament(twoRobots(), new PrisonerDelimmaGame(3));

        String result = server.addClientToTournament("MissingClient", id);

        assertEquals("Client not found.", result);
    }

    @Test
    void testAddClientToTournamentFailsWhenRegistrationClosed()
    {
        TournamentServer server = new TournamentServer();

        server.registerClient("Remote1", "localhost", "8081");

        int id = server.createTournament(twoRobots(), new PrisonerDelimmaGame(1));
        server.closeRegistration(id);

        String result = server.addClientToTournament("Remote1", id);

        assertEquals("Registration is closed.", result);
    }

    @Test
    void testCloseRegistration()
    {
        TournamentServer server = new TournamentServer();

        int id = server.createTournament(twoRobots(), new PrisonerDelimmaGame(1));

        String result = server.closeRegistration(id);

        assertEquals("Registration closed.", result);
        assertTrue(server.viewTournaments().contains(id + ":CLOSED"));
    }

    @Test
    void testCloseRegistrationMissingTournament()
    {
        TournamentServer server = new TournamentServer();

        String result = server.closeRegistration(999);

        assertEquals("Tournament not found.", result);
    }

    @Test
    void testStartTournamentFailsIfRegistrationStillOpen()
    {
        TournamentServer server = new TournamentServer();

        int id = server.createTournament(twoRobots(), new PrisonerDelimmaGame(1));

        assertThrows(IllegalStateException.class, () -> server.startTournament(id));
    }

    @Test
    void testStartTournamentWithMissingTournamentThrowsException()
    {
        TournamentServer server = new TournamentServer();

        assertThrows(IllegalArgumentException.class, () -> server.startTournament(999));
    }

    @Test
    void testStartTournamentWorksAfterRegistrationClosed() throws InterruptedException
    {
        TournamentServer server = new TournamentServer();

        int id = server.createTournament(twoRobots(), new PrisonerDelimmaGame(1));
        server.closeRegistration(id);

        Robot winner = server.startTournament(id);

        assertNull(winner);
        assertTrue(server.viewTournaments().contains(id + ":ACTIVE"));

        Thread.sleep(2000);

        assertTrue(server.viewTournaments().contains(id + ":FINISHED"));
    }

    @Test
    void testStartTournamentTwiceThrowsException()
    {
        TournamentServer server = new TournamentServer();

        int id = server.createTournament(twoRobots(), new PrisonerDelimmaGame(1));
        server.closeRegistration(id);
        server.startTournament(id);

        assertThrows(IllegalStateException.class, () -> server.startTournament(id));
    }

    @Test
    void testViewTournamentsContainsCreatedTournamentIdAndRegStatus()
    {
        TournamentServer server = new TournamentServer();

        int id = server.createTournament(twoRobots(), new PrisonerDelimmaGame(1));

        String result = server.viewTournaments();

        assertTrue(result.contains(id + ":REG"));
    }

    @Test
    void testViewTournamentsShowsClosedStatusAfterClose()
    {
        TournamentServer server = new TournamentServer();

        int id = server.createTournament(twoRobots(), new PrisonerDelimmaGame(1));
        server.closeRegistration(id);

        String result = server.viewTournaments();

        assertTrue(result.contains(id + ":CLOSED"));
    }

    @Test
    void testViewTournamentsShowsFinishedStatusAfterTournamentRuns() throws InterruptedException
    {
        TournamentServer server = new TournamentServer();

        int id = server.createTournament(twoRobots(), new PrisonerDelimmaGame(1));
        server.closeRegistration(id);
        server.startTournament(id);

        Thread.sleep(2000);

        String result = server.viewTournaments();

        assertTrue(result.contains(id + ":FINISHED"));
    }

    @Test
    void testTournamentMoveHistoryRecordsMoves() throws InterruptedException
    {
        TournamentServer server = new TournamentServer();

        int id = server.createTournament(twoRobots(), new PrisonerDelimmaGame(1));

        server.closeRegistration(id);
        server.startTournament(id);

        Thread.sleep(2000);

        String moves = server.getTournamentMoves(id);

        assertTrue(moves.contains("TOURNAMENT START"));
        assertTrue(moves.contains("MATCH"));
    }

    @Test
    void testGetTournamentMovesMissingTournament()
    {
        TournamentServer server = new TournamentServer();

        String result = server.getTournamentMoves(999);

        assertEquals("[]", result);
    }

    @Test
    void testHumanRobotDefaultConstructor()
    {
        HumanRobot robot = new HumanRobot("Human");

        assertEquals("Human", robot.getName());
    }

    @Test
    void testHumanRobotMakeMoveCooperate()
    {
        Scanner scanner = new Scanner("Cooperate\n");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(output);

        HumanRobot robot = new HumanRobot("Human1", scanner, out);

        assertEquals("Cooperate", robot.makeMove());
    }

    @Test
    void testHumanRobotMakeMoveDefect()
    {
        Scanner scanner = new Scanner("Defect\n");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(output);

        HumanRobot robot = new HumanRobot("Human1", scanner, out);

        assertEquals("Defect", robot.makeMove());
    }

    @Test
    void testHumanRobotMakeMoveShortForms()
    {
        Scanner scanner = new Scanner("C\nD\n");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(output);

        HumanRobot robot1 = new HumanRobot("Human1", scanner, out);
        HumanRobot robot2 = new HumanRobot("Human2", scanner, out);

        assertEquals("Cooperate", robot1.makeMove());
        assertEquals("Defect", robot2.makeMove());
    }

    @Test
    void testHumanRobotInvalidThenValidInput()
    {
        Scanner scanner = new Scanner("banana\nCooperate\n");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(output);

        HumanRobot robot = new HumanRobot("Human1", scanner, out);

        String move = robot.makeMove();

        assertEquals("Cooperate", move);
        assertTrue(output.toString().contains("Invalid move"));
    }

    @Test
    void testHumanRobotRememberOpponentMovePrintsMessage()
    {
        Scanner scanner = new Scanner("Cooperate\n");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(output);

        HumanRobot robot = new HumanRobot("Human1", scanner, out);

        robot.rememberOpponentMove("Defect");

        assertTrue(output.toString().contains("opponent played: Defect"));
    }

    private ArrayList<Robot> twoRobots()
    {
        ArrayList<Robot> participants = new ArrayList<>();
        participants.add(new OnlyDefectRobot("A"));
        participants.add(new PrisonerSameRobot("B"));
        return participants;
    }
    
    @Test
    void testUrlHumanRobotIsHumanControlled()
    {
        UrlHumanRobot robot = new UrlHumanRobot("Remote1");

        assertTrue(robot.isHumanControlled());
    }

    @Test
    void testUrlHumanRobotAcceptsShortCooperateMove()
    {
        UrlHumanRobot robot = new UrlHumanRobot("Remote1");

        String result = robot.setNextMove("C");

        assertTrue(result.contains("Cooperate"));
        assertEquals("Cooperate", robot.getNextMove());
    }

    @Test
    void testUrlHumanRobotAcceptsShortDefectMove()
    {
        UrlHumanRobot robot = new UrlHumanRobot("Remote1");

        String result = robot.setNextMove("D");

        assertTrue(result.contains("Defect"));
        assertEquals("Defect", robot.getNextMove());
    }

    @Test
    void testUrlHumanRobotRejectsNullMove()
    {
        UrlHumanRobot robot = new UrlHumanRobot("Remote1");

        String result = robot.setNextMove(null);

        assertTrue(result.contains("ERROR"));
    }

    @Test
    void testUrlHumanRobotRememberOpponentMoveWithNull()
    {
        UrlHumanRobot robot = new UrlHumanRobot("Remote1");

        robot.rememberOpponentMove(null);

        assertEquals("Unknown", robot.getLastOpponentMove());
    }

    @Test
    void testUrlHumanRobotRememberOpponentMoveWithBlank()
    {
        UrlHumanRobot robot = new UrlHumanRobot("Remote1");

        robot.rememberOpponentMove("");

        assertEquals("Unknown", robot.getLastOpponentMove());
    }

}