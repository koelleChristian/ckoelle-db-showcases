package de.koelle.christian.spring.r2dbc.ratings.business;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import de.koelle.christian.spring.r2dbc.ratings.dao.RatingMetric;
import de.koelle.christian.spring.r2dbc.ratings.dao.RatingMetricRepository;
import de.koelle.christian.spring.r2dbc.ratings.dao.RatingPublication;
import de.koelle.christian.spring.r2dbc.ratings.dao.RatingPublicationRepository;
import de.koelle.christian.spring.r2dbc.ratings.dao.RatingResult;
import de.koelle.christian.spring.r2dbc.ratings.dao.RatingResultRepository;
import de.koelle.christian.spring.r2dbc.ratings.mapping.OutsideInsideMapper;
import de.koelle.christian.spring.r2dbc.ratings.shared.exterior.CreateMetricRO;
import de.koelle.christian.spring.r2dbc.ratings.shared.exterior.CreatePublicationRO;
import de.koelle.christian.spring.r2dbc.ratings.shared.exterior.CreateResultRO;
import de.koelle.christian.spring.r2dbc.ratings.shared.exterior.MetricRO;
import de.koelle.christian.spring.r2dbc.ratings.shared.exterior.MetricWithResultsRO;
import de.koelle.christian.spring.r2dbc.ratings.shared.exterior.PublicationMetricResultDenormalizedRO;
import de.koelle.christian.spring.r2dbc.ratings.shared.exterior.PublicationRO;
import de.koelle.christian.spring.r2dbc.ratings.shared.exterior.PublicationWithMetricResultsRO;
import de.koelle.christian.spring.r2dbc.ratings.shared.exterior.ResultRO;
import de.koelle.christian.spring.r2dbc.ratings.shared.types.PublicationDomain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

@Component
public class BusinessService {

	private static final Logger LOG = LoggerFactory.getLogger(BusinessService.class);

	private final RatingPublicationRepository publicationRepo;
	private final RatingMetricRepository metricRepo;
	private final RatingResultRepository resultRepo;
	private final R2dbcEntityTemplate template;
	private final OutsideInsideMapper outsideWorldMapper = new OutsideInsideMapper();

	public BusinessService(final RatingPublicationRepository publicationRepo, final RatingMetricRepository metricRepo, final RatingResultRepository resultRepo, final R2dbcEntityTemplate template) {
		this.publicationRepo = publicationRepo;
		this.metricRepo = metricRepo;
		this.resultRepo = resultRepo;
		this.template = template;
	}

	public Flux<RatingPublication> findAllPublications() {
		return publicationRepo.findAll();
	}

	public Flux<RatingMetric> findAllMetrics() {
		return metricRepo.findAll();
	}

	public Flux<RatingResult> findAllResults() {
		return resultRepo.findAll();
	}

	public Flux<PublicationMetricResultDenormalizedRO> findAllRatingsFlatByTupleQuery(final PublicationDomain domain, final Integer year, final String businessVersion) {
		return resultRepo.findBy(domain, year, businessVersion);
	}

	public Flux<PublicationMetricResultDenormalizedRO> findAllRatingsFlatByStream(final PublicationDomain domain, final Integer year, final String businessVersion) {
		return findAllRatingsByStream(domain, year, businessVersion)
			.map(i -> outsideWorldMapper.map2Outer(i));
	}

	public Mono<PublicationWithMetricResultsRO> findAllRatingsHierarchicalByStream(final PublicationDomain domain, final Integer year, final String businessVersion) {
		return reduce2HierarchicalResult(findAllRatingsByStream(domain, year, businessVersion));
	}

	@Transactional
	public Mono<RatingPublication> createPublication(RatingPublication ratingPublication) {
		return createPublication(ratingPublication, i -> false);
	}

