package ssm.demo.core.processor.service;

import ssm.demo.core.processor.dto.process.ProcessEvent;
import ssm.demo.core.processor.dto.process.ProcessState;
import ssm.demo.core.processor.persistence.ProcessRepository;
import org.springframework.data.util.CastUtils;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.service.StateMachineService;
import reactor.util.annotation.Nullable;
import ssm.demo.core.processor.engine.ProcessEngine;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public interface ProcessService {

	/**
	 * Redirects a process to the cancelled state
	 *
	 * @param machineId the Id of the process being cancelled
	 * @return true if successful
	 */
	default Boolean cancel(String machineId) {

		if (!this.getRepository().existsById(machineId)) {
			return false;
		}

		return this.cancel(this.getStateMachineService().acquireStateMachine(machineId));

	}

	ProcessRepository getRepository();

	/**
	 * Redirects a process to the cancelled state
	 *
	 * @param stateMachine the process being cancelled
	 * @return true if successful
	 */
	default Boolean cancel(StateMachine<ProcessState, ProcessEvent> stateMachine) {

		if (stateMachine.sendEvent(MessageBuilder.withPayload(ProcessEvent.CANCEL).build())) {
			this.getStateMachineService().releaseStateMachine(stateMachine.getId());
			return true;
		}

		return false;

	}

	StateMachineService<ProcessState, ProcessEvent> getStateMachineService();

	/**
	 * Retrieves a state machine with a specific machine Id
	 *
	 * @param machineId the Id of the state machine
	 * @return the state machine
	 */
	default Optional<StateMachine<ProcessState, ProcessEvent>> getStateMachine(String machineId) {

		if (!this.getRepository().existsById(machineId)) {
			return Optional.empty();
		}

		return Optional.ofNullable(this.getStateMachineService().acquireStateMachine(machineId));

	}

	/**
	 * Gets a variable which is automatically casted into a type. From the extended state of the state machine.
	 *
	 * @param machineId      the Id of the machine which contains the variable
	 * @param variableName   the key used to determine the correct variable
	 * @param <PAYLOAD_TYPE> the return type of the variable
	 * @return the variable within the extended state of the state machine
	 */
	default <PAYLOAD_TYPE> Optional<PAYLOAD_TYPE> getVariable(String machineId,
	                                                          String variableName) {

		if (!this.getRepository().existsById(machineId)) {
			return Optional.empty();
		}
		final Map<Object, Object> variables = this.getStateMachineService().acquireStateMachine(machineId, false).getExtendedState().getVariables();
		if (null == variables.get(variableName)) {
			return Optional.empty();
		}
		return Optional.of(CastUtils.cast(variables.get(variableName)));

	}

	/**
	 * Gets the names of all the variables which exist in a specific machine extended state
	 *
	 * @param machineId the machine which will have the extended state variable names
	 * @return a set containing the keys/names to all existing variables in an extended state of a specific machine
	 */
	default Set<String> getVariableNames(String machineId) {

		if (!this.getRepository().existsById(machineId)) {
			return Collections.emptySet();
		}
		return this.getStateMachineService().acquireStateMachine(machineId, false)
		           .getExtendedState()
		           .getVariables()
		           .keySet()
		           .stream()
		           .map(Object::toString)
		           .collect(Collectors.toSet());
	}


	/**
	 * Initializes a process given an initial payload
	 *
	 * @param initialData    the initial payload required to start a specific process
	 * @param <PAYLOAD_TYPE> the type of payload expected to start a specific process
	 * @return the Id of the newly created process
	 */
	default <PAYLOAD_TYPE> String initialize(@Nullable PAYLOAD_TYPE initialData) {

		final StateMachine<ProcessState, ProcessEvent> stateMachine = this.getStateMachineService().acquireStateMachine(UUID.randomUUID().toString(), false);
		if (null != initialData) {
			stateMachine.getExtendedState().getVariables().put(ProcessEngine.INITIAL_DATA_KEY, initialData);

		}
		stateMachine.start();
		return stateMachine.getId();

	}

	/**
	 * Initiates a transition to the process state.
	 * The state being a choice state which will divide successes and failures.
	 *
	 * @param machineId      the Id of the process being triggered
	 * @param payload        the required payload needed for the processing to occur
	 * @param <PAYLOAD_TYPE> the type of payload needed to process
	 * @return the Id of the process
	 */
	default <PAYLOAD_TYPE> Optional<String> process(String machineId,
	                                                @Nullable PAYLOAD_TYPE payload) {

		if (!this.getRepository().existsById(machineId)) {
			return Optional.empty();
		}
		return this.process(this.getStateMachineService().acquireStateMachine(machineId), payload);

	}

	/**
	 * Initiates a transition to the process state.
	 * The state being a choice state which will divide successes and failures.
	 *
	 * @param stateMachine   process being triggered
	 * @param payload        the required payload needed for the processing to occur
	 * @param <PAYLOAD_TYPE> the type of payload needed to process
	 * @return the Id of the process
	 */
	default <PAYLOAD_TYPE> Optional<String> process(StateMachine<ProcessState, ProcessEvent> stateMachine,
	                                                @Nullable PAYLOAD_TYPE payload) {

		final MessageBuilder<ProcessEvent> messageBuilder = MessageBuilder.withPayload(ProcessEvent.PROCESS);
		Optional.ofNullable(payload).ifPresent(safePayload -> messageBuilder.setHeader(ProcessEngine.PROCESS_DATA_KEY, payload));
		final Message<ProcessEvent> processEventMessage = messageBuilder.build();

		if (stateMachine.sendEvent(processEventMessage)) {
			return Optional.ofNullable(stateMachine.getId());
		}

		return Optional.empty();

	}

	/**
	 * Checks to see if the process exists with a specified Id
	 *
	 * @param id the id of the process
	 * @return true if the process exists and false if it does not
	 */
	default Boolean processExists(String id) {

		return this.getRepository().existsById(id);

	}

	/**
	 * Reads the current state of a running process
	 *
	 * @param machineId the identifier of the process
	 * @return the state which the process is in
	 */
	default Optional<ProcessState> read(String machineId) {

		if (!this.getRepository().existsById(machineId)) {
			return Optional.empty();
		}

		return Optional.ofNullable(this.getStateMachineService().acquireStateMachine(machineId, false).getState().getId());

	}

}