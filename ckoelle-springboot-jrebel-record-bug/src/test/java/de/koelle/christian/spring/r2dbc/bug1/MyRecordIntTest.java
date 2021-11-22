package de.koelle.christian.spring.r2dbc.bug1;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Hooks;
import reactor.test.StepVerifier;

@SpringBootTest(classes = IntTestConfiguration.class)
class MyRecordIntTest {

	public static final String SQL_DROP_TABLE =
		"DROP TABLE IF EXISTS my_record;";

	public static final String SQL_CREATE_TABLE =
		"""
			CREATE TABLE my_record (
			    id INT(20) AUTO_INCREMENT,
				whatever VARCHAR(100) NOT NULL						
			);
			""";

	@Autowired
	MyRecordRepository repo;

	@Autowired
	DatabaseClient database;

	@BeforeEach
	void setUp() {

		Hooks.onOperatorDebug();
		Stream.of(
			SQL_DROP_TABLE,
			SQL_CREATE_TABLE
		).forEach(it -> database.sql(it)
			.fetch()
			.rowsUpdated()
			.as(StepVerifier::create)
			.expectNextCount(1)
			.verifyComplete());
	}

	@Test
	void saveAndFindAll() {

		var r1 = new MyRecord(null, "ABBA");

		this.repo.save(r1)
			.as(StepVerifier::create)
			.expectNextCount(1)
			.verifyComplete();

		repo.findAll()
			.as(StepVerifier::create)
			.expectNext(r1)
			.verifyComplete();
	}
}
