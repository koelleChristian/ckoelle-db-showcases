package de.koelle.christian.spring.r2dbc.ratings.dao;


import java.math.BigDecimal;
import java.util.Objects;
import org.springframework.data.annotation.Id;
import de.koelle.christian.spring.r2dbc.ratings.shared.types.Trend;

public record RatingResult(@Id Integer id, Integer fkPublicationId, Integer fkMetricId, String resultReference, BigDecimal resultValue, Trend trend) {

	/** Property name constant for {@code resultReference}. */
	public static final String PROPERTYNAME_RESULT_REFERENCE = "resultReference";
	/** Property name constant for {@code id}. */
	public static final String PROPERTYNAME_ID = "id";
	/** Property name constant for {@code resultValue}. */
	public static final String PROPERTYNAME_RESULT_VALUE = "resultValue";
	/** Property name constant for {@code fkPublicationId}. */
	public static final String PROPERTYNAME_FK_PUBLICATION_ID = "fkPublicationId";
	/** Property name constant for {@code trend}. */
	public static final String PROPERTYNAME_TREND = "trend";
	/** Property name constant for {@code fkMetricId}. */
	public static final String PROPERTYNAME_FK_METRIC_ID = "fkMetricId";

	boolean hasId() {
		return id != null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof RatingResult other)) {
			return false;
		}
		return Objects.equals(fkPublicationId, other.fkPublicationId)
			&& Objects.equals(fkMetricId, other.fkMetricId)
			&& Objects.equals(resultReference, other.resultReference)
			&& Objects.equals(resultValue, other.resultValue)
			&& Objects.equals(trend, other.trend);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fkPublicationId, fkMetricId, resultReference, resultValue, trend);
	}
}
