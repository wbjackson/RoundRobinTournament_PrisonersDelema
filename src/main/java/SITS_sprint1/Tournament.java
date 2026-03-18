package SITS_sprint1;

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
	
	public abstract Robot runTournament();

}
