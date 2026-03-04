package SITS_sprint1;

public class MoveLoggingSystem implements MoveObserver {

    @Override
    public void updateMove(String moveMessage) {
        System.out.println("[MOVE LOG] " + moveMessage);
    }
}