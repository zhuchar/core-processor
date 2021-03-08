package ssm.demo.core.processor.test.configuration;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.data.jpa.JpaRepositoryStateMachine;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;
import org.springframework.test.annotation.DirtiesContext;
import ssm.demo.core.processor.dto.process.ProcessEvent;
import ssm.demo.core.processor.dto.process.ProcessState;
import ssm.demo.core.processor.persistence.ProcessRepository;

import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static ssm.demo.core.processor.dto.process.ProcessState.*;
import static ssm.demo.core.processor.engine.ProcessEngine.INITIAL_DATA_KEY;
import static ssm.demo.core.processor.engine.ProcessEngine.PROCESS_DATA_KEY;

@Slf4j
@Getter
@SpringBootTest(
		properties = {
				"spring.cloud.config.enabled=false",
				"server.shutdown=graceful"
		}
)
public class ProcessorConfigurationTest {

	@Autowired
	protected ProcessRepository repository;

	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	@Autowired
	private StateMachineFactory<ProcessState, ProcessEvent> stateMachineFactory;


	@SneakyThrows
	@DirtiesContext
	@Test
	public void stateMachinePaths() {

		this.plan("machine1", "FALSE", "FALSE", PENDING).and().build().test();
		this.plan("machine2", "FALSE", "TRUE", PENDING).and().build().test();
		this.plan("machine3", "TRUE", "FALSE", FAILED).and().build().test();
		this.plan("machine4", "TRUE", "TRUE", COMPLETE).and().build().test();

		this.plan("machine5", "FALSE", "FALSE", PENDING)
		    .and().step().sendEvent(ProcessEvent.CANCEL).expectState(CANCELLED)
		    .and().build().test();
		this.plan("machine6", "TRUE", "FALSE", FAILED)
		    .and().step().sendEvent(ProcessEvent.CANCEL).expectState(CANCELLED)
		    .and().build().test();

		final ArrayList<JpaRepositoryStateMachine> jpaRepositoryStateMachines = Lists.newArrayList(this.getRepository().findAll().iterator());
		assertThat(jpaRepositoryStateMachines, hasSize(6));

	}

	@SneakyThrows
	protected StateMachineTestPlanBuilder<ProcessState, ProcessEvent>.StateMachineTestPlanStepBuilder plan(String machineId,
	                                                                                                       String initialData,
	                                                                                                       String processData,
	                                                                                                       ProcessState expectedState
	                                                                                                      ) {

		final StateMachine<ProcessState, ProcessEvent> machine1 = this.getStateMachineFactory().getStateMachine(machineId);
		machine1.getExtendedState().getVariables().put(INITIAL_DATA_KEY, initialData);
		machine1.getExtendedState().getVariables().put(PROCESS_DATA_KEY, processData);

		return StateMachineTestPlanBuilder.<ProcessState, ProcessEvent>builder()
				.stateMachine(machine1)
				.step().expectState(PENDING)
				.and().step().sendEvent(MessageBuilder.withPayload(ProcessEvent.PROCESS).build())
				.expectState(expectedState);

	}

}
