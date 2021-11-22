package de.koelle.christian.spring.r2dbc.ratings.shared.exterior;

import java.math.BigDecimal;
import de.koelle.christian.spring.r2dbc.ratings.shared.types.Trend;

public record CreateResultRO(String resultReference, BigDecimal resultValue, Trend trend) {

}
