package de.koelle.christian.spring.r2dbc.ratings.dao;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface RatingMetricRepository extends ReactiveCrudRepository<RatingMetric, Long> {

	@Query("""
		select id, fk_publication_id,  metric_number,  calculation_type
		from rating_metric m
		where m.fk_publication_id = :fkPublicationId
		""")
	Flux<RatingMetric> findBy(final Integer fkPublicationId);

}
