package SITS_sprint3;

import java.util.Objects;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

public class TournamentsViewController
{
    private TournamentModel model;
    private RemoteClientViewerApp app;

    @FXML
    private TextField ipField;

    @FXML
    private TextField portField;

    @FXML
    private ListView<TournamentInfo> tournamentList;

    @FXML
    private Button connectButton;

    @FXML
    private Button refreshButton;

    public void setModel(TournamentModel model)
    {
        this.model = model;

        if (tournamentList != null)
        {
            tournamentList.setItems(model.getObservableTournaments());
        }
    }

    public void setApp(RemoteClientViewerApp app)
    {
        this.app = app;
    }

    @FXML
    public void initialize()
    {
        if (tournamentList != null)
        {
            tournamentList.setCellFactory(listView -> new TournamentCell());
        }
    }

    @FXML
    public void handleConnect()
    {
        try
        {
            String ip = ipField.getText();
            String port = portField.getText();

            if (ip == null || ip.trim().isEmpty())
            {
                System.out.println("IP address is required.");
                return;
            }

            if (port == null || port.trim().isEmpty())
            {
                System.out.println("Port number is required.");
                return;
            }

            model.connectToServer(ip, port);
            model.startViewerClient();
            model.fetchTournaments();

            tournamentList.setItems(model.getObservableTournaments());
            tournamentList.refresh();
        }
        catch (Exception e)
        {
            System.out.println("Connect error: " + e.getMessage());
        }
    }
    
    @FXML
    public void handleRefresh()
    {
        try
        {
            if (model == null || !model.isConnected())
            {
                System.out.println("Cannot refresh. Not connected to server.");
                return;
            }

            model.fetchTournaments();
            tournamentList.setItems(model.getObservableTournaments());
            tournamentList.refresh();
        }
        catch (Exception e)
        {
            System.out.println("Refresh error: " + e.getMessage());
        }
    }
    //
    //ournament typre atm is binary 0 - > Move View "Wanna join this Tournament?", currently a viewer of the 
    //									  1 - > Move View, CLOSED TOURNAMENT, Actively updating tournament.
    // Case i want to solve:
    //I want to join an active registration T, join, wait for CLOSE, then perform what ever action yaddah yaddah yaddah
    //create default endpoint messages for no tournaments in the listView.
    // 
    // 
    public void refreshViewState()
    {
        if (model == null)
        {
            return;
        }

        if (ipField != null && model.getServerIP() != null)
        {
            ipField.setText(model.getServerIP());
        }

        if (portField != null && model.getServerPort() != null)
        {
            portField.setText(model.getServerPort());
        }

        if (tournamentList != null)
        {
            tournamentList.setItems(model.getObservableTournaments());
            tournamentList.refresh();
        }
    }

    private void handleSelectTournament(TournamentInfo tournament)
    {
        if (tournament == null)
        {
            return;
        }

        try
        {
            model.selectTournament(tournament);
            model.fetchMovesForSelectedTournament();
            model.registerForUpdates();
            app.switchToMoveView();
        }
        catch (Exception e)
        {
            System.out.println("Open tournament error: " + e.getMessage());
        }
    }

    private final class TournamentCell extends ListCell<TournamentInfo>
    {
        private final Text text = new Text();
        private final Button viewButton = new Button("View Tournament");
        private final Region spacer = new Region();
        private final HBox container = new HBox(10, text, spacer, viewButton);

        private TournamentCell()
        {
            HBox.setHgrow(spacer, Priority.ALWAYS);

            viewButton.setOnAction(event ->
            {
                TournamentInfo item = getItem();
                if (item != null)
                {
                    handleSelectTournament(item);
                }
            });
        }

        @Override
        protected void updateItem(TournamentInfo item, boolean empty)
        {
            super.updateItem(item, empty);

            if (empty || Objects.isNull(item))
            {
                setText(null);
                setGraphic(null);
                return;
            }

            text.setText(item.toString());
            viewButton.setDisable(false);

            setText(null);
            setGraphic(container);
        }
    }
}