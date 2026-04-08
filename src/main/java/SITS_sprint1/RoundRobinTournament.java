package SITS_sprint1;
import java.util.ArrayList;

public class RoundRobinTournament extends Tournament
{
	public RoundRobinTournament(ArrayList<Robot> participants, AGame game)
	{
		super(participants, game);
	}
	
	@Override
	public Robot runTournament() {
		
	    if (participants == null || participants.size() < 2) {
	        throw new IllegalArgumentException("Not enough participants");
	    }

		for (MoveObserver observer : game.moveObservers) {
			observer.updateMove("=====================================");
			observer.updateMove("TOURNAMENT START: Round Robin Tournament");
			observer.updateMove("=====================================");
			observer.updateMove("");
		}

		for (Robot r : participants) {
		    r.record = 0;
		}

	    System.out.println(participants.size());

	    for (int i = 0; i < participants.size(); i++) {
	        for (int j = i + 1; j < participants.size(); j++) {

	            Robot p1 = participants.get(i);
	            Robot p2 = participants.get(j);

	    		for (MoveObserver observer : game.moveObservers) {
	    			observer.updateMove("MATCH: " + p1.getName() + " vs " + p2.getName());
	    		}

	            game.playGame(p1, p2);

	            p1.addRecord(p1.getScore());
	            p2.addRecord(p2.getScore());

	    		for (ScoreObserver observer : game.scoreObservers) {
	    			observer.updateScore("TOURNAMENT RECORD UPDATE: " + p1.getName() + "=" + p1.getRecord()
	    					+ ", " + p2.getName() + "=" + p2.getRecord());
	    		}
	        }
	    }

	    Robot best = participants.get(0);
	    for (Robot r : participants) {
	        if (r.record > best.record) {
	            best = r;
	        } else if (r.record == best.record && r.score > best.score) {
	            best = r;
	        }
	    }

		for (ScoreObserver observer : game.scoreObservers) {
			observer.updateScore("=====================================");
			observer.updateScore("TOURNAMENT RESULT: Winner = " + best.getName() + ", Record = " + best.getRecord()
					+ ", Score = " + best.getScore());
			observer.updateScore("=====================================");
		}

	    return best;
	}
}