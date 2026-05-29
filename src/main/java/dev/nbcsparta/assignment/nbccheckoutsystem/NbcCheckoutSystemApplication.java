package dev.nbcsparta.assignment.nbccheckoutsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class NbcCheckoutSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(NbcCheckoutSystemApplication.class, args);
    }

}
