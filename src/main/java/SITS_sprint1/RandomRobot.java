package SITS_sprint1;

public class RandomRobot extends Robot {

    public RandomRobot(String name) {
        super(name);
    }

    public String makeMove() {
        return Math.random() < 0.5 ? "Cooperate" : "Defect";
    }

    public void rememberOpponentMove(String move) {}
}