package ssm.demo.core.processor.persistence;

import lombok.Getter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineException;
import org.springframework.statemachine.data.jpa.JpaPersistingStateMachineInterceptor;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptor;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;
import ssm.demo.core.processor.dto.process.ProcessEvent;
import ssm.demo.core.processor.dto.process.ProcessState;

@Getter
@Component
public class ProcessPersistenceInterceptor extends JpaPersistingStateMachineInterceptor<ProcessState, ProcessEvent, String> {

	private final ApplicationEventPublisher applicationEventPublisher;

	public ProcessPersistenceInterceptor(ProcessRepository repository,
	                                     ApplicationEventPublisher applicationEventPublisher) {

		super(repository);
		this.applicationEventPublisher = applicationEventPublisher;

	}

	@Override
	public StateMachineInterceptor<ProcessState, ProcessEvent> getInterceptor() {

		return this;

	}

	@Override
	public void postStateChange(State<ProcessState, ProcessEvent> state,
	                            Message<ProcessEvent> message,
	                            Transition<ProcessState, ProcessEvent> transition,
	                            StateMachine<ProcessState, ProcessEvent> stateMachine,
	                            StateMachine<ProcessState, ProcessEvent> rootStateMachine) {

		if (state != null && transition != null) {
			try {
				write(buildStateMachineContext(stateMachine, rootStateMachine, state), stateMachine.getId());
			} catch (Exception e) {
				throw new StateMachineException("Unable to persist stateMachineContext", e);
			}
		}

	}

	public void persist(State<ProcessState, ProcessEvent> state,
	                    StateMachine<ProcessState, ProcessEvent> stateMachine) {

		try {
			write(buildStateMachineContext(stateMachine, null, state), stateMachine.getId());
		} catch (Exception e) {
			throw new StateMachineException("Unable to persist stateMachineContext", e);
		}
	}
}