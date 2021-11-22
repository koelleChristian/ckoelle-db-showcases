package de.koelle.christian.spring.r2dbc.ratings.dao;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import de.koelle.christian.spring.r2dbc.ratings.shared.types.PublicationDomain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RatingPublicationRepository extends ReactiveCrudRepository<RatingPublication, Integer> {

	@Query("""
		select id, domain, year, business_version, publication_time 
		from rating_publication c 
		where c.domain = :domain
		and c.year = :year
		and c.business_version = :businessVersion
		""")
	Mono<RatingPublication> findByMono(final PublicationDomain domain, final Integer year, final String businessVersion);

	@Query("""
		select id, domain, year, business_version, publication_time 
		from rating_publication c 
		where c.year = :year
		""")
	Flux<RatingPublication> findBy( final Integer year);
}
