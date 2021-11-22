package de.koelle.christian.spring.r2dbc.ratings.shared.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import de.koelle.christian.spring.r2dbc.ratings.IntTestConfiguration;
import reactor.core.publisher.Hooks;

@SpringBootTest(classes = IntTestConfiguration.class)
class ManualDbInitializerTest {

	@Autowired
	DatabaseClient database;

	//@Disabled ("To be executed manuall to initialize the db schema.") // TODO
	@Test
	void initializeDatabase() {
		Hooks.onOperatorDebug();
		DbInitSupport.doInitDb(database);
	}


}
