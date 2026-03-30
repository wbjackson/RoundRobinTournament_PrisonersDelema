package SITS_sprint1;

public class PrisonerOppositeRobot extends Robot {

    public String oppsPrevMove = "Cooperate";

    public PrisonerOppositeRobot(String name) {
        super(name);
    }

    @Override
    public String makeMove() {

        if (oppsPrevMove.equalsIgnoreCase("Cooperate")) {
            return "Defect";
        }

        return "Cooperate";
    }
    @Override
    public void rememberOpponentMove(String move) {
    	oppsPrevMove = move;
    }
}