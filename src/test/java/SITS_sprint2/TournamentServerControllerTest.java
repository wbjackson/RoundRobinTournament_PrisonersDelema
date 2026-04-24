
package SITS_sprint2;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class TournamentServerControllerTest
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
    void testJoinCloseAndStartTournamentThroughController()
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
        assertTrue(startResult.contains("Winner:"));
    }

    @Test
    void testGetTournamentMovesThroughController()
    {
        TournamentServerController controller = new TournamentServerController();

        String createResult = controller.createTournament();
        int tournamentId = Integer.parseInt(createResult.replace("Tournament created: ", "").trim());

        controller.closeRegistration(tournamentId);
        controller.startTournament(tournamentId);

        String moves = controller.getTournamentMoves(tournamentId);

        assertTrue(moves.contains("TOURNAMENT START"));
        assertTrue(moves.contains("MATCH"));
        assertTrue(moves.contains("Round"));
    }

    @Test
    void testRegisterViewerThroughController()
    {
        TournamentServerController controller = new TournamentServerController();

        String createResult = controller.createTournament();
        int tournamentId = Integer.parseInt(createResult.replace("Tournament created: ", "").trim());

        String result = controller.registerViewer(tournamentId, "localhost", "8095");

        assertEquals("Viewer registered.", result);
    }

    @Test
    void testRegisterViewerInvalidTournamentThroughController()
    {
        TournamentServerController controller = new TournamentServerController();

        String result = controller.registerViewer(999, "localhost", "8095");

        assertEquals("Tournament not found.", result);
    }

    @Test
    void testUnregisterViewerThroughController()
    {
        TournamentServerController controller = new TournamentServerController();

        String createResult = controller.createTournament();
        int tournamentId = Integer.parseInt(createResult.replace("Tournament created: ", "").trim());

        controller.registerViewer(tournamentId, "localhost", "8095");
        String result = controller.unregisterViewer(tournamentId, "localhost", "8095");

        assertEquals("Viewer unregistered.", result);
    }

    @Test
    void testUnregisterViewerInvalidTournamentThroughController()
    {
        TournamentServerController controller = new TournamentServerController();

        String result = controller.unregisterViewer(999, "localhost", "8095");

        assertEquals("Tournament not found.", result);
    }

    @Test
    void testCloseMissingTournamentThroughController()
    {
        TournamentServerController controller = new TournamentServerController();

        String result = controller.closeRegistration(999);

        assertEquals("Tournament not found.", result);
    }
    
    @Test
    void testIsRegistrationOpen()
    {
        TournamentServer server = new TournamentServer();

        int id = server.createTournament(twoRobots(), new PrisonerDelimmaGame(1));

        assertTrue(server.isRegistrationOpen(id));

        server.closeRegistration(id);

        assertFalse(server.isRegistrationOpen(id));
    }
    
    @Test
    void testNormalizeResponseAllBranches()
    {
        RemoteClientRobot robot = new RemoteClientRobot("R", "localhost", "8080");

        assertEquals("Defect", robot.normalizeResponse(null));
        assertEquals("Cooperate", robot.normalizeResponse("Cooperate"));
        assertEquals("Cooperate", robot.normalizeResponse("cooperate"));
        assertEquals("Defect", robot.normalizeResponse("random"));
        assertEquals("Defect", robot.normalizeResponse(""));
    }
    
    
}