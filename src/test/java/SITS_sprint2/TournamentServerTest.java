package SITS_sprint2;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

public class TournamentServerTest
{
	private static class TestRobot extends Robot
	{
		private String move;
		private String remembered;

		public TestRobot(String name, String move)
		{
			super(name);
			this.move = move;
		}

		@Override
		public String makeMove()
		{
			return move;
		}

		@Override
		public void rememberOpponentMove(String move)
		{
			remembered = move;
		}

		public String getRemembered()
		{
			return remembered;
		}
	}

	private static class TestMoveObserver implements MoveObserver
	{
		String lastMessage;
		int count = 0;

		@Override
		public void updateMove(String moveMessage)
		{
			lastMessage = moveMessage;
			count++;
		}
	}

	private static class TestScoreObserver implements ScoreObserver
	{
		String lastMessage;
		int count = 0;

		@Override
		public void updateScore(String scoreMessage)
		{
			lastMessage = scoreMessage;
			count++;
		}
	}

	@Test
	public void testMoveLoggingSystemUpdateMove()
	{
	    MoveLoggingSystem logger = new MoveLoggingSystem();
	    assertDoesNotThrow(() -> logger.updateMove("test move"));
	}

	@Test
	public void testScoreLoggingSystemUpdateScore()
	{
	    ScoreLoggingSystem logger = new ScoreLoggingSystem();
	    assertDoesNotThrow(() -> logger.updateScore("test score"));
	}
	
	@Test
	public void testGetOpponentName()
	{
		Robot a = new OnlyDefectRobot("A");
		Robot b = new OnlyDefectRobot("B");
		assertEquals("B", a.getOpponentName(b));
	}
	
	@Test
	public void testOnlyDefectRobotAlwaysDefects()
	{
		OnlyDefectRobot robot = new OnlyDefectRobot("D");
		assertEquals("Defect", robot.makeMove());
	}

	@Test
	public void testRandomRobotReturnsValidMove()
	{
		RandomRobot robot = new RandomRobot("R");
		String move = robot.makeMove();
		assertTrue(move.equals("Cooperate") || move.equals("Defect"));
	}

	@Test
	public void testPrisonerSameRobotRemembersOpponentMove()
	{
		PrisonerSameRobot robot = new PrisonerSameRobot("Same");
		assertEquals("Cooperate", robot.makeMove());
		robot.rememberOpponentMove("Defect");
		assertEquals("Defect", robot.makeMove());
	}

	@Test
	public void testPrisonerOppositeRobotRemembersOpponentMove()
	{
		PrisonerOppositeRobot robot = new PrisonerOppositeRobot("Opp");
		assertEquals("Defect", robot.makeMove());
		robot.rememberOpponentMove("Defect");
		assertEquals("Cooperate", robot.makeMove());
	}
	
	@Test
	public void testGameCooperateCooperate()
	{
		PrisonerDelimmaGame game = new PrisonerDelimmaGame(1);
		TestRobot p1 = new TestRobot("A", "Cooperate");
		TestRobot p2 = new TestRobot("B", "Cooperate");
		game.playGame(p1, p2);
		assertEquals(3, p1.getScore());
		assertEquals(3, p2.getScore());
		assertEquals("Cooperate", p1.getRemembered());
		assertEquals("Cooperate", p2.getRemembered());
	}

	@Test
	public void testGameDefectDefect()
	{
		PrisonerDelimmaGame game = new PrisonerDelimmaGame(1);
		TestRobot p1 = new TestRobot("A", "Defect");
		TestRobot p2 = new TestRobot("B", "Defect");
		game.playGame(p1, p2);
		assertEquals(1, p1.getScore());
		assertEquals(1, p2.getScore());
	}

	@Test
	public void testGameDefectCooperate()
	{
		PrisonerDelimmaGame game = new PrisonerDelimmaGame(1);
		TestRobot p1 = new TestRobot("A", "Defect");
		TestRobot p2 = new TestRobot("B", "Cooperate");
		game.playGame(p1, p2);
		assertEquals(5, p1.getScore());
		assertEquals(0, p2.getScore());
	}

