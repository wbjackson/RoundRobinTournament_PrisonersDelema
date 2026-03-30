package SITS_sprint2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import SITS_sprint1.AGame;
import SITS_sprint1.Robot;
import SITS_sprint1.RoundRobinTournament;
import SITS_sprint1.Tournament;

public class TournamentServer
{
    private Map<Integer, Tournament> tournaments;
    private Map<String, RemoteClientRobot> clients;
    private int nextTournamentId;

    public TournamentServer()
    {
        tournaments = new HashMap<>();
        clients = new HashMap<>();
        nextTournamentId = 1;
    }

    public String registerClient(String name, String ip, String port)
    {
        if (clients.containsKey(name))
        {
            return "Client already registered.";
        }

        RemoteClientRobot robot = new RemoteClientRobot(name, ip, port);
        clients.put(name, robot);

        return "Client registered successfully: " + name;
    }

    public int createTournament(List<Robot> participants, AGame game)
    {
        int id = nextTournamentId++;

        Tournament t = new RoundRobinTournament(new ArrayList<>(participants), game);
        tournaments.put(id, t);

        return id;
    }

    public String addClientToTournament(String clientName, int tournamentId)
    {
        Tournament t = tournaments.get(tournamentId);
        RemoteClientRobot robot = clients.get(clientName);

        if (t == null)
        {
            return "Tournament not found.";
        }

        if (robot == null)
        {
            return "Client not found.";
        }

        t.getParticipants().add(robot); //TODO

        return "Client added to tournament.";
    }

    public Robot startTournament(int tournamentId)
    {
        Tournament t = tournaments.get(tournamentId);

        if (t == null)
        {
            throw new IllegalArgumentException("Tournament not found.");
        }

        return t.runTournament();
    }

    public String viewTournaments()
    {
        return tournaments.keySet().toString();
    }
}