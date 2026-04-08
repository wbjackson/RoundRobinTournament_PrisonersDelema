package SITS_sprint2;

import java.io.PrintStream;
import java.util.Scanner;

import SITS_sprint1.Robot;

public class HumanRobot extends Robot
{
    private Scanner scanner;
    private PrintStream out;

    public HumanRobot(String name)
    {
        this(name, new Scanner(System.in), System.out);
    }

    HumanRobot(String name, Scanner scanner, PrintStream out)
    {
        super(name);
        this.scanner = scanner;
        this.out = out;
    }

    @Override
    public String makeMove()
    {
        while (true)
        {
            out.println(getName() + ", enter your move (Cooperate/Defect):");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("Cooperate") || input.equalsIgnoreCase("C"))
            {
                return "Cooperate";
            }

            if (input.equalsIgnoreCase("Defect") || input.equalsIgnoreCase("D"))
            {
                return "Defect";
            }

            out.println("Invalid move. Please enter Cooperate or Defect.");
        }
    }

    @Override
    public void rememberOpponentMove(String move)
    {
        out.println(getName() + ", opponent played: " + move);
    }
}