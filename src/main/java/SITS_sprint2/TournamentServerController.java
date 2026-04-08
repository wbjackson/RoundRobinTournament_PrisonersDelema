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
{
    private TournamentServer server = new TournamentServer();

    @GetMapping("/tournaments")
    public String viewTournaments()
    {
        return server.viewTournaments();
    }

    @PostMapping("/register/{name}/{ip}/{port}")
    public String registerClient(@PathVariable String name,
                                 @PathVariable String ip,
                                 @PathVariable String port)
    {
        return server.registerClient(name, ip, port);
    }

    @PostMapping("/create")
    public String createTournament()
    {
        ArrayList<Robot> participants = new ArrayList<>();
        participants.add(new OnlyDefectRobot("Defector"));
        participants.add(new PrisonerSameRobot("CopyCat"));

        AGame game = new PrisonerDelimmaGame(3);

        int id = server.createTournament(participants, game);
        return "Tournament created: " + id;
    }

    @PostMapping("/join/{clientName}/{tournamentId}")
    public String addClientToTournament(@PathVariable String clientName,
                                        @PathVariable int tournamentId)
    {
        return server.addClientToTournament(clientName, tournamentId);
    }

    @PostMapping("/close/{tournamentId}")
    public String closeRegistration(@PathVariable int tournamentId)
    {
        return server.closeRegistration(tournamentId);
    }

    @PostMapping("/start/{tournamentId}")
    public String startTournament(@PathVariable int tournamentId)
    {
        Robot winner = server.startTournament(tournamentId);
        return "Winner: " + winner.getName();
    }
}