	@Test
	public void testGameCooperateDefect()
	{
		PrisonerDelimmaGame game = new PrisonerDelimmaGame(1);
		TestRobot p1 = new TestRobot("A", "Cooperate");
		TestRobot p2 = new TestRobot("B", "Defect");
		game.playGame(p1, p2);
		assertEquals(0, p1.getScore());
		assertEquals(5, p2.getScore());
	}
	
	@Test
	public void testGameReturnsWinner()
	{
		PrisonerDelimmaGame game = new PrisonerDelimmaGame(1);
		TestRobot p1 = new TestRobot("A", "Defect");
		TestRobot p2 = new TestRobot("B", "Cooperate");
		Robot winner = game.playGame(p1, p2);
		assertEquals("A", winner.getName());
	}	

	@Test
	public void testMoveObserverRegistrationAndNotification()
	{
		PrisonerDelimmaGame game = new PrisonerDelimmaGame(1);
		TestMoveObserver observer = new TestMoveObserver();
		game.registerMoveObserver(observer);
		game.playGame(new TestRobot("A", "Cooperate"), new TestRobot("B", "Defect"));
		assertEquals(1, observer.count);
		assertNotNull(observer.lastMessage);
	}

	@Test
	public void testScoreObserverRegistrationAndNotification()
	{
		PrisonerDelimmaGame game = new PrisonerDelimmaGame(1);
		TestScoreObserver observer = new TestScoreObserver();
		game.registerScoreObserver(observer);
		game.playGame(new TestRobot("A", "Cooperate"), new TestRobot("B", "Defect"));
		assertEquals(1, observer.count);
		assertNotNull(observer.lastMessage);
	}

	@Test
	public void testMoveObserverUnregister()
	{
		PrisonerDelimmaGame game = new PrisonerDelimmaGame(1);
		TestMoveObserver observer = new TestMoveObserver();
		game.registerMoveObserver(observer);
		game.unregisterMoveObserver(observer);
		game.playGame(new TestRobot("A", "Cooperate"), new TestRobot("B", "Defect"));
		assertEquals(0, observer.count);
	}

	@Test
	public void testScoreObserverUnregister()
	{
		PrisonerDelimmaGame game = new PrisonerDelimmaGame(1);
		TestScoreObserver observer = new TestScoreObserver();
		game.registerScoreObserver(observer);
		game.unregisterScoreObserver(observer);
		game.playGame(new TestRobot("A", "Cooperate"), new TestRobot("B", "Defect"));
		assertEquals(0, observer.count);
	}
	
	@Test
	public void testRoundRobinTournamentRejectsNullParticipants()
	{
		RoundRobinTournament tournament = new RoundRobinTournament(null, new PrisonerDelimmaGame(1));
		assertThrows(Exception.class, tournament::runTournament);
	}
	
	@Test
	public void testRoundRobinTournamentRejectsTooFewParticipants()
	{
		ArrayList<Robot> participants = new ArrayList<>();
		participants.add(new OnlyDefectRobot("Only"));
		RoundRobinTournament tournament = new RoundRobinTournament(participants, new PrisonerDelimmaGame(1));
		assertThrows(IllegalArgumentException.class, tournament::runTournament);
	}

	
	@Test
	public void testRoundRobinTournamentWinner()
	{
		ArrayList<Robot> participants = new ArrayList<>();
		participants.add(new OnlyDefectRobot("Defector"));
		participants.add(new PrisonerSameRobot("CopyCat"));
		RoundRobinTournament tournament = new RoundRobinTournament(participants, new PrisonerDelimmaGame(1));
		Robot winner = tournament.runTournament();
		assertNotNull(winner);
	}

	@Test
	public void testTournamentServerRegisterClient()
	{
		TournamentServer server = new TournamentServer();
		String result = server.registerClient("Remote1", "localhost", "8080");
		assertEquals("Client registered successfully: Remote1", result);
	}

