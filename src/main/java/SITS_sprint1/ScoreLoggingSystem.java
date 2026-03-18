package SITS_sprint1;

public class ScoreLoggingSystem implements ScoreObserver
{
    @Override
    public void updateScore(String scoreMessage) {
        // Example: log to console (you can later switch to file logging)
        System.out.println("[SCORE LOG] " + scoreMessage);	
    }
}
