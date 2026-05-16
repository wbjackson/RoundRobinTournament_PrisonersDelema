package SITS_sprint2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RobotClientApplication
{
	public static void main(String[] args)
	{
	    start(args);						// launcher for springboot
	    									//potential name change Tourn server app
	}

	static void start(String[] args)
	{
	    SpringApplication.run(RobotClientApplication.class, args);
	}
}


//http://localhost:8081/server/create
//http://localhost:8081/server/close/1
//http://localhost:8081/server/start/1
//http://localhost:8081/server/tournaments

//
//1. Start TournamentServer
//2. Start RobotClientApplication
//3. Start RemoteClientViewerApp
//
//Set remote robots:
//http://localhost:8081/robot/set/Remote1/human
//http://localhost:8081/human/set/Remote1/Cooperate
//http://localhost:8081/robot/set/Remote2/copycat
//
//Create tournament:
//http://localhost:8081/server/create
//
//Register clients:
//http://localhost:8081/server/register/Remote1/localhost/8081
//http://localhost:8081/server/register/Remote2/localhost/8081
//
//Join tournament:
//http://localhost:8081/server/join/Remote1/1
//http://localhost:8081/server/join/Remote2/1
//
//Close and start:
//http://localhost:8081/server/close/1
//http://localhost:8081/server/start/1
//
//View moves:
//http://localhost:8081/server/moves/1