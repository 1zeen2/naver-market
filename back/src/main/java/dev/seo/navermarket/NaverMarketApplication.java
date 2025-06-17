package dev.seo.navermarket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class NaverMarketApplication {

	public static void main(String[] args) {
		SpringApplication.run(NaverMarketApplication.class, args);
	}

}
