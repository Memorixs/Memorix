package com.memo.api.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;


@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
			.addSecurityItem(new SecurityRequirement().addList("Authorization"))
			.components(
				new Components()
					.addSecuritySchemes("Authorization", new SecurityScheme()
						.name("Authorization")
						.type(SecurityScheme.Type.APIKEY)
						.scheme("bearer")
						.in(SecurityScheme.In.HEADER)
						.bearerFormat("JWT")
					)
					.addSecuritySchemes("refresh-token", new SecurityScheme()
						.name("refresh-token")
						.type(SecurityScheme.Type.APIKEY)
						.in(SecurityScheme.In.COOKIE)
						.bearerFormat("JWT")
					)
			)
			.info(new Info()
				.title("Memorix API")
				.description("Memorix API 목록")
				.version("v1.0.0"))
			.servers(List.of(
				new Server()
					.url("http://localhost:8080")
					.description("개발용 서버")
			));
	}

}
