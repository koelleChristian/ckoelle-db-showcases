package de.koelle.christian.spring.r2dbc.ratings.dao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import de.koelle.christian.spring.r2dbc.ratings.shared.exterior.PublicationMetricResultDenormalizedRO;
import de.koelle.christian.spring.r2dbc.ratings.shared.types.CalculationType;
import de.koelle.christian.spring.r2dbc.ratings.shared.types.PublicationDomain;
import de.koelle.christian.spring.r2dbc.ratings.shared.types.Trend;
import io.r2dbc.spi.Row;

@ReadingConverter
public class PublicationMetricResultDenormalizedReadConverter implements Converter<Row, PublicationMetricResultDenormalizedRO> {

	/**
	 * If I use
	 * <pre>
	 * r.get("publicationDomain", PublicationDomain.class))
	 * </pre>
	 * instead of
	 * <pre>
	 * PublicationDomain.valueOf(r.get("publicationDomain", String.class))
	 * </pre>
	 * then I get
	 * <pre>
	 * Cannot decode value of type class [EnumType] for 253 with collation 45
	 * </pre>
	 * this might be due to the fact that the application starts up with
	 * <pre>
	 * ... MySqlConnection: The server timezone is <Mitteleurop?ische Zeit> that's unknown, trying to use system default timezone
	 * ... or due to the custom collation
	 * </pre>
	 */
	public PublicationMetricResultDenormalizedRO convert(Row r) {
		return new PublicationMetricResultDenormalizedRO(
			r.get("idPublication", Integer.class),
			r.get("idMetric", Integer.class),
			r.get("idResult", Integer.class),
			PublicationDomain.valueOf(r.get("publicationDomain", String.class)), // see javadoc
			r.get("publicationYear", Integer.class),
			r.get("publicationBusinessVersion", String.class),
			r.get("publicationTime", LocalDateTime.class),
			r.get("metricNumber", String.class),
			CalculationType.valueOf(r.get("metricCalculationType", String.class)), // see javadoc
			r.get("resultReference", String.class),
			r.get("resultValue", BigDecimal.class),
			Trend.valueOf(r.get("resultTrend", String.class)) // see javadoc
		);
	}
}
