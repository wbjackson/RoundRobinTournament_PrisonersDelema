package SITS_sprint2;

import SITS_sprint1.Robot;

public class UrlHumanRobot extends Robot
{
    private String nextMove;
    private String lastOpponentMove;

    public UrlHumanRobot(String name)
    {
        super(name);
        this.nextMove = null;
        this.lastOpponentMove = "No opponent move yet.";
    }

    @Override
    public boolean isHumanControlled()
    {
        return true;
    }

    @Override
    public synchronized String makeMove()
    {
        System.out.println("Waiting for " + getName() + " to make a move.");
        System.out.println("Use /human/set/" + getName() + "/Cooperate or /human/set/" + getName() + "/Defect");

        while (nextMove == null)
        {
            try
            {
                wait();
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
                return "Defect";
            }
        }

        String moveToReturn = nextMove;
        nextMove = null;

        System.out.println(getName() + " played: " + moveToReturn);
        return moveToReturn;
    }

    @Override
    public synchronized void rememberOpponentMove(String move)
    {
        if (move == null || move.isBlank())
        {
            lastOpponentMove = "Unknown";
        }
        else
        {
            lastOpponentMove = move;
        }

        System.out.println(getName() + " remembered opponent move: " + lastOpponentMove);
    }

    public synchronized String setNextMove(String move)
    {
        if (move == null)
        {
            return "ERROR: Invalid human move.";
        }

        if (move.equalsIgnoreCase("Cooperate") || move.equalsIgnoreCase("C"))
        {
            nextMove = "Cooperate";
            notifyAll();
            return getName() + " move set to Cooperate.";
        }

        if (move.equalsIgnoreCase("Defect") || move.equalsIgnoreCase("D"))
        {
            nextMove = "Defect";
            notifyAll();
            return getName() + " move set to Defect.";
        }

        return "ERROR: Human move must be Cooperate or Defect.";
    }

    public synchronized String getNextMove()
    {
        if (nextMove == null)
        {
            return "No move currently submitted.";
        }

        return nextMove;
    }

    public synchronized String getLastOpponentMove()
    {
        return lastOpponentMove;
    }
}