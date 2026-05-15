package org.ggne.rc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class RewardcraftApplication {

    public static void main(String[] args) {
        SpringApplication.run(RewardcraftApplication.class, args);
    }
}