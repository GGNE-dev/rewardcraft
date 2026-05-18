package org.ggne.challenge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class ChallengeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChallengeServiceApplication.class, args);
    }
}
