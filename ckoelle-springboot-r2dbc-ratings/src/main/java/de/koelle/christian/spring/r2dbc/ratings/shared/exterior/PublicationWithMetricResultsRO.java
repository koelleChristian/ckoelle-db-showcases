package de.koelle.christian.spring.r2dbc.ratings.shared.exterior;

import java.util.List;

public record PublicationWithMetricResultsRO(PublicationRO publication, List<MetricWithResultsRO> metricWithResults) {

}
