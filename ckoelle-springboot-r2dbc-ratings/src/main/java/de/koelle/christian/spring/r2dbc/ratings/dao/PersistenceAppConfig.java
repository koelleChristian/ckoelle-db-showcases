package de.koelle.christian.spring.r2dbc.ratings.dao;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;

/**
 * This class is only needed to register the custom converter: without this registration the converter is not applied.
 */
@Configuration
public class PersistenceAppConfig extends AbstractR2dbcConfiguration {

	public ConnectionFactory connectionFactory() {
		// I have to write something here, but it does not matter what, the settings in application.properties are required with the current
		// configuration.
		return ConnectionFactories.get("cheesecake");
	}

	@Override
	protected List<Object> getCustomConverters() {
		return List.of(new PublicationMetricResultDenormalizedReadConverter());
	}
}