package ssm.demo.core.processor.engine;

import org.slf4j.Logger;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.guard.Guard;
import ssm.demo.core.processor.dto.process.ProcessEvent;
import ssm.demo.core.processor.dto.process.ProcessState;

public interface ProcessEngine {

	String INITIAL_DATA_KEY = "initialData";

	String PROCESS_DATA_KEY = "processData";

	String THROWABLE_KEY = "throwable";

	default Action<ProcessState, ProcessEvent> getCancelAction() {

		return context -> {};

	}

	default Action<ProcessState, ProcessEvent> getCompleteAction() {

		return context -> {};

	}

	default Guard<ProcessState, ProcessEvent> getCompleteGuard() {

		return context -> true;

	}

	default Action<ProcessState, ProcessEvent> getFailedAction() {

		return context -> {};

	}

	default Action<ProcessState, ProcessEvent> getInitializeAction() {

		return context -> {};

	}

	Logger getLogger();

	default Action<ProcessState, ProcessEvent> getProcessAction() {

		return context -> {};

	}

	default Guard<ProcessState, ProcessEvent> getProcessGuard() {

		return context -> true;

	}

}
