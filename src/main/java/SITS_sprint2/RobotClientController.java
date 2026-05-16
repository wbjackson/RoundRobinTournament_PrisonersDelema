package SITS_sprint2;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import SITS_sprint1.OnlyDefectRobot;
import SITS_sprint1.PrisonerOppositeRobot;
import SITS_sprint1.PrisonerSameRobot;
import SITS_sprint1.RandomRobot;
import SITS_sprint1.Robot;

@RestController
@RequestMapping("/")
public class RobotClientController
{
    private Map<String, Robot> hostedRobots;

    public RobotClientController()
    {
        hostedRobots = new ConcurrentHashMap<>();
        hostedRobots.put("RemoteHostedRobot", new OnlyDefectRobot("RemoteHostedRobot"));
    }

    @GetMapping("/move/{robotName}")
    public String makeDecision(@PathVariable String robotName)
    {
        if (isInvalidName(robotName))
        {
            return "ERROR: Invalid robot name.";
        }

        Robot robot = hostedRobots.get(robotName);

        if (robot == null)
        {
            return "Defect";
        }

        return robot.makeMove();
    }

    @PostMapping("/remember/{robotName}/{move}")
    public String rememberOpponentMove(@PathVariable String robotName, @PathVariable String move)
    {
        if (isInvalidName(robotName))
        {
            return "ERROR: Invalid robot name.";
        }

        if (move == null || move.isBlank())
        {
            return "ERROR: Invalid move.";
        }

        Robot robot = hostedRobots.get(robotName);

        if (robot == null)
        {
            return "ERROR: No robot found named " + robotName + ".";
        }

        robot.rememberOpponentMove(move);
        return robotName + " remembered: " + move;
    }

    @GetMapping("/robot/set/{robotName}/{type}")
    public String setRobotType(@PathVariable String robotName, @PathVariable String type)
    {
        if (isInvalidName(robotName))
        {
            return "ERROR: Invalid robot name.";
        }

        if (type == null || type.isBlank())
        {
            return "ERROR: Invalid robot type.";
        }

        String cleanedType = type.trim().toLowerCase();
        Robot robot;

        switch (cleanedType)
        {
            case "defector":
            case "defect":
                robot = new OnlyDefectRobot(robotName);
                break;

            case "copycat":
            case "same":
                robot = new PrisonerSameRobot(robotName);
                break;

            case "opposite":
                robot = new PrisonerOppositeRobot(robotName);
                break;

            case "random":
                robot = new RandomRobot(robotName);
                break;

            case "human":
                robot = new UrlHumanRobot(robotName);
                break;

            default:
                return "ERROR: Unknown robot type. Use defector, copycat, opposite, random, or human.";
        }

        hostedRobots.put(robotName, robot);
        return robotName + " set to " + robot.getClass().getSimpleName() + ".";
    }

    @GetMapping("/robot/current/{robotName}")
    public String getCurrentRobot(@PathVariable String robotName)
    {
        if (isInvalidName(robotName))
        {
            return "ERROR: Invalid robot name.";
        }

        Robot robot = hostedRobots.get(robotName);

        if (robot == null)
        {
            return "No robot found named " + robotName + ".";
        }

        return "Current robot: " + robot.getName() + " (" + robot.getClass().getSimpleName() + ")";
    }

    @GetMapping("/robot/all")
    public String getAllRobots()
    {
        if (hostedRobots.isEmpty())
        {
            return "No robots are currently hosted.";
        }

        return hostedRobots.keySet().toString();
    }

    @GetMapping("/human/set/{robotName}/{move}")
    public String setHumanMove(@PathVariable String robotName, @PathVariable String move)
    {
        if (isInvalidName(robotName))
        {
            return "ERROR: Invalid robot name.";
        }

        Robot robot = hostedRobots.get(robotName);

        if (!(robot instanceof UrlHumanRobot))
        {
            return "ERROR: " + robotName + " is not a human URL robot. Use /robot/set/" + robotName + "/human first.";
        }

        UrlHumanRobot human = (UrlHumanRobot) robot;
        return human.setNextMove(move);
    }

    @GetMapping("/human/current/{robotName}")
    public String getHumanMove(@PathVariable String robotName)
    {
        if (isInvalidName(robotName))
        {
            return "ERROR: Invalid robot name.";
        }

        Robot robot = hostedRobots.get(robotName);

        if (!(robot instanceof UrlHumanRobot))
        {
            return "ERROR: " + robotName + " is not a human URL robot.";
        }

        UrlHumanRobot human = (UrlHumanRobot) robot;
        return robotName + " current human move: " + human.getNextMove();
    }


    @GetMapping("/move")
    public String makeDefaultDecision()
    {
        return makeDecision("RemoteHostedRobot");
    }

    @PostMapping("/remember/{move}")
    public String rememberDefaultOpponentMove(@PathVariable String move)
    {
        return rememberOpponentMove("RemoteHostedRobot", move);
    }

    @GetMapping("/robot/set/{type}")
    public String setDefaultRobotType(@PathVariable String type)
    {
        return setRobotType("RemoteHostedRobot", type);
    }

    @GetMapping("/robot/current")
    public String getDefaultCurrentRobot()
    {
        return getCurrentRobot("RemoteHostedRobot");
    }

    @GetMapping("/human/set/{move}")
    public String setDefaultHumanMove(@PathVariable String move)
    {
        return setHumanMove("RemoteHostedRobot", move);
    }

    @GetMapping("/human/current")
    public String getDefaultHumanMove()
    {
        return getHumanMove("RemoteHostedRobot");
    }
    
    @GetMapping("/robot/isHuman/{robotName}")
    public String isHumanRobot(@PathVariable String robotName)
    {
        if (isInvalidName(robotName))
        {
            return "false";
        }

        Robot robot = hostedRobots.get(robotName);

        if (robot == null)
        {
            return "false";
        }

        return String.valueOf(robot.isHumanControlled());
    }
    
    

    public Robot getHostedRobot(String robotName)
    {
        return hostedRobots.get(robotName);
    }

    public void setHostedRobot(String robotName, Robot robot)
    {
        if (!isInvalidName(robotName) && robot != null)
        {
            hostedRobots.put(robotName, robot);
        }
    }

    public Map<String, Robot> getHostedRobots()
    {
        return hostedRobots;
    }

    private boolean isInvalidName(String robotName)
    {
        return robotName == null || robotName.isBlank();
    }
    
    @GetMapping("/human/opponent/{robotName}")
    public String getHumanOpponentMove(@PathVariable String robotName)
    {
        if (isInvalidName(robotName))
        {
            return "ERROR: Invalid robot name.";
        }

        Robot robot = hostedRobots.get(robotName);

        if (!(robot instanceof UrlHumanRobot))
        {
            return "ERROR: " + robotName + " is not a human URL robot.";
        }

        UrlHumanRobot human = (UrlHumanRobot) robot;
        return robotName + " last opponent move: " + human.getLastOpponentMove();
    }
    
}

