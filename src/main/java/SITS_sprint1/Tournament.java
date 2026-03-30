package SITS_sprint1;

import java.util.ArrayList;
import java.util.List;


public abstract class Tournament
{
	public ArrayList<Robot> participants;
	public AGame game;
	
	public Tournament(ArrayList<Robot> participants, AGame game)
	{
		this.participants = participants;
		this.game = game;
	}
	
	public abstract Robot runTournament();

	public List<Robot> getParticipants()
	{
		// TODO Auto-generated method stub
		return this.participants;
	}

}
