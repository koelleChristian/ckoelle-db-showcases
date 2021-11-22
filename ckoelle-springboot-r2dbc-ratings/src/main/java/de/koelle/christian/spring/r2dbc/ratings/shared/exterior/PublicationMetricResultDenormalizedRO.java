package de.koelle.christian.spring.r2dbc.ratings.shared.exterior;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import de.koelle.christian.spring.r2dbc.ratings.shared.types.CalculationType;
import de.koelle.christian.spring.r2dbc.ratings.shared.types.PublicationDomain;
import de.koelle.christian.spring.r2dbc.ratings.shared.types.Trend;

/**
 * The opposite of PublicationMetricResultDenormalizedRO, aka a hierarchical object structure.
 */
public record PublicationMetricResultDenormalizedRO(Integer idPublication, Integer idMetric, Integer idResult, PublicationDomain publicationDomain,
                                                    Integer publicationYear, String publicationBusinessVersion, LocalDateTime publicationTime,
                                                    String metricNumber, CalculationType metricCalculationType, String resultReference,
                                                    BigDecimal resultValue, Trend resultTrend) {

}
