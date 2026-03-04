package SITS_sprint1;

import java.util.ArrayList;

public class RoundRobinTournament extends Tournament
{

	public RoundRobinTournament(ArrayList<Robot> participants, AGame game)
	{
		super(participants, game);
	}

	@Override
	public Robot runTournament()
	{
		if (participants == null || participants.size()< 2) {
			String message = "not enough participants";
			System.out.println(message);
			return null; //maybe throw exception
		}else {
			System.out.println(participants.size());
		}
		
		for (int i = 0; i < participants.size(); i++) {
			
			for (int j = i + 1; j < participants.size(); j++) {
				
				Robot p1 = participants.get(i);
				Robot p2 = participants.get(j);
				

				game.playGame(p1, p2);

				p1.addRecord(p1.getScore());
				p2.addRecord(p2.getScore());
			}
		}
	
	
		
		Robot best = participants.get(0);
		
		for (Robot r : participants) {
			if (r.record > best.record) {
				best = r;
			}else if (r.record == best.record && r.score > best.score) { 
				best = r;
			}
		}
		return best;
	}
}
