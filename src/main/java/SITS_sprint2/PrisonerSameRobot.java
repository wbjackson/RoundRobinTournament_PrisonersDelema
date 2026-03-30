package SITS_sprint2;

import SITS_sprint2.Robot;

public class PrisonerSameRobot extends Robot {

    public String oppsPrevMove = "Cooperate";

    public PrisonerSameRobot(String name) {
        super(name);
        
    }

    @Override
    public String makeMove() {
        return oppsPrevMove;
    }
    @Override
    public void rememberOpponentMove(String move) {
    	oppsPrevMove = move;
    }
}
