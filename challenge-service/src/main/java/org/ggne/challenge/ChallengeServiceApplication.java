package org.ggne.challenge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ChallengeServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChallengeServiceApplication.class, args);
    }
}
