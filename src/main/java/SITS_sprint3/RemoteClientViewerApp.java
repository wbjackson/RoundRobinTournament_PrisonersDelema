package SITS_sprint3;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class RemoteClientViewerApp extends Application
{
    private static final String TOURNAMENTS_FXML = "/tournaments-view.fxml";
    private static final String MOVE_FXML = "/move-view.fxml";

    private Stage primaryStage;
    private Scene tournamentsScene;
    private Scene moveScene;

    private TournamentModel model;
    private TournamentsViewController tournamentsViewController;
    private MoveViewController moveViewController;

    @Override
    public void start(Stage stage) throws IOException
    {
        this.primaryStage = stage;
        this.model = new TournamentModel();

        
        loadScenes();

        
        primaryStage.setTitle("Remote Client Viewer");
        primaryStage.setScene(tournamentsScene);
        primaryStage.show();
    }

    private void loadScenes() throws IOException
    {
    	System.out.println(getClass().getResource("/SITS_sprint3/move-view.fxml"));
        FXMLLoader tournamentsLoader =
                new FXMLLoader(getClass().getResource(TOURNAMENTS_FXML));
        Parent tournamentsRoot = tournamentsLoader.load();
        tournamentsViewController = tournamentsLoader.getController();
        tournamentsViewController.setModel(model);
        tournamentsViewController.setApp(this);

        FXMLLoader moveLoader =
                new FXMLLoader(getClass().getResource(MOVE_FXML));
        Parent moveRoot = moveLoader.load();
        moveViewController = moveLoader.getController();
        moveViewController.setModel(model);
        moveViewController.setApp(this);

        tournamentsScene = new Scene(tournamentsRoot);
        moveScene = new Scene(moveRoot);
        System.out.println(getClass().getResource("/SITS_sprint3/move-view.fxml"));
    }

    public void switchToMoveView()
    {
        moveViewController.refreshViewState();
        primaryStage.setScene(moveScene);
    }

    public void switchToTournamentsView()
    {
        tournamentsViewController.refreshViewState();
        primaryStage.setScene(tournamentsScene);
    }

    @Override
    public void stop()
    { 
        if (model != null)
        {
            model.unregisterFromUpdates();
            model.stopViewerClient();
        }
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}