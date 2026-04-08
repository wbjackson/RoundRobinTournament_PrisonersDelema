package SITS_sprint2;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Scanner;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import SITS_sprint1.OnlyDefectRobot;
import SITS_sprint1.PrisonerDelimmaGame;
import SITS_sprint1.PrisonerSameRobot;
import SITS_sprint1.Robot;

class TournamentServerTest
{
    // =========================
    // Client tests
    // =========================

    @Test
    void testClientMakeDecisionReturnsValidMove()
    {
        Client client = new Client();

        String move = client.makeDecision();

        assertTrue(move.equals("Cooperate") || move.equals("Defect"));
    }

    @Test
    void testClientMakeDecisionEventuallyReturnsValidMoves()
    {
        Client client = new Client();

        for (int i = 0; i < 20; i++) {
            String move = client.makeDecision();
            assertTrue(move.equals("Cooperate") || move.equals("Defect"));
        }
    }

    // =========================
    // RemoteClientRobot tests
    // =========================

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
    void testRemoteClientRobotRememberOpponentMoveDoesNothing()
    {
        RemoteClientRobot robot = new RemoteClientRobot("Remote", "localhost", "8080");

        assertDoesNotThrow(() -> robot.rememberOpponentMove("Defect"));
    }

    @Test
    void testRemoteClientRobotMakeMoveFallsBackToDefectWhenServerUnavailable()
    {
        RemoteClientRobot robot = new RemoteClientRobot("Remote", "localhost", "9999");

        String move = robot.makeMove();

        assertEquals("Defect", move);
    }

    // =========================
    // TournamentServer tests
    // =========================

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

        ArrayList<Robot> participants = new ArrayList<>();
        participants.add(new OnlyDefectRobot("Defector"));
        participants.add(new PrisonerSameRobot("CopyCat"));

        PrisonerDelimmaGame game = new PrisonerDelimmaGame(3);

        int tournamentId = server.createTournament(participants, game);

