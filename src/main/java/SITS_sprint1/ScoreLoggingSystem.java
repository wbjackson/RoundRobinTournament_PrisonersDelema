package SITS_sprint1;

public class ScoreLoggingSystem implements ScoreObserver
{
    @Override
    public void updateScore(String scoreMessage) {
        System.out.println("[SCORE LOG] " + scoreMessage);	
    }
}
