package de.koelle.christian.spring.r2dbc.ratings.shared.exterior;

import de.koelle.christian.spring.r2dbc.ratings.shared.types.PublicationDomain;


public record CreatePublicationRO(PublicationDomain domain, Integer year, String businessVersion) {

}
