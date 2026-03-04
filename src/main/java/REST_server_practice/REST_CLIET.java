package REST_server_practice;

import org.springframework.web.client.RestClient;

public class REST_CLIET
{
	
	public static void main(String[]args)
	{
		RestClient restClient = RestClient.create();

		
		String baseuri = "http://cs-hydra.centre.edu:9000";
		
		
		String request_password = restClient.get()
			.uri(baseuri +"/request/BeckerJackson")
			.retrieve()
			.body(String.class); 
			 
		
		String authenticate_password = restClient.get()
				.uri(baseuri + "/auth/BeckerJackson/"+request_password)
				.retrieve()
				.body(String.class);
		
		System.out.print(request_password);
		System.out.print(authenticate_password);
		
		
		
		
	}
	
	
	
}
