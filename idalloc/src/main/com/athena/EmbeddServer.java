package com.athena;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@SpringBootApplication(exclude={DataSourceAutoConfiguration.class,HibernateJpaAutoConfiguration.class})
public class EmbeddServer {


	public void start() {
		// 选主

		// 如果当前server 为active, 则启动当前应用
	}

	public static void main(String[] args) {
		SpringApplication.run(EmbeddServer.class, args);
	}
}
