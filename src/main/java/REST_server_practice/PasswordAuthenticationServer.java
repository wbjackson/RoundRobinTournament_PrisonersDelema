package REST_server_practice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController 
@RequestMapping("/")

public class PasswordAuthenticationServer
{
	Map<String, String> passwords = new HashMap<>();
	List<String> authenticated = new ArrayList<>();
	List<String> requested = new ArrayList<>();
	
	@ResponseStatus(HttpStatus.OK)
	@GetMapping("/")
	public String homePage() {
		return "<html><body>" +
				"<h1>Password Server<h1>" +
				"<ol>" +
				"<li><a href='request'>request/</a> Users that have requested a password.<li>" +
				"<li><a href='auth'>auth/</a> Users that have authenticated their password</li>" +
				"<li><a href='request/bckrj'>request/:username</a> A request for a password for the username provided." +
				"<li><a href='auth'>auth/bckrj/2432'>auth/:username/:password</a> Attemps to authenticate the password for the username provide.<li>" +
				"</ol>" +
				"</body></html";
	}
	@ResponseStatus(HttpStatus.OK)
	@GetMapping("/request")
	public String requestUsers() {
		return "Requesters: " + requested;
	}	
	
	@ResponseStatus(HttpStatus.OK)
	@GetMapping("/auth")
	public String authenticatedUsers() {
		return "Authenticated: " + authenticated;
	}
	
	@ResponseStatus(HttpStatus.OK)
	@GetMapping("/request/{username}")
	public String requestPassword(@PathVariable String username) {
		int password = Math.abs(username.hashCode()) %10000;
		passwords.put(username,String.valueOf(password));
		
		if(!requested.contains(username)) {
			requested.add(username);
		}
		
		return String.valueOf(password);
	}
	
	@ResponseStatus(HttpStatus.OK)
	@GetMapping("/auth/{username}/{password}")
	public String authenticatePassword(@PathVariable String username, @PathVariable String password) {
		
		if(!password.contains(username)) {
			return "Username not found";
		}
		
		if (passwords.get(username).equals(password)) {
			if(!authenticated.contains(username)) {
				authenticated.add(username);
			}
			return "Authentication Successfull";
		}
		else {
			return "Authentication Failed";
		}
	}
}
