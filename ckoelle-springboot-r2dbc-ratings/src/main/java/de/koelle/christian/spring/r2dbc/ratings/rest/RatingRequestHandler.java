package de.koelle.christian.spring.r2dbc.ratings.rest;

import java.time.LocalDateTime;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import de.koelle.christian.spring.r2dbc.ratings.business.BusinessService;
import de.koelle.christian.spring.r2dbc.ratings.mapping.OutsideInsideMapper;
import de.koelle.christian.spring.r2dbc.ratings.shared.exterior.CreatePublicationRO;
import de.koelle.christian.spring.r2dbc.ratings.shared.exterior.CreatePublicationWithMetricResultsRO;
import de.koelle.christian.spring.r2dbc.ratings.shared.exterior.MetricRO;
import de.koelle.christian.spring.r2dbc.ratings.shared.exterior.PublicationMetricResultDenormalizedRO;
import de.koelle.christian.spring.r2dbc.ratings.shared.exterior.PublicationRO;
import de.koelle.christian.spring.r2dbc.ratings.shared.exterior.PublicationWithMetricResultsRO;
import de.koelle.christian.spring.r2dbc.ratings.shared.exterior.RatingCriteriaRO;
import de.koelle.christian.spring.r2dbc.ratings.shared.exterior.ResultRO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@SuppressWarnings("java:S1172") // S1172: Unused method parameters should be removed -> Required for RequestMapping
public class RatingRequestHandler {

	private final BusinessService businessService;
	private final OutsideInsideMapper outsideWorldMapper = new OutsideInsideMapper();

	public RatingRequestHandler(BusinessService businessService) {
		this.businessService = businessService;
	}

	public Mono<ServerResponse> findAllPublications(ServerRequest request) {
		Flux<PublicationRO> values = businessService
			.findAllPublications() // Flux<RatingPublication>
			.map(i -> outsideWorldMapper.map2Outer(i)); // Map to external
		return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(values, PublicationRO.class);
	}

	public Mono<ServerResponse> findAllMetrics(ServerRequest request) {
		Flux<MetricRO> values = businessService
			.findAllMetrics() //Flux<RatingMetric>
			.map(i -> outsideWorldMapper.map2Outer(i)); // Map to external
		return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(values, MetricRO.class);
	}

	public Mono<ServerResponse> findAllResults(ServerRequest request) {
		Flux<ResultRO> values = businessService
			.findAllResults() // Flux<RatingResult>
			.map(i -> outsideWorldMapper.map2Outer(i)); // Map to external
		return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(values, ResultRO.class);
	}

	public Mono<ServerResponse> createPublication(ServerRequest request) {
		return request
			.bodyToMono(CreatePublicationRO.class)
			.map(i -> outsideWorldMapper.map2Inner(i, LocalDateTime.now()))
			.flatMap(i -> businessService.createPublication(i))
			.flatMap(i -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(i)))
			.switchIfEmpty(ServerResponse.notFound().build())
			.doOnError(i-> ServerResponse.badRequest());
	}

	public Mono<ServerResponse> createOrReplaceFullPublication(ServerRequest request) {
		return request
			.bodyToMono(CreatePublicationWithMetricResultsRO.class)
			.map(i -> outsideWorldMapper.map2ReferenceMap(i))
			.flatMap(i -> businessService.createOrReplaceFullPublication(i))
			.flatMap(i -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(i)))
			.switchIfEmpty(ServerResponse.notFound().build());
	}

	public Mono<ServerResponse> findAllRatingsFlatByTupleQuery(final ServerRequest request) {
		final Flux<PublicationMetricResultDenormalizedRO> values = request
			.bodyToMono(RatingCriteriaRO.class)
			.flatMapMany(i -> businessService.findAllRatingsFlatByTupleQuery(
				i.domain(), i.year(), i.businessVersion())
			);
		return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(values, PublicationMetricResultDenormalizedRO.class);
	}

	public Mono<ServerResponse> findAllRatingsFlatByStream(final ServerRequest request) {
		final Flux<PublicationMetricResultDenormalizedRO> values = request
			.bodyToMono(RatingCriteriaRO.class)
			.flatMapMany(i -> businessService.findAllRatingsFlatByStream(
				i.domain(), i.year(), i.businessVersion())
			);
		return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(values, PublicationMetricResultDenormalizedRO.class);
	}

	public Mono<ServerResponse> findAllRatingsHierarchicalByStream(final ServerRequest request) {
		final Mono<PublicationWithMetricResultsRO> values = request
			.bodyToMono(RatingCriteriaRO.class)
			.flatMap(i -> businessService.findAllRatingsHierarchicalByStream(i.domain(), i.year(), i.businessVersion()));
		return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(values, PublicationWithMetricResultsRO.class);
	}
}