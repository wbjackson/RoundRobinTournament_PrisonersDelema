package HelloWorldJavaFX;
import javafx.application.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;

public class ClickMeProgram extends Application
{

	public static void main(String[] args)
	{
		launch(args);
	}
	
	Button btn;
	
	@Override public void start(Stage primaryStage)
	{
		//Create the button
		btn = new Button();
		btn.setText("Click me please!");
		btn.setOnAction(e -> buttonClick());
		
		//Add the button to a layout pane
		BorderPane pane = new BorderPane();
		pane.setCenter(btn);
		//Add the layout pane to a scene
		Scene scene = new Scene(pane, 300, 250);
		
		//finalize and show the stage 
		primaryStage.setScene(scene);
		primaryStage.setTitle("The Click Me App");
		primaryStage.show();
	}
	
	public void buttonClick()
	{
		if (btn.getText() == "Click me please!")
			{
				btn.setText("You clicked me!"); //picture on phone 3/30/26
			}
		else 
			{
				btn.setText("Click me please!");
			}
	}
	public ClickMeProgram()
	{
		// TODO Auto-generated constructor stub
	}

}
