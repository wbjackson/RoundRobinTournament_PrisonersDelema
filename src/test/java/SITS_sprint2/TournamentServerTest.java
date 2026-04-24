
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

class TournamentServerTest
{
    @Test
    void testClientMakeDecisionUsesHostedRobot()
    {
        Client client = new Client(new OnlyDefectRobot("Hosted"));

        assertEquals("Defect", client.makeDecision());
    }

    @Test
    void testClientCanUseDifferentHostedRobot()
    {
        Client client = new Client(new PrisonerSameRobot("CopyCat"));

        assertEquals("Cooperate", client.makeDecision());
    }

    @Test
    void testClientRememberOpponentMoveUpdatesHostedRobot()
    {
        PrisonerSameRobot robot = new PrisonerSameRobot("CopyCat");
        Client client = new Client(robot);

        client.rememberOpponentMove("Defect");

        assertEquals("Defect", client.makeDecision());
    }

    @Test
    void testClientGetterAndSetter()
    {
        Client client = new Client();
        Robot robot = new OnlyDefectRobot("TestRobot");

        client.setHostedRobot(robot);

        assertEquals(robot, client.getHostedRobot());
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

        server.createContext("/move", exchange -> {
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

        server.createContext("/move", exchange -> {
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

        server.createContext("/remember/Defect", exchange -> {
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

        server.createContext("/move", exchange -> {
            String response = previousMove[0];
            exchange.sendResponseHeaders(200, response.getBytes().length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });

        server.createContext("/remember/Defect", exchange -> {
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
            RemoteClientRobot robot = new RemoteClientRobot("Remote", "localhost", String.valueOf(port));

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
    void testIsRegistrationOpenInvalidId()
    {
        TournamentServer server = new TournamentServer();

        assertFalse(server.isRegistrationOpen(999));
    }

    @Test
    void testTournamentStartsWithRegistrationOpen()
    {
        TournamentServer server = new TournamentServer();

        int id = server.createTournament(twoRobots(), new PrisonerDelimmaGame(1));

        assertTrue(server.isRegistrationOpen(id));
    }

    @Test
    void testCloseRegistration()
    {
        TournamentServer server = new TournamentServer();

        int id = server.createTournament(twoRobots(), new PrisonerDelimmaGame(1));

        String result = server.closeRegistration(id);

        assertEquals("Registration closed.", result);
        assertFalse(server.isRegistrationOpen(id));
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
    void testStartTournamentWorksAfterRegistrationClosed()
    {
        TournamentServer server = new TournamentServer();

        int id = server.createTournament(twoRobots(), new PrisonerDelimmaGame(1));
        server.closeRegistration(id);

        Robot winner = server.startTournament(id);

        assertNotNull(winner);
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
    void testViewTournamentsShowsActiveStatusAfterClose()
    {
        TournamentServer server = new TournamentServer();

        int id = server.createTournament(twoRobots(), new PrisonerDelimmaGame(1));
        server.closeRegistration(id);

        String result = server.viewTournaments();

        assertTrue(result.contains(id + ":ACTIVE"));
    }

    @Test
    void testRegisterViewerSuccess()
    {
        TournamentServer server = new TournamentServer();

        int id = server.createTournament(twoRobots(), new PrisonerDelimmaGame(1));

        String result = server.registerViewer(id, "localhost", "8095");

        assertEquals("Viewer registered.", result);
    }

    @Test
    void testRegisterViewerMissingTournament()
    {
        TournamentServer server = new TournamentServer();

        String result = server.registerViewer(999, "localhost", "8095");

        assertEquals("Tournament not found.", result);
    }

    @Test
    void testUnregisterViewerSuccess()
    {
        TournamentServer server = new TournamentServer();

        int id = server.createTournament(twoRobots(), new PrisonerDelimmaGame(1));

        server.registerViewer(id, "localhost", "8095");
        String result = server.unregisterViewer(id, "localhost", "8095");

        assertEquals("Viewer unregistered.", result);
    }

    @Test
    void testUnregisterViewerMissingTournament()
    {
        TournamentServer server = new TournamentServer();

        String result = server.unregisterViewer(999, "localhost", "8095");

        assertEquals("Tournament not found.", result);
    }

    @Test
    void testUnregisterViewerNotFound()
    {
        TournamentServer server = new TournamentServer();

        int id = server.createTournament(twoRobots(), new PrisonerDelimmaGame(1));

        String result = server.unregisterViewer(id, "localhost", "8095");

        assertEquals("Viewer not found.", result);
    }

    @Test
    void testTournamentMoveHistoryRecordsMoves()
    {
        TournamentServer server = new TournamentServer();

        int id = server.createTournament(twoRobots(), new PrisonerDelimmaGame(1));

        server.closeRegistration(id);
        server.startTournament(id);

        String moves = server.getTournamentMoves(id);

        assertTrue(moves.contains("TOURNAMENT START"));
        assertTrue(moves.contains("MATCH"));
        assertTrue(moves.contains("Round"));
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
}