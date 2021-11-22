package de.koelle.christian.spring.r2dbc.bug1;

import java.util.Objects;
import org.springframework.data.annotation.Id;
public record MyRecord(@Id Long id, String whatever) {

	public boolean hasId() {
		return id != null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof MyRecord other)) {
			return false;
		}
		return Objects.equals(whatever, other.whatever);
	}

	@Override
	public int hashCode() {
		return Objects.hash(whatever);
	}
}
