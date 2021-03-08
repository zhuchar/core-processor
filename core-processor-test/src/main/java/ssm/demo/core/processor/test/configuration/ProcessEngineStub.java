package ssm.demo.core.processor.test.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.guard.Guard;
import ssm.demo.core.processor.dto.process.ProcessEvent;
import ssm.demo.core.processor.dto.process.ProcessState;
import ssm.demo.core.processor.engine.ProcessEngine;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Getter
public class ProcessEngineStub implements ProcessEngine {

	@Override
	public Action<ProcessState, ProcessEvent> getCancelAction() {

		return context -> log.debug(context.toString());

	}

	@Override
	public Action<ProcessState, ProcessEvent> getCompleteAction() {

		return context -> log.debug(context.toString());

	}

	@Override
	public Guard<ProcessState, ProcessEvent> getCompleteGuard() {

		return context -> "TRUE".equals(context.getExtendedState().getVariables().get(ProcessEngine.PROCESS_DATA_KEY));

	}

	@Override
	public Action<ProcessState, ProcessEvent> getFailedAction() {

		return context -> log.debug(context.toString());

	}

	@Override
	public Action<ProcessState, ProcessEvent> getInitializeAction() {

		return context -> {

			Optional.ofNullable(context.getMessage())
			        .map(message -> message.getHeaders().get(ProcessEngine.INITIAL_DATA_KEY))
			        .ifPresent(initialData -> {
				        final String[] split = String.valueOf(initialData).split("-");
				        if (split.length > 0) {
					        context.getExtendedState().getVariables().put(ProcessEngine.INITIAL_DATA_KEY, split[0]);
				        }
				        if (split.length > 1) {
					        context.getExtendedState().getVariables().put(ProcessEngine.PROCESS_DATA_KEY, split[1]);
				        }
			        });
			log.debug(context.toString());

		};

	}

	@Override
	public Action<ProcessState, ProcessEvent> getProcessAction() {

		return context -> {

			Optional.ofNullable(context.getMessage().getHeaders().get(ProcessEngine.PROCESS_DATA_KEY))
			        .ifPresent(processData -> context.getExtendedState().getVariables().put(ProcessEngine.PROCESS_DATA_KEY, processData));
			log.debug(context.getSource().getId() + " -> " + context.getTarget().getId());

		};

	}

	@Override
	public Guard<ProcessState, ProcessEvent> getProcessGuard() {

		return context -> "TRUE".equals(context.getExtendedState().getVariables().get(ProcessEngine.INITIAL_DATA_KEY));

	}

	@Override
	public Logger getLogger() {

		return log;
	}

}
