package SITS_sprint2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import SITS_sprint1.AGame;
import SITS_sprint1.MoveObserver;
import SITS_sprint1.Robot;
import SITS_sprint1.RoundRobinTournament;
import SITS_sprint1.Tournament;


@SpringBootApplication
public class TournamentServer
{
    private static class HostedTournament
    {
        Tournament tournament;
        boolean registrationOpen;
        boolean started;
        boolean finished;

        List<String> moveHistory;
        MoveObserver historyObserver;

        HostedTournament(Tournament tournament)
        {
            this.tournament = tournament;
            this.registrationOpen = true;
            this.started = false;
            this.finished = false;
            this.moveHistory = new ArrayList<>();

            this.historyObserver = new MoveObserver()
            {
                @Override
                public void updateMove(String moveMessage)
                {
                    moveHistory.add(moveMessage);
                }
            };

            this.tournament.getGame().registerMoveObserver(this.historyObserver);
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

        Tournament tournament = new RoundRobinTournament(new ArrayList<>(participants), game);
        tournaments.put(id, new HostedTournament(tournament));

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

        if (hosted.started)
        {
            throw new IllegalStateException("Tournament already started.");
        }

        hosted.started = true;

        new Thread(() ->
        {
            try
            {
                Thread.sleep(1500);
                hosted.tournament.runTournament();
            }
            catch (InterruptedException e)
            {
            }
            finally
            {
                hosted.finished = true;
            }
        }).start();

        return null;
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
            else if (!hosted.started && !hosted.finished)
            {
                tournamentInfo.add(id + ":CLOSED");
            }
            else if (hosted.started && !hosted.finished)
            {
                tournamentInfo.add(id + ":ACTIVE");
            }
            else if (hosted.finished)
            {
                tournamentInfo.add(id + ":FINISHED");
            }
        }

        return tournamentInfo.toString();
    }
    
    public static void main(String[] args) {
    	SpringApplication.run(TournamentServer.class, args);
    }

}