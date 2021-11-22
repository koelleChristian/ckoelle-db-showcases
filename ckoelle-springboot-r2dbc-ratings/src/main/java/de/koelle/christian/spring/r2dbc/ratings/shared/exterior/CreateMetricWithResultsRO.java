package de.koelle.christian.spring.r2dbc.ratings.shared.exterior;

import java.util.List;

public record CreateMetricWithResultsRO(CreateMetricRO metric, List<CreateResultRO> results) {

}
