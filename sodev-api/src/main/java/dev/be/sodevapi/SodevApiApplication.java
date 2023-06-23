package dev.be.sodevapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(
        scanBasePackages = { "dev.be.sodevapi", "dev.be.sodevcommon" }
)
@EntityScan(basePackages = "dev.be.sodevcommon.domain")
@EnableJpaRepositories(basePackages = "dev.be.sodevcommon.repository")
public class SodevApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SodevApiApplication.class, args);
    }

}