	@Test
	public void testTournamentServerDuplicateClient()
	{
		TournamentServer server = new TournamentServer();
		server.registerClient("Remote1", "localhost", "8080");
		String result = server.registerClient("Remote1", "localhost", "8080");
		assertEquals("Client already registered.", result);
	}
	
	@Test
	public void testTournamentServerStartTournamentSuccess()
	{
		TournamentServer server = new TournamentServer();
		ArrayList<Robot> participants = new ArrayList<>();
		participants.add(new OnlyDefectRobot("A"));
		participants.add(new PrisonerSameRobot("B"));
		int id = server.createTournament(participants, new PrisonerDelimmaGame(1));
		Robot winner = server.startTournament(id);

		assertNotNull(winner);
	}

	@Test
	public void testTournamentServerCreateTournamentAndView()
	{
		TournamentServer server = new TournamentServer();
		ArrayList<Robot> participants = new ArrayList<>();
		participants.add(new OnlyDefectRobot("A"));
		participants.add(new PrisonerSameRobot("B"));
		int id = server.createTournament(participants, new PrisonerDelimmaGame(1));
		assertEquals(1, id);
		assertTrue(server.viewTournaments().contains("1"));
	}

	@Test
	public void testTournamentServerAddMissingTournament()
	{
		TournamentServer server = new TournamentServer();
		server.registerClient("Remote1", "localhost", "8080");
		String result = server.addClientToTournament("Remote1", 999);
		assertEquals("Tournament not found.", result);
	}

	@Test
	public void testTournamentServerAddClientToTournamentSuccess()
	{
		TournamentServer server = new TournamentServer();
		server.registerClient("Remote1", "localhost", "8080");

		ArrayList<Robot> participants = new ArrayList<>();
		participants.add(new OnlyDefectRobot("A"));
		participants.add(new PrisonerSameRobot("B"));

		int id = server.createTournament(participants, new PrisonerDelimmaGame(1));
		String result = server.addClientToTournament("Remote1", id);

		assertEquals("Client added to tournament.", result);
	}
	
	@Test
	public void testTournamentServerAddMissingClient()
	{
		TournamentServer server = new TournamentServer();
		ArrayList<Robot> participants = new ArrayList<>();
		participants.add(new OnlyDefectRobot("A"));
		participants.add(new PrisonerSameRobot("B"));
		int id = server.createTournament(participants, new PrisonerDelimmaGame(1));
		String result = server.addClientToTournament("Nope", id);

		assertEquals("Client not found.", result);
	}

	@Test
	public void testTournamentServerStartMissingTournament()
	{
		TournamentServer server = new TournamentServer();
		assertThrows(IllegalArgumentException.class, () -> server.startTournament(999));
	}
	
	@Test
	public void testWinnerBranchHigherRecord()
	{
		ArrayList<Robot> participants = new ArrayList<>();

		TestRobot a = new TestRobot("A", "Cooperate");
		TestRobot b = new TestRobot("B", "Defect");
		TestRobot c = new TestRobot("C", "Cooperate");

		participants.add(a);
		participants.add(b);
		participants.add(c);

		AGame game = new AGame(1)
		{
			@Override
			public Robot playGame(Robot p1, Robot p2)
			{
				p1.resetScore();
				p2.resetScore();

				String n1 = p1.getName();
				String n2 = p2.getName();

				if (n1.equals("A") || n2.equals("A"))
				{
					if (n1.equals("A"))
					{
						p1.addScore(1);
					}
					else
					{
						p2.addScore(1);
					}
				}
				else if ((n1.equals("B") && n2.equals("C")) || (n1.equals("C") && n2.equals("B")))
				{
					if (n1.equals("B"))
					{
						p1.addScore(5);
					}
					else
					{
						p2.addScore(5);
					}
				}

				return null;
			}
		};

		RoundRobinTournament tournament = new RoundRobinTournament(participants, game);
		Robot winner = tournament.runTournament();

		assertEquals("B", winner.getName());
	}

