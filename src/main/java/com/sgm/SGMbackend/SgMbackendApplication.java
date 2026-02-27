package com.sgm.SGMbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing // Active @CreatedDate et @LastModifiedDate
@EnableScheduling // Active les @Scheduled (pour les alertes automatiques)
public class SgMbackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SgMbackendApplication.class, args);
	}
}

