package SITS_sprint3;

import static org.junit.jupiter.api.Assertions.*;

import java.io.OutputStream;
import java.net.InetSocketAddress;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import com.sun.net.httpserver.HttpServer;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

public class MoveViewControllerTest extends ApplicationTest
{
    private TournamentModel model;
    private MoveViewController controller;
    private HttpServer server;

    @Override
    public void start(Stage stage) throws Exception
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/move-view.fxml"));
        Parent root = loader.load();

        controller = loader.getController();
        model = new TournamentModel();

        controller.setModel(model);
        controller.setApp(null);

        stage.setScene(new Scene(root));
        stage.show();
    }

    @AfterEach
    void cleanup()
    {
        if (server != null)
        {
            server.stop(0);
            server = null;
        }

        if (model != null)
        {
            model.stopViewerClient();
        }
    }

    @Test
    void testViewLoadsRequiredControls()
    {
        assertNotNull(lookup("#tournamentTitleLabel").queryAs(Label.class));
        assertNotNull(lookup("#moveList").queryAs(ListView.class));
        assertNotNull(lookup("#exitButton").queryButton());
    }

    @Test
    void testMoveListBindsToModelMoves()
    {
        interact(() ->
        {
            model.addMove("Move A");
            model.addMove("Move B");
        });

        ListView<?> moveList = lookup("#moveList").queryAs(ListView.class);

        assertEquals(2, moveList.getItems().size());
        assertTrue(moveList.getItems().contains("Move A"));
        assertTrue(moveList.getItems().contains("Move B"));
    }

    @Test
    void testRefreshViewStateWithNoSelectedTournament()
    {
        interact(() -> controller.refreshViewState());

        Label title = lookup("#tournamentTitleLabel").queryAs(Label.class);

        assertEquals("Tournament: None Selected", title.getText());
    }

    @Test
    void testRefreshViewStateWithSelectedTournament()
    {
        interact(() ->
        {
            model.selectTournament(new TournamentInfo(1, "Test Tournament", true, false));
            controller.refreshViewState();
        });

        Label title = lookup("#tournamentTitleLabel").queryAs(Label.class);

        assertEquals("Tournament: Test Tournament", title.getText());
    }

    @Test
    void testFetchMovesUpdatesListView() throws Exception
    {
        startMoveServer("[Round 1: A -> Defect, Round 1: B -> Cooperate]");

        interact(() ->
        {
            model.connectToServer("localhost", String.valueOf(server.getAddress().getPort()));
            model.selectTournament(new TournamentInfo(1, "Live Test", true, false));
            model.fetchMovesForSelectedTournament();
            controller.refreshViewState();
        });

        ListView<?> moveList = lookup("#moveList").queryAs(ListView.class);

        assertEquals(2, moveList.getItems().size());
        assertTrue(moveList.getItems().contains("Round 1: A -> Defect"));
        assertTrue(moveList.getItems().contains("Round 1: B -> Cooperate"));
    }

    @Test
    void testLiveRefreshDoesNotCrashWhenConnected() throws Exception
    {
        startMoveServer("[Move A]");

        interact(() ->
        {
            model.connectToServer("localhost", String.valueOf(server.getAddress().getPort()));
            model.selectTournament(new TournamentInfo(1, "Live Test", true, false));
            controller.refreshViewState();
        });

        sleep(1200);

        assertTrue(model.getObservableMoves().contains("Move A"));
    }

    @Test
    void testHandleExitDoesNotCrashWithoutApp()
    {
        assertDoesNotThrow(() -> clickOn("#exitButton"));
    }

    @Test
    void testHandleExitRefreshesTournamentsIfConnected() throws Exception
    {
        server = HttpServer.create(new InetSocketAddress(0), 0);

        server.createContext("/server/tournaments", exchange ->
        {
            String response = "[1:FINISHED]";
            byte[] bytes = response.getBytes();

            exchange.sendResponseHeaders(200, bytes.length);

            try (OutputStream os = exchange.getResponseBody())
            {
                os.write(bytes);
            }
        });

        server.createContext("/server/moves/1", exchange ->
        {
            String response = "[Move A]";
            byte[] bytes = response.getBytes();

            exchange.sendResponseHeaders(200, bytes.length);

            try (OutputStream os = exchange.getResponseBody())
            {
                os.write(bytes);
            }
        });

        server.start();

        interact(() ->
        {
            model.connectToServer("localhost", String.valueOf(server.getAddress().getPort()));
            model.selectTournament(new TournamentInfo(1, "Exit Test", true, false));
        });

        clickOn("#exitButton");

        assertEquals(1, model.getObservableTournaments().size());
        assertEquals(1, model.getObservableTournaments().get(0).getId());
    }

    private void startMoveServer(String moveResponse) throws Exception
    {
        server = HttpServer.create(new InetSocketAddress(0), 0);

        server.createContext("/server/moves/1", exchange ->
        {
            byte[] bytes = moveResponse.getBytes();

            exchange.sendResponseHeaders(200, bytes.length);

            try (OutputStream os = exchange.getResponseBody())
            {
                os.write(bytes);
            }
        });

        server.start();
    }
    
    @Test
    void testGetRefreshTimelineAfterRefreshViewState()
    {
        interact(() -> controller.refreshViewState());

        assertNotNull(controller.getRefreshTimeline());
    }

    @Test
    void testRefreshViewStateWithUnknownTournamentName()
    {
        interact(() ->
        {
            model.selectTournament(new TournamentInfo(1, "", true, false));
            controller.refreshViewState();
        });

        Label title = lookup("#tournamentTitleLabel").queryAs(Label.class);

        assertEquals("Tournament: Unknown", title.getText());
    }

    @Test
    void testHandleExitWhenNotConnectedDoesNotCrash()
    {
        assertDoesNotThrow(() -> clickOn("#exitButton"));
    }
    
    @Test
    void testExitButtonUsesCommandAndRefreshesTournamentList() throws Exception
    {
        server = HttpServer.create(new InetSocketAddress(0), 0);

        server.createContext("/server/tournaments", exchange ->
        {
            String response = "[4:FINISHED]";
            byte[] bytes = response.getBytes();

            exchange.sendResponseHeaders(200, bytes.length);

            try (OutputStream os = exchange.getResponseBody())
            {
                os.write(bytes);
            }
        });

        server.start();

        interact(() -> model.connectToServer("localhost", String.valueOf(server.getAddress().getPort())));

        clickOn("#exitButton");

        assertEquals(1, model.getObservableTournaments().size());
        assertEquals(4, model.getObservableTournaments().get(0).getId());
    }
    
    @Test
    void testFetchMovesKeepsRoundLineWithTwoPlayersTogether() throws Exception
    {
        startMoveServer("[MATCH: A vs B, Round 1: A -> Defect, B -> Cooperate]");

        interact(() ->
        {
            model.connectToServer("localhost", String.valueOf(server.getAddress().getPort()));
            model.selectTournament(new TournamentInfo(1, "Live Test", true, false));
            model.fetchMovesForSelectedTournament();
            controller.refreshViewState();
        });

        ListView<?> moveList = lookup("#moveList").queryAs(ListView.class);

        assertEquals(2, moveList.getItems().size());
        assertTrue(moveList.getItems().contains("MATCH: A vs B"));
        assertTrue(moveList.getItems().contains("Round 1: A -> Defect, B -> Cooperate"));
    }
    
    @Test
    void testRefreshViewStateStartsAutoRefresh()
    {
        interact(() ->
        {
            controller.refreshViewState();
        });

        assertNotNull(controller.getRefreshTimeline());
    }
}