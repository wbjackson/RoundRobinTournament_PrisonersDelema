package SITS_sprint1;

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
