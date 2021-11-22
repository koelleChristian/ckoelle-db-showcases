package de.koelle.christian.spring.r2dbc.ratings.shared.exterior;

import java.util.List;

public record CreatePublicationWithMetricResultsRO(CreatePublicationRO publication, List<CreateMetricWithResultsRO> metricWithResults) {

}
