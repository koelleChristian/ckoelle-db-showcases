package de.koelle.christian.spring.r2dbc.ratings.dao;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import de.koelle.christian.spring.r2dbc.ratings.shared.exterior.PublicationMetricResultDenormalizedRO;
import de.koelle.christian.spring.r2dbc.ratings.shared.types.PublicationDomain;
import reactor.core.publisher.Flux;


public interface RatingResultRepository extends ReactiveCrudRepository<RatingResult, Long> {

    @Query("""
        select id, fk_publication_id , fk_metric_id, result_reference, result_value, trend
        from rating_result res
        where
        res.fk_publication_id = :fkPublicationId and
        res.fk_metric_id = :fkMetricId
        """)
    Flux<RatingResult> findBy(final Integer fkPublicationId, final Integer fkMetricId);

    @Query("""
        select publ.id                 as idPublication,
               metric.id               as idMetric,
               res.id                  as idResult,
               publ.domain             as publicationDomain,
               publ.year               as publicationYear,
               publ.business_version   as publicationBusinessVersion,
               publ.publication_time   as publicationTime,
               metric.metric_number    as metricNumber,
               metric.calculation_type as metricCalculationType,
               res.result_reference    as resultReference,
               res.result_value        as resultValue,
               res.trend               as resultTrend
        from rating_result res
                 join rating_publication publ on res.fk_publication_id = publ.id
                 join rating_metric metric on metric.fk_publication_id = publ.id and
                                              res.fk_metric_id = metric.id
                where
                    publ.domain = :domain and
                    publ.year = :year and
                    publ.business_version = :businessVersion
                """)
    Flux<PublicationMetricResultDenormalizedRO> findBy(final PublicationDomain domain, final Integer year, final String businessVersion);

}
