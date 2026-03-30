package SITS_sprint2;

import SITS_sprint2.MoveObserver;

public class MoveLoggingSystem implements MoveObserver {

    @Override
    public void updateMove(String moveMessage) {
        System.out.println("[MOVE LOG] " + moveMessage);
    }
}