	@Test
	public void testWinnerBranchTieBreakByScore()
	{
		ArrayList<Robot> participants = new ArrayList<>();

		TestRobot a = new TestRobot("A", "Cooperate");
		TestRobot b = new TestRobot("B", "Defect");
		TestRobot c = new TestRobot("C", "Cooperate");

		participants.add(a);
		participants.add(b);
		participants.add(c);

		AGame game = new AGame(1)
		{
			@Override
			public Robot playGame(Robot p1, Robot p2)
			{
				p1.resetScore();
				p2.resetScore();

				String n1 = p1.getName();
				String n2 = p2.getName();

				if ((n1.equals("A") && n2.equals("B")) || (n1.equals("B") && n2.equals("A")))
				{
					if (n1.equals("A"))
					{
						p1.addScore(2);
					}
					else
					{
						p2.addScore(2);
					}
				}
				else if ((n1.equals("A") && n2.equals("C")) || (n1.equals("C") && n2.equals("A")))
				{
				}
				else if ((n1.equals("B") && n2.equals("C")) || (n1.equals("C") && n2.equals("B")))
				{
					if (n1.equals("B"))
					{
						p1.addScore(2);
					}
					else
					{
						p2.addScore(2);
					}
				}

				return null;
			}
		};

		RoundRobinTournament tournament = new RoundRobinTournament(participants, game);
		Robot winner = tournament.runTournament();

		assertEquals("B", winner.getName());
	}
	
	@Test
	public void testRemoteClientRobotSuccess()
	{
	    Client client = new Client(); // only if testing controller directly?
	    RemoteClientRobot robot = new RemoteClientRobot("Remote", "localhost", "8080");
	    String move = robot.makeMove();

	    assertTrue(move.equals("Cooperate") || move.equals("Defect"));
	}
	
	@Test
	public void testRemoteClientRobotFallbackOnConnectionFailure()
	{
		RemoteClientRobot robot = new RemoteClientRobot("Remote", "localhost", "9999");
		String move = robot.makeMove();

		assertEquals("Defect", move);
	}
	
	@Test
	public void testRemoteClientRobotRememberOpponentMove()
	{
	    RemoteClientRobot robot = new RemoteClientRobot("Remote", "localhost", "8080");

	    assertDoesNotThrow(() -> robot.rememberOpponentMove("Cooperate"));
	}
	
	@Test
	public void testRemoteClientRobotNullResponseFallsBackToDefect()
	{
	    RemoteClientRobot robot = new RemoteClientRobot("Remote", "localhost", "8080");
	    String move = robot.makeMove();

	    assertEquals("Defect", move);
	}

	@Test
	public void testClientMakeDecision()
	{
	    Client client = new Client();
	    String move = client.makeDecision();
	    assertTrue(move.equals("Cooperate") || move.equals("Defect"));
	}	
	
	@Test
	public void testClientBothOutputs()
	{
	    Client client = new Client();

	    boolean sawCooperate = false;
	    boolean sawDefect = false;

	    for (int i = 0; i < 100; i++)
	    {
	        String move = client.makeDecision();

	        if (move.equals("Cooperate")) sawCooperate = true;
	        if (move.equals("Defect")) sawDefect = true;
	    }

	    assertTrue(sawCooperate && sawDefect);
	}
	
	@Test
	public void testNormalizeResponseNull()
	{
	    RemoteClientRobot robot = new RemoteClientRobot("R", "localhost", "8080");
	    assertEquals("Defect", robot.normalizeResponse(null));
	}

	@Test
	public void testNormalizeResponseCooperate()
	{
	    RemoteClientRobot robot = new RemoteClientRobot("R", "localhost", "8080");
	    assertEquals("Cooperate", robot.normalizeResponse("cooperate"));
	}

	@Test
	public void testNormalizeResponseDefect()
	{
	    RemoteClientRobot robot = new RemoteClientRobot("R", "localhost", "8080");
	    assertEquals("Defect", robot.normalizeResponse("defect"));
	}
}