	/**
	 * This signature is only used to supply conditional exceptionbehaviour for testing.
	 */
	@Transactional
	public Mono<RatingPublication> createPublication(RatingPublication ratingPublication, Predicate<RatingPublication> shouldTriggerExceptionPostSavingPredicate) {
		return publicationRepo
			.findByMono(ratingPublication.domain(), ratingPublication.year(), ratingPublication.businessVersion())
			.handle((BiConsumer<RatingPublication, SynchronousSink<RatingPublication>>) (i, s) -> {
				if (i != null) {
					s.error(new IllegalArgumentException("Publication with same business identity already exists: " + i + "."));
				}
			})
			.switchIfEmpty(
				publicationRepo.save(ratingPublication)
					.map(i -> {
						if (shouldTriggerExceptionPostSavingPredicate.test(i)) {
							throw new IllegalStateException("Exception invoked by provided Test predicate: " + i + ".");
						} else {
							return i;
						}
					}));
	}

	@Transactional // TODO Not yet propagated to the outside
	public Mono<RatingPublication> updatePublication(final Integer publicationId, final String businessVersion) {
		return publicationRepo
			.findById(publicationId)
			.flatMap(i -> {
				RatingPublication updatedEntity = new RatingPublication(i.id(), i.domain(), i.year(), businessVersion, i.publicationTime());
				return this.publicationRepo.save(updatedEntity);
			})
			.switchIfEmpty(Mono.error(new IllegalArgumentException(String.format("Publication with the id=%s does not exist.", publicationId))));
	}

	@Transactional
	public Mono<PublicationWithMetricResultsRO> createOrReplaceFullPublication(final Map<CreatePublicationRO, Map<CreateMetricRO, List<CreateResultRO>>> param) {
		if (param.size() != 1) {
			throw new IllegalArgumentException("More than one publication"); // TODO think about exception handling
		}

		final Map.Entry<CreatePublicationRO, Map<CreateMetricRO, List<CreateResultRO>>> oneAnOnly = param.entrySet().iterator().next();
		final CreatePublicationRO incomingPublication = oneAnOnly.getKey();
		final Map<CreateMetricRO, List<CreateResultRO>> incomingPublicationEntries = oneAnOnly.getValue();
		// TODO Preconditions

		//TODO: Joinen mit dem darauffolgenden Flux.
		deleteAllRatings(incomingPublication.domain(), incomingPublication.year(), incomingPublication.businessVersion())
			.subscribe(
				i -> LOG.info("Old records deleted: Amount:  Publications={} Metrics={} Results={}", i.getT1(), i.getT2(), i.getT3()),
				e -> e.printStackTrace(),
				() -> LOG.info("Deletion done.")
			);

		Flux<Tuple3<RatingPublication, RatingMetric, RatingResult>> result2BeReduced =
			publicationRepo.save(outsideWorldMapper.map2Inner(incomingPublication, LocalDateTime.now()))
				.map(i -> Tuples.of(i, incomingPublicationEntries.entrySet()))
				.flatMapMany(i -> {
					final RatingPublication savedPublication = i.getT1();
					Map<RatingMetric, List<CreateResultRO>> mappedMetricsPriorSave = i.getT2().stream()
						.collect(Collectors.toMap(
							j -> outsideWorldMapper.map2Inner(j.getKey(), savedPublication.id()),
							j -> j.getValue()
						));
					return metricRepo
						.saveAll(mappedMetricsPriorSave.keySet())
						.map(j -> Tuples.of(savedPublication, j, mappedMetricsPriorSave.get(j)));
				})
				.flatMap(i -> {
					final RatingPublication savedPublication = i.getT1();
					final RatingMetric savedMetric = i.getT2();
					final List<RatingResult> mappedResultsPriorSave = i.getT3().stream()
						.map(j -> outsideWorldMapper.map2Inner(j, savedPublication.id(), savedMetric.id()))
						.collect(Collectors.toList());
					return resultRepo.saveAll(mappedResultsPriorSave)
						.map(j -> Tuples.of(savedPublication, savedMetric, j));
				})
			/**/;

		return reduce2HierarchicalResult(result2BeReduced);
	}


