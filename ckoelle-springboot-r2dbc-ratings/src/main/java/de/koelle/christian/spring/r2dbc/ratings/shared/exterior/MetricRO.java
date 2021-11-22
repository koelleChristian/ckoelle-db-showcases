package de.koelle.christian.spring.r2dbc.ratings.shared.exterior;

import de.koelle.christian.spring.r2dbc.ratings.shared.types.CalculationType;

public record MetricRO(Integer id, Integer publicationId, String metricNumber, CalculationType calculationType) {

}
