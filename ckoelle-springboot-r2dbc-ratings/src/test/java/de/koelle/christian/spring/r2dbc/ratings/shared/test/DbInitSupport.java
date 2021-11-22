package de.koelle.christian.spring.r2dbc.ratings.shared.test;

import java.util.stream.Stream;
import org.springframework.r2dbc.core.DatabaseClient;
import de.koelle.christian.spring.r2dbc.ratings.shared.consts.SqlStatementIds;
import reactor.test.StepVerifier;


public final class DbInitSupport {

	public static void doInitDb(final DatabaseClient database) {
		Stream.of(
			SqlStatementIds.DROP_RATING_RESULT,
			SqlStatementIds.DROP_RATING_METRIC,
			SqlStatementIds.DROP_RATING_PUBLICATION,
			SqlStatementIds.CREATE_RATING_PUBLICATION,
			SqlStatementIds.CREATE_RATING_METRIC,
			SqlStatementIds.CREATE_RATING_RESULT
		)
			.forEach(it -> database.sql(it)
				.fetch()
				.rowsUpdated()
				.as(StepVerifier::create)
				.expectNextCount(1)
				.verifyComplete());
	}

	private DbInitSupport() {
		// intentionally blank
	}

}
