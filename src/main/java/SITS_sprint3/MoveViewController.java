package SITS_sprint3;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class MoveViewController
{
    private TournamentModel model;
    private RemoteClientViewerApp app;

    @FXML
    private Label tournamentTitleLabel;

    @FXML
    private ListView<String> moveList;

    @FXML
    private Button exitButton;

    public void setModel(TournamentModel model)
    {
        this.model = model;

        if (moveList != null)
        {
            moveList.setItems(model.getObservableMoves());
        }
    }

    public void setApp(RemoteClientViewerApp app)
    {
        this.app = app;
    }

    @FXML
    public void initialize()
    {
        // no extra setup needed yet
    }

    public void refreshViewState()
    {
    	
//    	model.addMove("Round 1: A cooperated");
//    	model.addMove("Round 1: B defected");
//    	model.addMove("Round 2: A defected");
//    	model.addMove("Round 2: B defected");
//
//    	// duplicate test (should NOT appear twice)
//    	model.addMove("Round 1: A cooperated");
        if (model == null)
        {
            return;
        }

        if (moveList != null && moveList.getItems() != model.getObservableMoves())
        {
            moveList.setItems(model.getObservableMoves());
        }

        if (tournamentTitleLabel != null)
        {
            TournamentInfo selected = model.getSelectedTournament();

            if (selected == null)
            {
                tournamentTitleLabel.setText("Tournament: None Selected");
            }
            else
            {
                tournamentTitleLabel.setText("Tournament: " + selected.getName());
            }
        }
    }

    @FXML
    public void handleExit()
    {
        try
        {
            if (model != null)
            {
                model.unregisterFromUpdates();

                if (model.isConnected())
                {
                    model.fetchTournaments();
                }
            }

            if (app != null)
            {
                app.switchToTournamentsView();
            }
        }
        catch (Exception e)
        {
            System.out.println("Exit error: " + e.getMessage());

            if (app != null)
            {
                app.switchToTournamentsView();
            }
        }
    }
}

