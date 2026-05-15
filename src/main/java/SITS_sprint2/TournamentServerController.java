package SITS_sprint2;

import org.springframework.web.bind.annotation.*;

import SITS_sprint1.AGame;
import SITS_sprint1.OnlyDefectRobot;
import SITS_sprint1.PrisonerDelimmaGame;
import SITS_sprint1.PrisonerSameRobot;
import SITS_sprint1.Robot;

import java.util.ArrayList;

@RestController
@RequestMapping("/server")
public class TournamentServerController
{										//REST controller for TounramentServer URLs
    private TournamentServer server;

    public TournamentServerController()
    {
        this.server = new TournamentServer();
    }

    public TournamentServerController(TournamentServer server)
    {
        this.server = server;
    }

    @GetMapping("/tournaments")
    public String viewTournaments()
    {
        return server.viewTournaments();
    }

    @GetMapping("/moves/{tournamentId}")
    public String getTournamentMoves(@PathVariable int tournamentId)
    {
        return server.getTournamentMoves(tournamentId);
    }

    @GetMapping("/register/{name}/{ip}/{port}")
    public String registerClient(@PathVariable String name,
                                 @PathVariable String ip,
                                 @PathVariable String port)
    {
        if (name == null || name.isEmpty())
        {
            return "Invalid client name.";
        }

        if (ip == null || ip.isEmpty())
        {
            return "Invalid IP address.";
        }

        if (port == null || port.isEmpty())
        {
            return "Invalid port.";
        }

        return server.registerClient(name, ip, port);
    }

    @GetMapping("/create")
    public String createTournament()
    {
        ArrayList<Robot> participants = new ArrayList<>();
        participants.add(new OnlyDefectRobot("Defector"));
        participants.add(new PrisonerSameRobot("CopyCat"));

        AGame game = new PrisonerDelimmaGame(3);

        int id = server.createTournament(participants, game);
        return "Tournament created: " + id;
    }

    @GetMapping("/join/{clientName}/{tournamentId}")
    public String addClientToTournament(@PathVariable String clientName,
                                        @PathVariable int tournamentId)
    {
        if (clientName == null || clientName.isEmpty())
        {
            return "Invalid client name.";
        }

        return server.addClientToTournament(clientName, tournamentId);
    }

    @GetMapping("/close/{tournamentId}")
    public String closeRegistration(@PathVariable int tournamentId)
    {
        return server.closeRegistration(tournamentId);
    }

    @GetMapping("/start/{tournamentId}")
    public String startTournament(@PathVariable int tournamentId)
    {
        try
        {
            Robot winner = server.startTournament(tournamentId);

            if (winner == null)
            {
                return "Tournament started.";
            }

            return "Winner: " + winner.getName();
        }
        catch (IllegalArgumentException | IllegalStateException e)
        {
            return e.getMessage();
        }
    }

    public TournamentServer getServer()
    {
        return server;
    }

    public void setServer(TournamentServer server)
    {
        this.server = server;
    }
}