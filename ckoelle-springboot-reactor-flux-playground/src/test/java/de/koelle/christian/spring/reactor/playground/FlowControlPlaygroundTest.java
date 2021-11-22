package de.koelle.christian.spring.reactor.playground;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;

@SuppressWarnings("java:S2699") // S2699 At least one Assertion required.
class FlowControlPlaygroundTest {

	@Test
	void requestingLess() {
		Flux.range(1, 10)
			.doOnRequest(r -> System.out.println("request of " + r))
			.subscribe(new BaseSubscriber<>() {

				@Override
				public void hookOnSubscribe(Subscription subscription) {
					request(5);
				}

				@Override
				public void hookOnNext(Integer integer) {
					System.out.println("hookOnNext(): " + integer);
				}
			});
	}

	@Test
	void requestingLessWithCancel() {
		Flux.range(1, 10)
			.doOnRequest(r -> System.out.println("request of " + r))
			.subscribe(new BaseSubscriber<>() {

				@Override
				public void hookOnSubscribe(Subscription subscription) {
					request(5);
				}

				@Override
				public void hookOnNext(Integer integer) {
					System.out.println("hookOnNext(): " + integer);
					if (integer == 2) {
						System.out.println("Cancelling after having received: " + integer);
						cancel();
					}
				}
			});
	}
}
