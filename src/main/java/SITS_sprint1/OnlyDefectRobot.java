package SITS_sprint1;

public class OnlyDefectRobot extends Robot 
{
	
	public OnlyDefectRobot(String name){
		super(name);
	}

	 @Override
	 public String makeMove()
	 {
		return "Defect";
	 }
	 
	 @Override
	 public void rememberOpponentMove(String move) {
		 
	 }
}
