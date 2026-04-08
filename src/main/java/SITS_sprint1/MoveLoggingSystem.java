package SITS_sprint1;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class MoveLoggingSystem implements MoveObserver
{
    private static final String FILE_NAME = "TournamentsResults.txt";

    @Override
    public void updateMove(String moveInfo)
    {
    	//System.out.println("LOG PATH: " + new java.io.File(FILE_NAME).getAbsolutePath());
        try (FileWriter fw = new FileWriter(FILE_NAME, true);
             PrintWriter pw = new PrintWriter(fw))
        {
            pw.println(moveInfo);
        }
        
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}