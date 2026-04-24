package SITS_sprint3;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class TournamentInfoTest
{
    @Test
    void testTournamentInfoStoresValues()
    {
        TournamentInfo info = new TournamentInfo(1, "Test", true, false);

        assertEquals(1, info.getId());
        assertEquals("Test", info.getName());
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
    void testToStringContainsUsefulInfo()
    {
        TournamentInfo info = new TournamentInfo(2, "RoundRobin", true, false);

        String text = info.toString();

        assertTrue(text.contains("2"));
        assertTrue(text.contains("RoundRobin"));
        assertTrue(text.contains("Active"));
    }
}