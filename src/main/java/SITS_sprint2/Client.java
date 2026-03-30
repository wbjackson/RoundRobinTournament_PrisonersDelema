package SITS_sprint2;

import org.springframework.web.bind.annotation.*;

import java.util.Random;

@RestController
@RequestMapping("/")
public class Client {

    private Random rand = new Random();

    // Endpoint the server will call
    @GetMapping("/move")
    public String makeDecision() {


        if (rand.nextBoolean()) {
            return "Cooperate";
        } else {
            return "Defect";
        }
    }
}