
package SITS_sprint2;

import org.springframework.web.bind.annotation.*;
import SITS_sprint1.OnlyDefectRobot;
import SITS_sprint1.Robot;

@RestController
@RequestMapping("/")
public class Client
{
    private Robot hostedRobot;

    public Client()
    {
        this.hostedRobot = new OnlyDefectRobot("RemoteHostedRobot");
    }

    public Client(Robot hostedRobot)
    {
        this.hostedRobot = hostedRobot;
    }

    public void setHostedRobot(Robot hostedRobot)
    {
        this.hostedRobot = hostedRobot;
    }

    public Robot getHostedRobot()
    {
        return hostedRobot;
    }

    @GetMapping("/move")
    public String makeDecision()
    {
        return hostedRobot.makeMove();
    }

    @PostMapping("/remember/{move}")
    public String rememberOpponentMove(@PathVariable String move)
    {
        hostedRobot.rememberOpponentMove(move);
        return "Remembered: " + move;
    }
}