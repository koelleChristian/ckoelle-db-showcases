package de.koelle.christian.spring.reactor.playground;


import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

@SuppressWarnings("java:S2699") // S2699: Tests should include assertions
class FluxAndMonoPlaygroundTest {

	@Test
	void fluxFromObject() {
		Flux<String> seq1 = Flux.just("foo", "bar", "foobar");
		List<String> iterable = Arrays.asList("foo", "bar", "foobar");
		Flux<String> seq2 = Flux.fromIterable(iterable);
		seq1.subscribe(System.out::println);
		seq2.subscribe(System.out::println);
		Flux.range(5, 10).subscribe(System.out::println);
	}

	@Test
	void monoFromObject() {
		Mono<String> s1 = Mono.just("foo");
		s1.subscribe(System.out::println);
	}

	@Test
	void whateverMono2() {
		Mono.just("foo")
			.flatMapMany(i -> Flux.fromIterable(createTestRecords(10, i)))
			.subscribe(System.out::println);
	}

	@Test
	void zipWith() {
		Mono.just("foo1")
			.zipWith(Mono.just(Integer.valueOf(10)))
			.map(i -> i.mapT2(j -> i.getT1() + j))
			.subscribe(System.out::println);


		Mono.just("foo2")
			.flux()
			.zipWith(Flux.range(1, 10))
			.map(i -> i.mapT2(j -> i.getT1() + "__" + j))
			.subscribe(System.out::println);

		Flux.range(1, 10)
			.zipWith(Mono.just("foo3").flux())
			.subscribe(System.out::println);
	}

	@Test
	void combineMonoWithFlux() {
		Mono<String> mono1 = Mono.just("x");
		Flux<String> flux1 = Flux.just("{1}", "{2}", "{3}", "{4}");

		mono1
			.flatMapMany(m -> flux1.map(f -> Tuples.of(m, f)))
			.map(i -> Tuples.of(i.getT1(), i.getT2(), i.getT1() + "___" + i.getT2()))
			.subscribe(System.out::println);

		mono1
			.flatMapMany(m -> flux1.map(f -> Tuples.of(m, f, m + "___" + f)))
			.subscribe(System.out::println);
	}

	@Test
	void combineFluxWithMono() {
		Mono<String> mono1 = Mono.just("x");
		Flux<String> flux1 = Flux.just("{1}", "{2}", "{3}", "{4}");

		flux1
			.flatMap(f -> mono1.map(m -> Tuples.of(f, m)))
			.map(i -> Tuples.of(i.getT1(), i.getT2(), i.getT1() + "___" + i.getT2()))
			.subscribe(System.out::println);

		flux1
			.flatMap(f -> mono1.map(m -> Tuples.of(f, m, f + "___" + m)))
			.subscribe(System.out::println);
	}

	@Test
	void combineFluxWithFlux() {
		Flux<String> flux1 = Flux.just("{1}", "{2}", "{3}", "{4}");
		Flux<String> flux2 = Flux.just("a", "b", "c", "d");
		flux1.flatMap(m -> flux2.map(x -> Tuples.of(m, x, m + "___" + x)));
	}






	List<TestRecord> createTestRecords(int amount, String idParent) {
		return IntStream.range(1, amount)
			.mapToObj(i -> new TestRecord(idParent, "child" + i))
			.collect(Collectors.toList());
	}

}
