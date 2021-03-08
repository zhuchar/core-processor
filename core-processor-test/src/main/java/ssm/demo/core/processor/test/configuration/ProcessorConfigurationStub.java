package ssm.demo.core.processor.test.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.service.StateMachineService;
import ssm.demo.core.processor.EnableBilProcessor;
import ssm.demo.core.processor.dto.process.ProcessEvent;
import ssm.demo.core.processor.dto.process.ProcessState;
import ssm.demo.core.processor.engine.ProcessEngine;
import ssm.demo.core.processor.persistence.ProcessRepository;
import ssm.demo.core.processor.service.ClusterStateMachineService;
import ssm.demo.core.processor.service.ProcessService;

@EnableBilProcessor
@Configuration
public class ProcessorConfigurationStub {

	@Bean
	public ProcessEngine processConfigurationStub() {

		return new ProcessEngineStub();

	}

	@Bean
	public ProcessService processService(ProcessRepository processRepository,
										 ClusterStateMachineService<ProcessState, ProcessEvent> stateMachineService) {

		return new ProcessService() {

			@Override
			public ProcessRepository getRepository() {

				return processRepository;
			}

			@Override
			public ClusterStateMachineService<ProcessState, ProcessEvent> getStateMachineService() {

				return stateMachineService;
			}
		};
	}

}