	@Transactional
	public Mono<Tuple3<Integer, Integer, Integer>> deleteAllRatings(final PublicationDomain domain, final Integer year, final String businessVersion) {
		// Note: Delete returns the amount of deleted rows
		return template.select(RatingPublication.class)
			.matching(
				query(where(RatingPublication.PROPERTYNAME_DOMAIN).is(domain)
					.and(RatingPublication.PROPERTYNAME_YEAR).is(year)
					.and(RatingPublication.PROPERTYNAME_BUSINESS_VERSION).is(businessVersion))
			)
			.one()
			.flatMap(i -> template
				.delete(RatingResult.class)
				.matching(
					query(where(RatingResult.PROPERTYNAME_FK_PUBLICATION_ID).is(i.id()))
				)
				.all()
				.map(j -> Tuples.of(i, j)))
			.flatMap(i -> template
				.delete(RatingMetric.class)
				.matching(
					query(where(RatingMetric.PROPERTYNAME_FK_PUBLICATION_ID).is(i.getT1().id()))
				)
				.all()
				.map(j -> Tuples.of(i.getT1(), j, i.getT2())))
			.flatMap(i -> template
				.delete(RatingPublication.class)
				.matching(
					query(where(RatingPublication.PROPERTYNAME_ID).is(i.getT1().id()))
				)
				.all()
				.map(j -> Tuples.of(i.getT1(), j, i.getT2(), i.getT3())))
			// return the amount of deleted entities/rows.
			.map(i -> Tuples.of(i.getT2(), i.getT3(), i.getT4()));
	}


	private PublicationWithMetricResultsRO map(Map<PublicationRO, Map<MetricRO, List<ResultRO>>> input) {
		if (input.size() != 1) {
			throw new IllegalStateException("Cannot be mapped to more than one unique publication");
		}
		PublicationRO publication = input.keySet().iterator().next();
		List<MetricWithResultsRO> metricWithResults = input.values().iterator().next().entrySet().stream()
			.map(i -> new MetricWithResultsRO(i.getKey(), i.getValue()))
			.collect(Collectors.toList());
		return new PublicationWithMetricResultsRO(publication, metricWithResults);
	}

	private Flux<Tuple3<RatingPublication, RatingMetric, RatingResult>> findAllRatingsByStream(final PublicationDomain domain, final Integer year, final String businessVersion) {
		return publicationRepo
			.findByMono(domain, year, businessVersion)
			.flatMapMany(i -> metricRepo.findBy(i.id())
				.map(j -> Tuples.of(i, j))
			)
			.flatMap(i -> resultRepo.findBy(i.getT1().id(), i.getT2().id())
				.map(j -> Tuples.of(i.getT1(), i.getT2(), j)));
	}

	private Mono<PublicationWithMetricResultsRO> reduce2HierarchicalResult(Flux<Tuple3<RatingPublication, RatingMetric, RatingResult>> input) {
		return input.reduce(
			// initial ...
			new PublicationWithMetricResultsRO(null, List.of()),
			// accumulator ...
			(prev, values) -> {
				Map<PublicationRO, Map<MetricRO, List<ResultRO>>> collector = new HashMap<>();
				if (prev.publication() != null) {
					final Map<MetricRO, List<ResultRO>> collect = prev.metricWithResults().stream()
						.collect(Collectors.toMap(
							i -> i.metric(),
							i -> i.results()
						));
					collector.putIfAbsent(prev.publication(), new HashMap<>());
					collector.get(prev.publication()).putAll(collect);
				}
				final PublicationRO vPublication = outsideWorldMapper.map2Outer(values.getT1());
				final MetricRO vMetric = outsideWorldMapper.map2Outer(values.getT2());
				final ResultRO vResult = outsideWorldMapper.map2Outer(values.getT3());
				collector.putIfAbsent(vPublication, new HashMap<>());
				collector.get(vPublication).putIfAbsent(vMetric, new ArrayList<>());
				collector.get(vPublication).get(vMetric).add(vResult);

				return map(collector);
			});
	}

}
