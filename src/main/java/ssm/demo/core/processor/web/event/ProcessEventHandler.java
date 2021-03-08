package ssm.demo.core.processor.web.event;

import ssm.demo.core.processor.dto.process.ProcessState;
import ssm.demo.core.processor.dto.process.ProcessWrapper;
import ssm.demo.core.processor.service.ProcessService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.util.annotation.NonNull;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;

@Getter
@RequiredArgsConstructor
public abstract class ProcessEventHandler<INITIALIZE, PROCESS> implements ApplicationListener<ProcessStateChangeEvent> {

	private final Map<ProcessState, EmitterProcessor<String>> emitterProcessorMap = Arrays.stream(ProcessState.values()).collect(Collectors.toMap(identity(), type -> EmitterProcessor.create()));

	private final ProcessService service;

	/**
	 * This bean exposes a producer which outputs all Ids of process when placed in a cancelled state
	 *
	 * @return the supplier of the process Ids
	 */
	@Bean
	public Supplier<Flux<String>> cancelled() {

		return () -> this.getEmitterProcessorMap().get(ProcessState.CANCELLED);

	}

	/**
	 * This bean exposes a producer which outputs all Ids of process when placed in a complete state
	 *
	 * @return the supplier of the process Ids
	 */
	@Bean
	public Supplier<Flux<String>> complete() {

		return () -> this.getEmitterProcessorMap().get(ProcessState.COMPLETE);

	}

	/**
	 * This bean exposes a consumer to create a process
	 *
	 * @return the consumer which initializes a process
	 */
	public Consumer<INITIALIZE> create() {

		return initialPayload -> this.getService().initialize(initialPayload);

	}

	/**
	 * This bean exposes a consumer to delete a process by process Id
	 *
	 * @return the consumer which cancels a process
	 */
	@Bean
	public Consumer<String> cancel() {

		return id -> this.getService().cancel(id);

	}

	/**
	 * This bean exposes a producer which outputs all Ids of process when placed in a failed state
	 *
	 * @return the supplier of the process Ids
	 */
	@Bean
	public Supplier<Flux<String>> failed() {

		return () -> this.getEmitterProcessorMap().get(ProcessState.FAILED);

	}

	@Override
	public void onApplicationEvent(@NonNull ProcessStateChangeEvent event) {

		Optional.ofNullable(this.getEmitterProcessorMap().get(event.getState())).ifPresent(stringEmitterProcessor -> stringEmitterProcessor.onNext(String.valueOf(event.getSource())));

	}

	/**
	 * This bean exposes a producer which outputs all Ids of process when placed in a pending state
	 *
	 * @return the supplier of the process Ids
	 */
	@Bean
	public Supplier<Flux<String>> pending() {

		return () -> this.getEmitterProcessorMap().get(ProcessState.PENDING);

	}

	/**
	 * This bean exposes a consumer to process a process
	 *
	 * @return the consumer which processes a process
	 */
	public Consumer<ProcessWrapper<PROCESS>> process() {

		return processWrapper -> this.getService().process(processWrapper.getId(), processWrapper.getPayload());

	}

	/**
	 * This bean exposes a producer which outputs all Ids of process when placed in a processing state
	 *
	 * @return the supplier of the process Ids
	 */
	@Bean
	public Supplier<Flux<String>> processing() {

		return () -> this.getEmitterProcessorMap().get(ProcessState.PROCESSING);

	}


}
