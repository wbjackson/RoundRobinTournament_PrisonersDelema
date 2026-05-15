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