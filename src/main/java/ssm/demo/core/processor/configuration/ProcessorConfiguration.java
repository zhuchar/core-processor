package ssm.demo.core.processor.configuration;

import ssm.demo.core.processor.dto.process.ProcessEvent;
import ssm.demo.core.processor.dto.process.ProcessState;
import ssm.demo.core.processor.persistence.ProcessPersistenceInterceptor;
import ssm.demo.core.processor.persistence.ProcessRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.service.DefaultStateMachineService;
import org.springframework.statemachine.service.StateMachineService;

import static org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType.HAL;
import static org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType.HAL_FORMS;

@EnableScheduling
@EnableJpaRepositories(basePackageClasses = {ProcessRepository.class})
@EnableCaching
@Slf4j
@Import({
		        StateMachineFactoryConfiguration.class,
		        ProcessPersistenceInterceptor.class
        })
@EnableHypermediaSupport(type = {HAL, HAL_FORMS})
@Configuration(proxyBeanMethods = false)
@Getter
public class ProcessorConfiguration {

	@Bean
	public MessageConverter jsonMessageConverter() {

		return new Jackson2JsonMessageConverter();

	}

	/**
	 * This bean creates a service to allow easier interaction to multiple state machines
	 *
	 * @param stateMachineFactory           the factory which creates state machines
	 * @param processPersistenceInterceptor the interceptor which persists state machines on state change
	 * @return the state machine service
	 */
	@Bean
	public StateMachineService<ProcessState, ProcessEvent> stateMachineService(@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") StateMachineFactory<ProcessState, ProcessEvent> stateMachineFactory,
																			   ProcessPersistenceInterceptor processPersistenceInterceptor
	                                                                          ) {

		return new DefaultStateMachineService<>(stateMachineFactory, processPersistenceInterceptor);

	}

}
