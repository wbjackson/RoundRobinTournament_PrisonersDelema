package SITS_sprint3;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class TournamentModel
{
    private String serverIP = "";
    private String serverPort = "";

    private final ObservableList<TournamentInfo> tournaments;
    private final ObservableList<String> moves;

    private TournamentInfo selectedTournament;

    private final ViewerClient viewerClient;

    public TournamentModel()
    {
        this.tournaments = FXCollections.observableArrayList();
        this.moves = FXCollections.observableArrayList();

        this.viewerClient = new ViewerClient();
        this.viewerClient.setModel(this);
    }

    public void connectToServer(String ip, String port)
    {
        this.serverIP = ip == null ? "" : ip.trim();
        this.serverPort = port == null ? "" : port.trim();
    }

    public boolean isConnected()
    {
        return !serverIP.isBlank() && !serverPort.isBlank();
    }

    public String getServerIP()
    {
        return serverIP;
    }

    public String getServerPort()
    {
        return serverPort;
    }

    public void fetchTournaments()
    {
        if (!isConnected())
        {
            throw new IllegalStateException("Not connected to server.");
        }

        List<TournamentInfo> fetched =
                viewerClient.fetchTournamentList(serverIP, serverPort);

        tournaments.clear();

        if (fetched != null)
        {
            tournaments.addAll(fetched);
        }
    }

    public ObservableList<TournamentInfo> getObservableTournaments()
    {
        return tournaments;
    }

    public void selectTournament(TournamentInfo tournament)
    {
        this.selectedTournament = tournament;
    }

    public TournamentInfo getSelectedTournament()
    {
        return selectedTournament;
    }

    public void fetchMovesForSelectedTournament()
    {
        if (!isConnected() || selectedTournament == null)
        {
            return;
        }

        List<String> fetched =
                viewerClient.fetchTournamentMoves(
                        serverIP,
                        serverPort,
                        selectedTournament.getId()
                );

        moves.clear();

        if (fetched != null)
        {
            for (String move : fetched)
            {
                addMove(move);
            }
        }
    }

    public void clearMoves()
    {
        moves.clear();
    }

    public void addMove(String move)
    {
        if (move == null || move.isBlank())
        {
            return;
        }

        String cleanedMove = move.trim();

        if (!moves.contains(cleanedMove))
        {
            moves.add(cleanedMove);
        }
    }

    public ObservableList<String> getObservableMoves()
    {
        return moves;
    }

    public void startViewerClient()
    {
        viewerClient.startServer();
    }

    public void stopViewerClient()
    {
        viewerClient.stopServer();
    }

    public void registerForUpdates()
    {
        if (selectedTournament == null)
        {
            throw new IllegalStateException("No tournament selected.");
        }

        viewerClient.registerViewerWithServer(
                serverIP,
                serverPort,
                selectedTournament.getId()
        );
    }

    public void unregisterFromUpdates()
    {
        if (selectedTournament != null && isConnected())
        {
            viewerClient.unregisterViewerFromServer(
                    serverIP,
                    serverPort,
                    selectedTournament.getId()
            );
        }
    }
}