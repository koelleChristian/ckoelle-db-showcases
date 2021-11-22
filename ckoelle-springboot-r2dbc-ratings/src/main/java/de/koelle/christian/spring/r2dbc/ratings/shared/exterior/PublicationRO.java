package de.koelle.christian.spring.r2dbc.ratings.shared.exterior;

import java.time.LocalDateTime;
import de.koelle.christian.spring.r2dbc.ratings.shared.types.PublicationDomain;


public record PublicationRO(Integer id, PublicationDomain domain, Integer year, String businessVersion, LocalDateTime publicationTime) {

}
