package SITS_sprint3;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class TournamentInfoTest
{
    @Test
    void testConstructorAndGetters()
    {
        TournamentInfo info = new TournamentInfo(5, "Alpha", true, false);

        assertEquals(5, info.getId());
        assertEquals("Alpha", info.getName());
        assertTrue(info.isActive());
        assertFalse(info.isRegistrationOpen());
    }

    @Test
    void testStatusTextRegistration()
    {
        TournamentInfo info = new TournamentInfo(1, "Test", false, true);

        assertEquals("Registration", info.getStatusText());
    }

    @Test
    void testStatusTextActive()
    {
        TournamentInfo info = new TournamentInfo(1, "Test", true, false);

        assertEquals("Active", info.getStatusText());
    }

    @Test
    void testStatusTextClosed()
    {
        TournamentInfo info = new TournamentInfo(1, "Test", false, false);

        assertEquals("Closed", info.getStatusText());
    }

    @Test
    void testStatusPriorityRegistrationOverActive()
    {
        TournamentInfo info = new TournamentInfo(1, "Test", true, true);

        assertEquals("Registration", info.getStatusText());
    }

    @Test
    void testToStringFormat()
    {
        TournamentInfo info = new TournamentInfo(3, "RoundRobin", true, false);

        String expected = "Tournament 3 - RoundRobin [Active]";
        assertEquals(expected, info.toString());
    }

    @Test
    void testToStringClosed()
    {
        TournamentInfo info = new TournamentInfo(4, "ClosedTest", false, false);

        String text = info.toString();

        assertTrue(text.contains("Closed"));
    }

    @Test
    void testDifferentInstancesAreIndependent()
    {
        TournamentInfo a = new TournamentInfo(1, "A", true, false);
        TournamentInfo b = new TournamentInfo(2, "B", false, true);

        assertNotEquals(a.getId(), b.getId());
        assertNotEquals(a.getName(), b.getName());
    }
    
    @Test
    void testMoveViewCanBeCreated()
    {
        MoveView view = new MoveView();

        assertNotNull(view);
    }

    @Test
    void testTournamentsViewCanBeCreated()
    {
        TournamentsView view = new TournamentsView();

        assertNotNull(view);
    }

    @Test
    void testSceneManagerPaths()
    {
        assertEquals("/tournaments-view.fxml", SceneManager.TOURNAMENTS_FXML);
        assertEquals("/move-view.fxml", SceneManager.MOVE_FXML);
    }
    
    @Test
    void testTournamentInfoInactiveNotRegistrationIsClosed()
    {
        TournamentInfo info = new TournamentInfo(9, "Closed Tournament", false, false);

        assertFalse(info.isActive());
        assertFalse(info.isRegistrationOpen());
        assertEquals("Closed", info.getStatusText());
        assertTrue(info.toString().contains("Closed"));
    }
}