        assertTrue(tournamentId > 0);
    }

    @Test
    void testCreateTournamentIdsIncrease()
    {
        TournamentServer server = new TournamentServer();

        ArrayList<Robot> participants = new ArrayList<>();
        participants.add(new OnlyDefectRobot("Defector"));
        participants.add(new PrisonerSameRobot("CopyCat"));

        PrisonerDelimmaGame game = new PrisonerDelimmaGame(3);

        int id1 = server.createTournament(participants, game);
        int id2 = server.createTournament(participants, game);

        assertEquals(id1 + 1, id2);
    }

    @Test
    void testAddClientToTournamentSuccess()
    {
        TournamentServer server = new TournamentServer();

        server.registerClient("Remote1", "localhost", "8080");

        ArrayList<Robot> participants = new ArrayList<>();
        participants.add(new OnlyDefectRobot("Defector"));
        participants.add(new PrisonerSameRobot("CopyCat"));

        PrisonerDelimmaGame game = new PrisonerDelimmaGame(3);

        int tournamentId = server.createTournament(participants, game);
        String result = server.addClientToTournament("Remote1", tournamentId);

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

        ArrayList<Robot> participants = new ArrayList<>();
        participants.add(new OnlyDefectRobot("Defector"));
        participants.add(new PrisonerSameRobot("CopyCat"));

        PrisonerDelimmaGame game = new PrisonerDelimmaGame(3);

        int tournamentId = server.createTournament(participants, game);
        String result = server.addClientToTournament("MissingClient", tournamentId);

        assertEquals("Client not found.", result);
    }

    @Test
    void testStartTournamentWithMissingTournamentThrowsException()
    {
        TournamentServer server = new TournamentServer();

        assertThrows(IllegalArgumentException.class, () -> server.startTournament(999));
    }



    @Test
    void testViewTournamentsContainsCreatedTournamentIds()
    {
        TournamentServer server = new TournamentServer();

        ArrayList<Robot> participants = new ArrayList<>();
        participants.add(new OnlyDefectRobot("Defector"));
        participants.add(new PrisonerSameRobot("CopyCat"));

        PrisonerDelimmaGame game = new PrisonerDelimmaGame(3);

        int tournamentId = server.createTournament(participants, game);
        String tournaments = server.viewTournaments();

        assertTrue(tournaments.contains(String.valueOf(tournamentId)));
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

            String move = robot.makeMove();

            assertEquals("Cooperate", move);
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

            String move = robot.makeMove();

            assertEquals("Defect", move);
        }
        finally {
            server.stop(0);
        }
    }
    
    //HUMAN ROBOT TESTS
    
    @Test
    void testHumanRobotMakeMoveCooperate()
    {
        Scanner scanner = new Scanner("Cooperate\n");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(output);

        HumanRobot robot = new HumanRobot("Human1", scanner, out);

        String move = robot.makeMove();

        assertEquals("Cooperate", move);
    }

    @Test
    void testHumanRobotMakeMoveDefect()
    {
        Scanner scanner = new Scanner("Defect\n");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(output);

        HumanRobot robot = new HumanRobot("Human1", scanner, out);

        String move = robot.makeMove();

        assertEquals("Defect", move);
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

        String text = output.toString();
        assertTrue(text.contains("Invalid move"));
    }

    @Test
    void testHumanRobotRememberOpponentMovePrintsMessage()
    {
        Scanner scanner = new Scanner("Cooperate\n");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(output);

        HumanRobot robot = new HumanRobot("Human1", scanner, out);

        robot.rememberOpponentMove("Defect");

        String text = output.toString();
        assertTrue(text.contains("opponent played: Defect"));
    }
    
    @Test
    void testHumanRobotDefaultConstructor()
    {
        HumanRobot robot = new HumanRobot("Human");

        assertEquals("Human", robot.getName());
    }
    
    //TOURNAMENT REGISTRATION PHASE TESTS
    
    
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

        ArrayList<Robot> participants = new ArrayList<>();
        participants.add(new OnlyDefectRobot("A"));
        participants.add(new PrisonerSameRobot("B"));

        int id = server.createTournament(participants, new PrisonerDelimmaGame(1));

        assertTrue(server.isRegistrationOpen(id));
    }

    @Test
    void testCloseRegistration()
    {
        TournamentServer server = new TournamentServer();

        ArrayList<Robot> participants = new ArrayList<>();
        participants.add(new OnlyDefectRobot("A"));
        participants.add(new PrisonerSameRobot("B"));

        int id = server.createTournament(participants, new PrisonerDelimmaGame(1));

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
    void testAddClientToTournamentFailsWhenRegistrationClosed()
    {
        TournamentServer server = new TournamentServer();

        server.registerClient("Remote1", "localhost", "8081");

        ArrayList<Robot> participants = new ArrayList<>();
        participants.add(new OnlyDefectRobot("A"));
        participants.add(new PrisonerSameRobot("B"));

        int id = server.createTournament(participants, new PrisonerDelimmaGame(1));
        server.closeRegistration(id);

        String result = server.addClientToTournament("Remote1", id);

        assertEquals("Registration is closed.", result);
    }

    @Test
    void testStartTournamentFailsIfRegistrationStillOpen()
    {
        TournamentServer server = new TournamentServer();

        ArrayList<Robot> participants = new ArrayList<>();
        participants.add(new OnlyDefectRobot("A"));
        participants.add(new PrisonerSameRobot("B"));

        int id = server.createTournament(participants, new PrisonerDelimmaGame(1));

        assertThrows(IllegalStateException.class, () -> server.startTournament(id));
    }

    @Test
    void testStartTournamentWorksAfterRegistrationClosed()
    {
        TournamentServer server = new TournamentServer();

        ArrayList<Robot> participants = new ArrayList<>();
        participants.add(new OnlyDefectRobot("A"));
        participants.add(new PrisonerSameRobot("B"));

        int id = server.createTournament(participants, new PrisonerDelimmaGame(1));
        server.closeRegistration(id);

        Robot winner = server.startTournament(id);

        assertNotNull(winner);
    }

    @Test
    void testViewTournamentsShowsOnlyOpenTournaments()
    {
        TournamentServer server = new TournamentServer();

        ArrayList<Robot> participants = new ArrayList<>();
        participants.add(new OnlyDefectRobot("A"));
        participants.add(new PrisonerSameRobot("B"));

        int openId = server.createTournament(participants, new PrisonerDelimmaGame(1));
        int closedId = server.createTournament(participants, new PrisonerDelimmaGame(1));

        server.closeRegistration(closedId);

        String result = server.viewTournaments();

        assertTrue(result.contains(String.valueOf(openId)));
        assertFalse(result.contains(String.valueOf(closedId)));
    }
    
    //REST CONTROLLER TESTS I THINK
    
    
    
}
