package SITS_sprint1;

import java.util.ArrayList;

public abstract class AGame
{

	
	protected ArrayList<ScoreObserver> scoreObservers;
	protected ArrayList<MoveObserver> moveObservers;
	protected int gameRounds;
	public AGame(int gameRounds)
	{
		this.gameRounds = gameRounds;
		this.scoreObservers = new ArrayList<>();
		this.moveObservers = new ArrayList<>();
	}
	
	//Register methods
	public void registerScoreObserver(ScoreObserver observer) {
		scoreObservers.add(observer);
	}
	
	public void registerMoveObserver(MoveObserver observer) {
		moveObservers.add(observer);
	}

	//Unregister methods
	public void unregisterScoreObserver(ScoreObserver observer) {
		scoreObservers.remove(observer);
	}
	
	public void unregisterMoveObserver(MoveObserver observer) {
		moveObservers.remove(observer);
	}
	
	
	
	//Notify methods
	public void notifyScoreObserver(String message) {
		for (ScoreObserver observer : scoreObservers) {
			observer.updateScore(message);
		}
	}
	
	public void notifyMoveObserver(String message) {
		for (MoveObserver observer : moveObservers) {
			observer.updateMove(message);
		}
	}
	
	//Abstract
	public abstract Robot playGame(Robot p1, Robot p2);
}

