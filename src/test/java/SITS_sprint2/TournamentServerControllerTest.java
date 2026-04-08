package SITS_sprint2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class TournamentServerControllerTest
{
    @Test
    void testViewTournamentsInitiallyEmpty()
    {
        TournamentServerController controller = new TournamentServerController();

        String result = controller.viewTournaments();

        assertTrue(result.contains("["));
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

        String idText = createResult.replace("Tournament created: ", "").trim();
        int tournamentId = Integer.parseInt(idText);

        String joinResult = controller.addClientToTournament("Remote1", tournamentId);
        String closeResult = controller.closeRegistration(tournamentId);

        assertTrue(joinResult.contains("Client added"));
        assertTrue(closeResult.contains("Registration closed"));

        String startResult = controller.startTournament(tournamentId);

        assertTrue(startResult.contains("Winner:"));
    }
    
    @Test
    void testIsRegistrationOpenInvalidId()
    {
        TournamentServer server = new TournamentServer();

        assertFalse(server.isRegistrationOpen(999));
    }
    
    @Test
    void testHumanRobotDefaultConstructor()
    {
        HumanRobot robot = new HumanRobot("Human");

        assertEquals("Human", robot.getName());
    }
    
}
