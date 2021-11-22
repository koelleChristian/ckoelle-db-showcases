package de.koelle.christian.spring.r2dbc.bug1;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface MyRecordRepository extends ReactiveCrudRepository<MyRecord, Long> {

}
