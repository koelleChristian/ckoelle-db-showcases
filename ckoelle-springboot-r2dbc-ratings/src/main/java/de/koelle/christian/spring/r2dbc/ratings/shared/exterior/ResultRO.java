package de.koelle.christian.spring.r2dbc.ratings.shared.exterior;

import java.math.BigDecimal;
import org.springframework.data.annotation.Id;
import de.koelle.christian.spring.r2dbc.ratings.shared.types.Trend;

public record ResultRO(@Id Integer id, Integer publicationId, Integer metricId, String resultReference, BigDecimal resultValue, Trend trend) {

}
