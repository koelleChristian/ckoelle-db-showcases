package de.koelle.christian.spring.r2dbc.ratings;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import de.koelle.christian.spring.r2dbc.ratings.rest.RatingRequestHandler;

@SpringBootApplication
@EnableTransactionManagement
public class RatingsR2dbcWebfluxApp {

	public static void main(String[] args) {
		SpringApplication.run(RatingsR2dbcWebfluxApp.class, args);
	}

	@Bean
	public RouterFunction<ServerResponse> route(RatingRequestHandler handler) {
		return RouterFunctions
			.route(RequestPredicates.GET("/publication")
				.and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), handler::findAllPublications)
			.andRoute(RequestPredicates.GET("/metric")
				.and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), handler::findAllMetrics)
			.andRoute(RequestPredicates.GET("/result")
				.and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), handler::findAllResults)
			.andRoute(RequestPredicates.POST("/publication")
				.and(RequestPredicates.contentType(MediaType.APPLICATION_JSON)), handler::createPublication)
			.andRoute(RequestPredicates.POST("/result/full/flat/tuplequery")
				.and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), handler::findAllRatingsFlatByTupleQuery)
			.andRoute(RequestPredicates.POST("/result/full/flat/stream")
				.and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), handler::findAllRatingsFlatByStream)
			.andRoute(RequestPredicates.POST("/result/full/hierarchical")
				.and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), handler::findAllRatingsHierarchicalByStream)
			.andRoute(RequestPredicates.POST("/result/full")
				.and(RequestPredicates.contentType(MediaType.APPLICATION_JSON)), handler::createOrReplaceFullPublication)
			/**/;
	}

}
