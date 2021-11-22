package de.koelle.christian.spring.r2dbc.ratings.dao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import de.koelle.christian.spring.r2dbc.ratings.IntTestConfiguration;
import de.koelle.christian.spring.r2dbc.ratings.shared.test.DbInitSupport;
import de.koelle.christian.spring.r2dbc.ratings.shared.types.CalculationType;
import de.koelle.christian.spring.r2dbc.ratings.shared.types.PublicationDomain;
import de.koelle.christian.spring.r2dbc.ratings.shared.types.Trend;
import reactor.core.publisher.Hooks;
import reactor.test.StepVerifier;
import reactor.util.function.Tuples;

@SpringBootTest(classes = IntTestConfiguration.class)
class RepositoryDbTest {

	@Autowired
	RatingPublicationRepository ratingPublicationRepository;
	@Autowired
	RatingMetricRepository ratingMetricRepository;
	@Autowired
	RatingResultRepository ratingResultRepository;
	@Autowired
	DatabaseClient database;

	private final Predicate<Object> loggingTruePredicate = i -> {
		System.out.println(i.toString());
		return true;
	};

	@BeforeEach
	void setUp() {
		Hooks.onOperatorDebug();
		DbInitSupport.doInitDb(database);
	}

	@Test
	void executesFindAll() {

		var pub1 = new RatingPublication(null, PublicationDomain.MEDIA, 2020, "v01", LocalDateTime.of(2020, Month.JUNE, 13, 14, 15));
		var pub2 = new RatingPublication(null, PublicationDomain.LOGISTICS, 2020, "v02", LocalDateTime.of(2020, Month.JUNE, 14, 15, 16));
		var pub3 = new RatingPublication(null, PublicationDomain.TELCO, 2021, "v01", LocalDateTime.of(2020, Month.JUNE, 15, 16, 17));

		insert(pub1, pub2, pub3);

		ratingPublicationRepository.findAll()
			.as(StepVerifier::create)
			.expectNext(pub1)
			.expectNext(pub2)
			.expectNext(pub3)
			.verifyComplete();
	}

	@Test
	void executesAnnotatedQuery() {
		var pub1 = new RatingPublication(null, PublicationDomain.MEDIA, 2020, "v01", LocalDateTime.of(2020, Month.JUNE, 13, 14, 15));
		var pub2 = new RatingPublication(null, PublicationDomain.MEDIA, 2020, "v02", LocalDateTime.of(2020, Month.JUNE, 14, 15, 16));
		var pub3 = new RatingPublication(null, PublicationDomain.MEDIA, 2021, "v01", LocalDateTime.of(2020, Month.JUNE, 15, 16, 17));

		insert(pub1, pub2, pub3);

		ratingPublicationRepository.findBy(2020)
			.as(StepVerifier::create)
			.expectNext(pub1)
			.expectNext(pub2)
			.verifyComplete();
	}

	@Test
	void executesRepoChainWithOneToManyData() {

		final LocalDateTime execTime = LocalDateTime.of(2020, Month.JUNE, 13, 14, 15);
		final RatingPublication ratingPublication = new RatingPublication(null, PublicationDomain.MEDIA, 2020, "v01", execTime);
		ratingPublicationRepository
			.save(ratingPublication)
			.flatMap(i -> ratingMetricRepository
				.save(new RatingMetric(null, i.id(), "30001", CalculationType.C1))
				.map(j -> Tuples.of(i, j)))
			.flatMapMany(i -> ratingResultRepository
				.saveAll(List.of(
					new RatingResult(null, i.getT1().id(), i.getT2().id(), "abc1", BigDecimal.valueOf(10.10), Trend.UPWARDS),
					new RatingResult(null, i.getT1().id(), i.getT2().id(), "abc2", BigDecimal.valueOf(20.20), Trend.UNCHANGED)
					)
				)
				.map(j -> Tuples.of(i.getT1(), i.getT2(), j)))
			.as(StepVerifier::create)
			.expectNextCount(2)
			.verifyComplete();


		ratingResultRepository.findBy(ratingPublication.domain(), ratingPublication.year(), ratingPublication.businessVersion())
			.as(StepVerifier::create)
			.expectNextMatches(loggingTruePredicate)
			.expectNextMatches(loggingTruePredicate)
			.verifyComplete();
	}

	private void insert(RatingPublication... values) {
		final List<RatingPublication> entities = Arrays.asList(values);
		final int expectedCount = entities.size();
		this.ratingPublicationRepository.saveAll(entities)
			.as(StepVerifier::create)
			.expectNextCount(expectedCount)
			.verifyComplete();
	}
}
