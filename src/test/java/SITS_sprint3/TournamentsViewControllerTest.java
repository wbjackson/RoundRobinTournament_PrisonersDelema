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
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import SITS_sprint1.OnlyDefectRobot;
import SITS_sprint2.RobotClientController;

public class TournamentsViewControllerTest extends ApplicationTest
{
    private TournamentModel model;
    private TournamentsViewController controller;
    private HttpServer server;

    @Override
    public void start(Stage stage) throws Exception
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/tournaments-view.fxml"));
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
        assertNotNull(lookup("#ipField").queryAs(TextField.class));
        assertNotNull(lookup("#portField").queryAs(TextField.class));
        assertNotNull(lookup("#tournamentList").queryAs(ListView.class));
        assertNotNull(lookup("#connectButton").queryButton());
        assertNotNull(lookup("#refreshButton").queryButton());
        assertNotNull(lookup("Refresh").queryButton());
    }

    @Test
    void testConnectWithBlankIpDoesNotConnect()
    {
        clickOn("#ipField").write("");
        clickOn("#portField").write("8081");

        clickOn("#connectButton");

        assertFalse(model.isConnected());
        assertEquals(0, model.getObservableTournaments().size());
    }

    @Test
    void testConnectWithBlankPortDoesNotConnect()
    {
        clickOn("#ipField").write("localhost");
        clickOn("#portField").write("");

        clickOn("#connectButton");

        assertFalse(model.isConnected());
        assertEquals(0, model.getObservableTournaments().size());
    }

    @Test
    void testConnectLoadsTournamentsFromServer() throws Exception
    {
        startTournamentServer("[1:REG, 2:ACTIVE]");

        interact(() ->
        {
            model.connectToServer("localhost", String.valueOf(server.getAddress().getPort()));
            model.fetchTournaments();
            controller.refreshViewState();
        });

        assertTrue(model.isConnected());
        assertEquals(2, model.getObservableTournaments().size());
        assertEquals(1, model.getObservableTournaments().get(0).getId());
        assertEquals(2, model.getObservableTournaments().get(1).getId());
    }

    @Test
    void testRefreshWithoutConnectionDoesNotCrash()
    {
        assertDoesNotThrow(() -> clickOn("#refreshButton"));
        assertEquals(0, model.getObservableTournaments().size());
    }

    @Test
    void testRefreshUpdatesTournamentList() throws Exception
    {
        startTournamentServer("[3:ACTIVE]");

        interact(() -> model.connectToServer("localhost", String.valueOf(server.getAddress().getPort())));

        clickOn("#refreshButton");

        assertEquals(1, model.getObservableTournaments().size());
        assertEquals(3, model.getObservableTournaments().get(0).getId());
        assertTrue(model.getObservableTournaments().get(0).isActive());
    }

    @Test
    void testRefreshViewStateRestoresIpPortAndList()
    {
        interact(() ->
        {
            model.connectToServer("localhost", "8081");
            model.getObservableTournaments().add(new TournamentInfo(7, "Manual", true, false));
            controller.refreshViewState();
        });

        TextField ipField = lookup("#ipField").queryAs(TextField.class);
        TextField portField = lookup("#portField").queryAs(TextField.class);
        ListView<?> list = lookup("#tournamentList").queryAs(ListView.class);

        assertEquals("localhost", ipField.getText());
        assertEquals("8081", portField.getText());
        assertEquals(1, list.getItems().size());
    }

    @Test
    void testTournamentListDisplaysTournamentInfo()
    {
        interact(() ->
        {
            model.getObservableTournaments().add(new TournamentInfo(4, "DisplayTest", true, false));
            controller.refreshViewState();
        });

        assertNotNull(lookup("Tournament 4 - DisplayTest [Active]").query());
        assertNotNull(lookup("View Tournament").queryButton());
    }

    @Test
    void testViewTournamentButtonSelectsTournament()
    {
        interact(() ->
        {
            model.connectToServer("localhost", "9999");
            model.getObservableTournaments().add(new TournamentInfo(9, "Selectable", true, false));
            controller.refreshViewState();
        });

        clickOn("View Tournament");

        assertNotNull(model.getSelectedTournament());
        assertEquals(9, model.getSelectedTournament().getId());
        assertEquals(0, model.getObservableMoves().size());
    }

    private void startTournamentServer(String tournamentResponse) throws Exception
    {
        server = HttpServer.create(new InetSocketAddress(0), 0);

        server.createContext("/server/tournaments", exchange ->
        {
            byte[] bytes = tournamentResponse.getBytes();

            exchange.sendResponseHeaders(200, bytes.length);

            try (OutputStream os = exchange.getResponseBody())
            {
                os.write(bytes);
            }
        });

        server.createContext("/server/moves/9", exchange ->
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
    }
 
    @Test
    void testConnectButtonUsesCommandAndLoadsTournaments() throws Exception
    {
        startTournamentServer("[8:REG]");

        interact(() ->
        {
            model.connectToServer("localhost", String.valueOf(server.getAddress().getPort()));
            model.fetchTournaments();
            controller.refreshViewState();
        });

        assertTrue(model.isConnected());
        assertEquals(1, model.getObservableTournaments().size());
        assertEquals(8, model.getObservableTournaments().get(0).getId());
    }
 
    @Test
    void testRefreshButtonUsesCommandAndUpdatesTournaments() throws Exception
    {
        startTournamentServer("[10:ACTIVE]");

        interact(() -> model.connectToServer("localhost", String.valueOf(server.getAddress().getPort())));

        clickOn("#refreshButton");

        assertEquals(1, model.getObservableTournaments().size());
        assertEquals(10, model.getObservableTournaments().get(0).getId());
        assertTrue(model.getObservableTournaments().get(0).isActive());
    }
    
    @Test
    void testViewTournamentButtonUsesCommandAndSelectsTournament() throws Exception
    {
        startTournamentServer("[9:ACTIVE]");

        interact(() ->
        {
            model.connectToServer("localhost", String.valueOf(server.getAddress().getPort()));
            model.getObservableTournaments().add(new TournamentInfo(9, "Selectable", true, false));
            controller.refreshViewState();
        });

        clickOn("View Tournament");

        assertNotNull(model.getSelectedTournament());
        assertEquals(9, model.getSelectedTournament().getId());
    }
    
    @Test
    void testRobotClientControllerDefaultEndpointsStillWork()
    {
        RobotClientController controller = new RobotClientController();

        assertEquals("Defect", controller.makeDefaultDecision());

        String current = controller.getDefaultCurrentRobot();
        assertTrue(current.contains("RemoteHostedRobot"));
    }

    @Test
    void testRobotClientControllerDefaultSetRobotType()
    {
        RobotClientController controller = new RobotClientController();

        String result = controller.setDefaultRobotType("copycat");

        assertTrue(result.contains("RemoteHostedRobot"));
        assertEquals("Cooperate", controller.makeDefaultDecision());
    }

    @Test
    void testRobotClientControllerDefaultHumanMove()
    {
        RobotClientController controller = new RobotClientController();

        controller.setDefaultRobotType("human");

        String setResult = controller.setDefaultHumanMove("Cooperate");
        String currentResult = controller.getDefaultHumanMove();

        assertTrue(setResult.contains("Cooperate"));
        assertTrue(currentResult.contains("Cooperate"));
    }

    @Test
    void testRobotClientControllerSetHostedRobotIgnoresInvalidInput()
    {
        RobotClientController controller = new RobotClientController();

        controller.setHostedRobot("", new OnlyDefectRobot("Bad"));
        controller.setHostedRobot("Remote1", null);

        assertNull(controller.getHostedRobot(""));
        assertNull(controller.getHostedRobot("Remote1"));
    }

    @Test
    void testRobotClientControllerGetHostedRobotsReturnsMap()
    {
        RobotClientController controller = new RobotClientController();

        controller.setRobotType("Remote1", "defector");
        controller.setRobotType("Remote2", "copycat");

        assertTrue(controller.getHostedRobots().containsKey("Remote1"));
        assertTrue(controller.getHostedRobots().containsKey("Remote2"));
    }

    @Test
    void testRobotClientControllerHumanOpponentFailsForNonHuman()
    {
        RobotClientController controller = new RobotClientController();

        controller.setRobotType("Remote1", "defector");

        String result = controller.getHumanOpponentMove("Remote1");

        assertTrue(result.contains("ERROR"));
    }

    @Test
    void testRobotClientControllerHumanOpponentFailsForInvalidName()
    {
        RobotClientController controller = new RobotClientController();

        String result = controller.getHumanOpponentMove("");

        assertTrue(result.contains("ERROR"));
    }

    @Test
    void testRobotClientControllerSetHumanMoveInvalidName()
    {
        RobotClientController controller = new RobotClientController();

        String result = controller.setHumanMove("", "Cooperate");

        assertTrue(result.contains("ERROR"));
    }

    @Test
    void testRobotClientControllerGetHumanMoveInvalidName()
    {
        RobotClientController controller = new RobotClientController();

        String result = controller.getHumanMove("");

        assertTrue(result.contains("ERROR"));
    }

    @Test
    void testRobotClientControllerGetCurrentRobotInvalidName()
    {
        RobotClientController controller = new RobotClientController();

        String result = controller.getCurrentRobot("");

        assertTrue(result.contains("ERROR"));
    }
}