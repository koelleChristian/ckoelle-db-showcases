package de.koelle.christian.spring.r2dbc.ratings.shared.exterior;

import de.koelle.christian.spring.r2dbc.ratings.shared.types.CalculationType;

public record CreateMetricRO(String metricNumber, CalculationType calculationType) {

}
