package de.koelle.christian.spring.r2dbc.ratings.dao;

import java.time.LocalDateTime;
import java.util.Objects;
import org.springframework.data.annotation.Id;
import de.koelle.christian.spring.r2dbc.ratings.shared.types.PublicationDomain;
public record RatingPublication(@Id Integer id, PublicationDomain domain, Integer year, String businessVersion, LocalDateTime publicationTime) {

	/** Property name constant for {@code year}. */
	public static final String PROPERTYNAME_YEAR = "year";
	/** Property name constant for {@code id}. */
	public static final String PROPERTYNAME_ID = "id";
	/** Property name constant for {@code businessVersion}. */
	public static final String PROPERTYNAME_BUSINESS_VERSION = "businessVersion";
	/** Property name constant for {@code publicationTime}. */
	public static final String PROPERTYNAME_PUBLICATION_TIME = "publicationTime";
	/** Property name constant for {@code domain}. */
	public static final String PROPERTYNAME_DOMAIN = "domain";
//
	public boolean hasId() {
		return id != null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof RatingPublication other)) {
			return false;
		}
		return Objects.equals(domain, other.domain)
			&& Objects.equals(year, other.year)
			&& Objects.equals(businessVersion, other.businessVersion)
			&& Objects.equals(publicationTime, other.publicationTime);
	}

	@Override
	public int hashCode() {
		return Objects.hash(domain, year, businessVersion, publicationTime);
	}
}
