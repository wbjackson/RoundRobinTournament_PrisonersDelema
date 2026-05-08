package SITS_sprint2;

import org.springframework.web.bind.annotation.*;

import SITS_sprint1.OnlyDefectRobot;
import SITS_sprint1.Robot;

@RestController
@RequestMapping("/")
public class RobotClientController
{
    private Robot hostedRobot;

    public RobotClientController()
    {
        this.hostedRobot = new OnlyDefectRobot("RemoteHostedRobot");
    }

    public RobotClientController(Robot hostedRobot)
    {
        this.hostedRobot = hostedRobot;
    }

    @GetMapping("/move")
    public String makeDecision()
    {
        if (hostedRobot == null)
        {
            return "ERROR: No robot available";
        }

        return hostedRobot.makeMove();
    }

    @PostMapping("/remember/{move}")
    public String rememberOpponentMove(@PathVariable String move)
    {
        if (hostedRobot == null)
        {
            return "ERROR: No robot available";
        }

        if (move == null || move.isEmpty())
        {
            return "ERROR: Invalid move";
        }

        hostedRobot.rememberOpponentMove(move);
        return "Remembered: " + move;
    }

    public Robot getHostedRobot()
    {
        return hostedRobot;
    }

    public void setHostedRobot(Robot robot)
    {
        this.hostedRobot = robot;
    }
}