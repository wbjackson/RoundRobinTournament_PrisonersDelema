package SITS_sprint2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ClientApplication
{
	public static void main(String[] args)
	{
	    start(args);
	}

	static void start(String[] args)
	{
	    SpringApplication.run(ClientApplication.class, args);
	}
}