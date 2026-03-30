package SITS_sprint2;

import java.util.ArrayList;

public abstract class Tournament
{
	public ArrayList<Robot> participants;
	public AGame game;
	
	public Tournament(ArrayList<Robot> participants, AGame game)
	{
		this.participants = participants;
		this.game = game;
	}
	
	public ArrayList<Robot> getParticipants()
	{
		return participants;
	}
	
	public abstract Robot runTournament();
}