package phong.demo.Springpro;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class RandomNumberService {
    private final Random random = new Random();


    public int generateRandomNumber(int bound) {

        return random.nextInt(bound);

    }



}
