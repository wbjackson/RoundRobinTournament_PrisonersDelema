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

        game.notifyMoveObserver("=====================================");
        game.notifyMoveObserver("TOURNAMENT START: Round Robin Tournament");
        game.notifyMoveObserver("=====================================");
        game.notifyMoveObserver("");

        for (Robot r : participants) {
            r.resetRecord();
        }

        for (int i = 0; i < participants.size(); i++) {
            for (int j = i + 1; j < participants.size(); j++) {

                Robot p1 = participants.get(i);
                Robot p2 = participants.get(j);

                game.notifyMoveObserver("MATCH: " + p1.getName() + " vs " + p2.getName());

                game.playGame(p1, p2);

                p1.addRecord(p1.getScore());
                p2.addRecord(p2.getScore());

                game.notifyScoreObserver(
                    "TOURNAMENT RECORD UPDATE: " + p1.getName() + "=" + p1.getRecord()
                    + ", " + p2.getName() + "=" + p2.getRecord()
                );
            }
        }

        Robot best = participants.get(0);

        for (Robot r : participants) {
            if (r.getRecord() > best.getRecord()) {
                best = r;
            } else if (r.getRecord() == best.getRecord() &&
                       r.getScore() > best.getScore()) {
                best = r;
            }
        }

        game.notifyScoreObserver("=====================================");
        game.notifyScoreObserver(
            "TOURNAMENT RESULT: Winner = " + best.getName() +
            ", Record = " + best.getRecord() +
            ", Score = " + best.getScore()
        );
        game.notifyScoreObserver("=====================================");

        return best;
    }
}