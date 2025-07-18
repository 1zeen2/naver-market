package dev.seo.navermarket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import dev.seo.navermarket.config.FileStorageProperties;
import dev.seo.navermarket.config.JwtProperties;

@SpringBootApplication
@EnableJpaAuditing
@EnableConfigurationProperties({JwtProperties.class, FileStorageProperties.class})
public class NaverMarketApplication {

	public static void main(String[] args) {
		SpringApplication.run(NaverMarketApplication.class, args);
	}

}
