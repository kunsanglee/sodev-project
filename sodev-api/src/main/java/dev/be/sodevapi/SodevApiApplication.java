package dev.be.sodevapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaAuditing
@EntityScan(basePackages = "dev.be.sodevcommon.domain.entity")
@EnableJpaRepositories(basePackages = "dev.be.sodevcommon.domain.repository")
@SpringBootApplication
public class SodevApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SodevApiApplication.class, args);
    }

}
