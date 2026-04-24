
package SITS_sprint2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import SITS_sprint1.AGame;
import SITS_sprint1.MoveObserver;
import SITS_sprint1.Robot;
import SITS_sprint1.RoundRobinTournament;
import SITS_sprint1.Tournament;
import SITS_sprint3.RemoteClientViewer;

public class TournamentServer
{
    private static class HostedTournament
    {
        Tournament tournament;
        boolean registrationOpen;
        boolean started;
        boolean finished;

        List<RemoteClientViewer> viewers;
        List<String> moveHistory;
        MoveObserver historyObserver;

        HostedTournament(Tournament tournament)
        {
            this.tournament = tournament;
            this.registrationOpen = true;
            this.started = false;
            this.finished = false;
            this.viewers = new ArrayList<>();
            this.moveHistory = new ArrayList<>();

            this.historyObserver = new MoveObserver()
            {
                @Override
                public void updateMove(String moveMessage)
                {
                    moveHistory.add(moveMessage);
                }
            };

            this.tournament.game.registerMoveObserver(this.historyObserver);
        }
    }

    private Map<Integer, HostedTournament> tournaments;
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
        tournaments.put(id, new HostedTournament(t));

        return id;
    }

    public String addClientToTournament(String clientName, int tournamentId)
    {
        HostedTournament hosted = tournaments.get(tournamentId);
        RemoteClientRobot robot = clients.get(clientName);

        if (hosted == null)
        {
            return "Tournament not found.";
        }

        if (!hosted.registrationOpen)
        {
            return "Registration is closed.";
        }

        if (robot == null)
        {
            return "Client not found.";
        }

        hosted.tournament.getParticipants().add(robot);

        return "Client added to tournament.";
    }

    public String closeRegistration(int tournamentId)
    {
        HostedTournament hosted = tournaments.get(tournamentId);

        if (hosted == null)
        {
            return "Tournament not found.";
        }

        hosted.registrationOpen = false;
        return "Registration closed.";
    }

    public boolean isRegistrationOpen(int tournamentId)
    {
        HostedTournament hosted = tournaments.get(tournamentId);

        if (hosted == null)
        {
            return false;
        }

        return hosted.registrationOpen;
    }

    public Robot startTournament(int tournamentId)
    {
        HostedTournament hosted = tournaments.get(tournamentId);

        if (hosted == null)
        {
            throw new IllegalArgumentException("Tournament not found.");
        }

        if (hosted.registrationOpen)
        {
            throw new IllegalStateException("Registration is still open.");
        }

        hosted.started = true;
        Robot winner = hosted.tournament.runTournament();
        hosted.finished = true;

        return winner;
    }

    public String registerViewer(int tournamentId, String ip, String port)
    {
        HostedTournament hosted = tournaments.get(tournamentId);

        if (hosted == null)
        {
            return "Tournament not found.";
        }

        RemoteClientViewer viewer = new RemoteClientViewer(ip, port);
        hosted.viewers.add(viewer);

        hosted.tournament.game.registerMoveObserver(viewer);

        return "Viewer registered.";
    }

    public String unregisterViewer(int tournamentId, String ip, String port)
    {
        HostedTournament hosted = tournaments.get(tournamentId);

        if (hosted == null)
        {
            return "Tournament not found.";
        }

        RemoteClientViewer toRemove = null;

        for (RemoteClientViewer viewer : hosted.viewers)
        {
            if (viewer.getViewerIP().equals(ip) && viewer.getViewerPort().equals(port))
            {
                toRemove = viewer;
                break;
            }
        }

        if (toRemove != null)
        {
            hosted.tournament.game.unregisterMoveObserver(toRemove);
            hosted.viewers.remove(toRemove);
            return "Viewer unregistered.";
        }

        return "Viewer not found.";
    }

    public String getTournamentMoves(int tournamentId)
    {
        HostedTournament hosted = tournaments.get(tournamentId);

        if (hosted == null)
        {
            return "[]";
        }

        return hosted.moveHistory.toString();
    }

    public String viewTournaments()
    {
        ArrayList<String> tournamentInfo = new ArrayList<>();

        for (Map.Entry<Integer, HostedTournament> entry : tournaments.entrySet())
        {
            int id = entry.getKey();
            HostedTournament hosted = entry.getValue();

            if (hosted.registrationOpen)
            {
                tournamentInfo.add(id + ":REG");
            }
            else if (hosted.finished)
            {
                tournamentInfo.add(id + ":ACTIVE");
            }
            else if (hosted.started)
            {
                tournamentInfo.add(id + ":ACTIVE");
            }
            else
            {
                tournamentInfo.add(id + ":ACTIVE");
            }
        }

        return tournamentInfo.toString();
    }
}