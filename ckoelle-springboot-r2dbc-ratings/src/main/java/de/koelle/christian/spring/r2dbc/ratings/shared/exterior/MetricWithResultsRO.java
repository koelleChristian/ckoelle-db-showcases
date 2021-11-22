package de.koelle.christian.spring.r2dbc.ratings.shared.exterior;

import java.util.List;

public record MetricWithResultsRO(MetricRO metric, List<ResultRO> results) {

}
