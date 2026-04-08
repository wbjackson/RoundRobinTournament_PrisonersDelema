package SITS_sprint1;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class ScoreLoggingSystem implements ScoreObserver
{
    private static final String FILE_NAME = "TournamentsResults.txt";

    @Override
    public void updateScore(String scoreInfo)
    {
        try (FileWriter fw = new FileWriter(FILE_NAME, true);
             PrintWriter pw = new PrintWriter(fw))
        {
            pw.println(scoreInfo);
            pw.println(); // spacing after scores
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}