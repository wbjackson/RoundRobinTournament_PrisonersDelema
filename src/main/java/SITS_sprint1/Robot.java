package SITS_sprint1;

public abstract class Robot
{
	public String name;
	public int score;
	public int record;
	//public int oppsLastMove;

	public Robot(String name)
	{
		this.name = name;
		this.score = 0;
		this.record = 0;
	}

	//UML: getOpponentName(r: Robot): String
	public String getOpponentName(Robot r) {
		return r.name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getScore() { 
		return score;
	}
	
	public void addScore(int points) {
		score += points;
	}
	public void addRecord(int points) {
		record += points;
	}
	
	public void resetScore() {
		score = 0;
	}
	public abstract String makeMove();
	
	public abstract void rememberOpponentMove(String move);
}
