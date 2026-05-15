package SITS_sprint3;

import SITS_sprint4.CommandInvoker;
import SITS_sprint4.ExitMoveViewCommand;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.util.Duration;

public class MoveViewController
{
    private TournamentModel model;
    private RemoteClientViewerApp app;

    private CommandInvoker commandInvoker;	 

    private Timeline refreshTimeline;

    @FXML
    private Label tournamentTitleLabel;

    @FXML
    private ListView<String> moveList;

    @FXML
    private Button exitButton;

    public void setModel(TournamentModel model)
    {
        this.model = model;

        if (moveList != null && model != null)
        {
            moveList.setItems(model.getObservableMoves());
        }

        startAutoRefresh();
    }

    public void setApp(RemoteClientViewerApp app)
    {
        this.app = app;
    }

    @FXML
    public void initialize()
    {
        commandInvoker = new CommandInvoker();
    }

    private void startAutoRefresh()
    {
        if (refreshTimeline != null)
        {
            return;
        }

        refreshTimeline = new Timeline(
            new KeyFrame(Duration.seconds(1), e -> {
                if (model != null && model.isConnected())
                {
                    model.fetchMovesForSelectedTournament();
                }
            })
        );

        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }

    private void stopAutoRefresh()
    {
        if (refreshTimeline != null)
        {
            refreshTimeline.stop();
            refreshTimeline = null;
        }
    }

    public void refreshViewState()
    {
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
            else if (selected.getName() == null || selected.getName().isEmpty())
            {
                tournamentTitleLabel.setText("Tournament: Unknown");
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
        stopAutoRefresh();

        try
        {
            commandInvoker.setCommand(new ExitMoveViewCommand(model, app));
            commandInvoker.executeCommand();
        }
        catch (Exception e)
        {
            if (app != null)
            {
                app.switchToTournamentsView();
            }
        }
    }

    public Timeline getRefreshTimeline()
    {
        return refreshTimeline;
    }
}
