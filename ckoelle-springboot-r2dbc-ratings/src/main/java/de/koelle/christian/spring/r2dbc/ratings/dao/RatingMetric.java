package de.koelle.christian.spring.r2dbc.ratings.dao;

import java.util.Objects;
import org.springframework.data.annotation.Id;
import de.koelle.christian.spring.r2dbc.ratings.shared.types.CalculationType;


public record RatingMetric(@Id Integer id, Integer fkPublicationId, String metricNumber, CalculationType calculationType) {

	/** Property name constant for {@code metricNumber}. */
	public static final String PROPERTYNAME_METRIC_NUMBER = "metricNumber";
	/** Property name constant for {@code id}. */
	public static final String PROPERTYNAME_ID = "id";
	/** Property name constant for {@code calculationType}. */
	public static final String PROPERTYNAME_CALCULATION_TYPE = "calculationType";
	/** Property name constant for {@code fkPublicationId}. */
	public static final String PROPERTYNAME_FK_PUBLICATION_ID = "fkPublicationId";

	boolean hasId() {
		return id != null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof RatingMetric other)) {
			return false;
		}
		return Objects.equals(fkPublicationId, other.fkPublicationId)
			&& Objects.equals(metricNumber, other.metricNumber)
			&& Objects.equals(calculationType, other.calculationType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fkPublicationId, metricNumber, calculationType);
	}
}
