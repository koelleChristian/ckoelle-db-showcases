package de.koelle.christian.spring.r2dbc.ratings.mapping;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import de.koelle.christian.spring.r2dbc.ratings.dao.RatingMetric;
import de.koelle.christian.spring.r2dbc.ratings.dao.RatingPublication;
import de.koelle.christian.spring.r2dbc.ratings.dao.RatingResult;
import de.koelle.christian.spring.r2dbc.ratings.shared.exterior.CreateMetricRO;
import de.koelle.christian.spring.r2dbc.ratings.shared.exterior.CreatePublicationRO;
import de.koelle.christian.spring.r2dbc.ratings.shared.exterior.CreatePublicationWithMetricResultsRO;
import de.koelle.christian.spring.r2dbc.ratings.shared.exterior.CreateResultRO;
import de.koelle.christian.spring.r2dbc.ratings.shared.exterior.MetricRO;
import de.koelle.christian.spring.r2dbc.ratings.shared.exterior.PublicationMetricResultDenormalizedRO;
import de.koelle.christian.spring.r2dbc.ratings.shared.exterior.PublicationRO;
import de.koelle.christian.spring.r2dbc.ratings.shared.exterior.ResultRO;
import reactor.util.function.Tuple3;

/**
 * Hopefully, this code written by hand can be replaced by Mapstruct or whatever.
 */
public class OutsideInsideMapper {

	public PublicationMetricResultDenormalizedRO map2Outer(Tuple3<RatingPublication, RatingMetric, RatingResult> i) {
		final RatingPublication p = i.getT1();
		final RatingMetric m = i.getT2();
		final RatingResult r = i.getT3();
		return new PublicationMetricResultDenormalizedRO(p.id(), m.id(), r.id(), p.domain(), p.year(), p.businessVersion(), p.publicationTime(), m
			.metricNumber(), m.calculationType(), r.resultReference(), r.resultValue(), r.trend());
	}

	public PublicationRO map2Outer(final RatingPublication i) {
		return new PublicationRO(i.id(), i.domain(), i.year(), i.businessVersion(), i.publicationTime());
	}

	public RatingPublication map2Inner(final CreatePublicationRO i, final LocalDateTime publicationTime) {
		return new RatingPublication(null, i.domain(), i.year(), i.businessVersion(), publicationTime);
	}

	public MetricRO map2Outer(final RatingMetric i) {
		return new MetricRO(i.id(), i.fkPublicationId(), i.metricNumber(), i.calculationType());
	}

	public RatingMetric map2Inner(final CreateMetricRO i, final Integer fkPublicationId) {
		return new RatingMetric(null, fkPublicationId, i.metricNumber(), i.calculationType());
	}


	public ResultRO map2Outer(final RatingResult i) {
		return new ResultRO(i.id(), i.fkPublicationId(), i.fkMetricId(), i.resultReference(), i.resultValue(), i.trend());
	}

	public RatingResult map2Inner(final CreateResultRO i, final Integer fkPublicationId, final Integer fkMetricId) {
		return new RatingResult(null, fkPublicationId, fkMetricId, i.resultReference(), i.resultValue(), i.trend());
	}

	public Map<CreatePublicationRO, Map<CreateMetricRO, List<CreateResultRO>>> map2ReferenceMap(final CreatePublicationWithMetricResultsRO i) {
		Map<CreatePublicationRO, Map<CreateMetricRO, List<CreateResultRO>>> result = new HashMap<>();
		result.putIfAbsent(i.publication(), new HashMap<>());
		result.get(i.publication()).putAll(i.metricWithResults().stream()
			.collect(Collectors.toMap(
				j -> j.metric(),
				j -> j.results()
			)));
		return result;
	}
}
