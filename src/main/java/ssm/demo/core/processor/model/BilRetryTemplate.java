package ssm.demo.core.processor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Data
public class BilRetryTemplate {

	@Builder.Default
	private Function<Integer, Optional<Duration>> backOffPolicy = BilRetryTemplate.noBackOffPolicy();

	@Singular
	private Set<Class<? extends Throwable>> cancelOnExceptions;

	@Singular
	private Set<Class<? extends Throwable>> retryOnExceptions;

	public static Function<Integer, Optional<Duration>> exponentialBackOffPolicy(Duration maxInterval,
	                                                                             Duration initialInterval,
	                                                                             Integer multiplier) {


		assert (!maxInterval.isNegative() && !maxInterval.isZero());

		return integer -> {
			final Optional<Duration> waitTime = BilRetryTemplate.exponentialBackOffPolicy(initialInterval, multiplier).apply(integer);
			if (waitTime.isEmpty() || waitTime.get().toMillis() > maxInterval.toMillis()) {
				return Optional.empty();
			}
			return waitTime;
		};

	}

	public static Function<Integer, Optional<Duration>> exponentialBackOffPolicy(Duration initialInterval,
	                                                                             Integer multiplier) {

		assert (!initialInterval.isNegative() && !initialInterval.isZero());
		assert (multiplier > 1);

		return integer -> Optional.of(Duration.ofMillis(Math.multiplyExact(Math.multiplyExact(integer, initialInterval.toMillis()), multiplier)));

	}

	public static Function<Integer, Optional<Duration>> fixedBackOffPolicy(Integer maxRetries,
	                                                                       Duration waitTime) {

		assert (maxRetries >= 0);
		assert (!waitTime.isNegative() && !waitTime.isZero());

		return integer -> {
			if (integer > maxRetries) {
				return Optional.empty();
			}
			return BilRetryTemplate.fixedBackOffPolicy(waitTime).apply(integer);
		};

	}

	public static Function<Integer, Optional<Duration>> fixedBackOffPolicy(Duration waitTime) {

		assert (!waitTime.isNegative() && !waitTime.isZero());

		return integer -> Optional.of(waitTime);

	}

	public static Function<Integer, Optional<Duration>> noBackOffPolicy(Integer maxRetries) {

		assert (maxRetries >= 0);

		return integer -> {
			if (integer > maxRetries) {
				return Optional.empty();
			}
			return BilRetryTemplate.noBackOffPolicy().apply(integer);
		};

	}

	public static Function<Integer, Optional<Duration>> noBackOffPolicy() {

		return integer -> Optional.of(Duration.ZERO);

	}

}
