package SITS_sprint2;

import SITS_sprint2.Robot;

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