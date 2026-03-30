package SITS_sprint1;

public class PrisonerDelimmaGame extends AGame
{
	public PrisonerDelimmaGame(int rounds)
	{
		super(rounds);
	}

	@Override
	public Robot playGame(Robot p1, Robot p2)
	{
		p1.resetScore();
		p2.resetScore();

		for (int i = 0; i < gameRounds; i++)
			{
				String move1 = p1.makeMove();
				String move2 = p2.makeMove();

				notifyMoveObserver("Round " + (i + 1) + ": " + p1.getName() + " -> " + move1 + ", " + p2.getName()
						+ " -> " + move2);

				applyPayoff(p1, p2, move1, move2);

				notifyScoreObserver("After round " + (i + 1) + ": " + p1.getName() + "=" + p1.getScore() + ", "
						+ p2.getName() + "=" + p2.getScore());

				p1.rememberOpponentMove(move2);
				p2.rememberOpponentMove(move1);
			}

		return (p1.getScore() >= p2.getScore()) ? p1 : p2;
	}

	private void applyPayoff(Robot p1, Robot p2, String move1, String move2)
	{
		boolean c1 = move1.equalsIgnoreCase("Cooperate");
		boolean c2 = move2.equalsIgnoreCase("Cooperate");

		if (c1 && c2)
			{
				p1.addScore(3);
				p2.addScore(3);
			} else if (!c1 && !c2)
			{
				p1.addScore(1);
				p2.addScore(1);
			} else if (!c1 && c2)
			{
				p1.addScore(5);
			} else
			{
				p2.addScore(5);
			}
	}

}
