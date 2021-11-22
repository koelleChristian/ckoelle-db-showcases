package de.koelle.christian.spring.reactor.playground;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

class StepVerifierPlaygroundTest {

	@Test
	void verifyException() {
		Flux<Integer> flux = Flux.range(1, 6)
			.map(i -> {
				if (i <= 3) {
					return i;
				}
				throw new IllegalStateException("Got to 4");
			});
		flux.as(StepVerifier::create)
			.expectNext(1,2,3)
			.expectError(IllegalStateException.class)
			.verify();
	}

	@Test
	void verifyZipResult() {

		Flux<Integer> evenNumbers = Flux
			.range(1, 6)
			.filter(x -> x % 2 == 0); // i.e. 2, 4

		Flux<Integer> oddNumbers = Flux
			.range(1, 6)
			.filter(x -> x % 2 > 0);  // ie. 1, 3, 5

		Flux<Integer> fluxOfIntegers = Flux.zip(
			evenNumbers,
			oddNumbers,
			(a, b) -> a + b);

		StepVerifier.create(fluxOfIntegers)
			.expectNext(3) // 2 + 1
			.expectNext(7) // 4 + 3
			.expectNext(11) // 6 + 5
			.expectComplete()
			.verify();
	}
}
