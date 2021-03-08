package ssm.demo.core.processor.configuration;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import ssm.demo.core.processor.dto.process.ProcessEvent;
import ssm.demo.core.processor.dto.process.ProcessState;
import ssm.demo.core.processor.engine.ProcessEngine;
import ssm.demo.core.processor.persistence.ProcessPersistenceInterceptor;
import ssm.demo.core.processor.service.RedisLockService;
import ssm.demo.core.processor.web.event.ProcessStateChangeEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import static ssm.demo.core.processor.dto.process.ProcessEvent.CANCEL;
import static ssm.demo.core.processor.dto.process.ProcessEvent.PROCESS;
import static ssm.demo.core.processor.dto.process.ProcessState.*;
import static ssm.demo.core.processor.engine.ProcessEngine.THROWABLE_KEY;

@Configuration(proxyBeanMethods = false)
@EnableStateMachineFactory
@Slf4j
@Getter
@RequiredArgsConstructor
public class StateMachineFactoryConfiguration extends StateMachineConfigurerAdapter<ProcessState, ProcessEvent> {

	private final ProcessEngine ProcessEngine;

	private final ApplicationEventPublisher applicationEventPublisher;

	private final ProcessPersistenceInterceptor processPersistenceInterceptor;

	private final RedissonClient redisson;

	@Value("${spring.application.name}")
	private String appName;

	@Override
	public void configure(StateMachineConfigurationConfigurer<ProcessState, ProcessEvent> config) throws Exception {

		config.withPersistence().runtimePersister(this.getProcessPersistenceInterceptor());

	}

	@Override
	public void configure(StateMachineStateConfigurer<ProcessState, ProcessEvent> states) throws Exception {

		states.withStates()
		      .initial(PENDING)
		      .state(PENDING, this.chainPublishEvent(PENDING, this.getProcessEngine().getInitializeAction()))
		      .choice(PROCESSING)
		      .state(FAILED, this.chainPublishEvent(FAILED, this.getProcessEngine().getFailedAction()))
		      .end(CANCELLED)
		      .state(CANCELLED, this.chainPublishEvent(CANCELLED, this.getProcessEngine().getCancelAction()))
		      .end(COMPLETE);

	}

	private Action<ProcessState, ProcessEvent> chainPublishEvent(ProcessState target,
	                                                             Action<ProcessState, ProcessEvent> chain) {

		return context -> {
			this.getApplicationEventPublisher()
			    .publishEvent(new ProcessStateChangeEvent(context.getStateMachine().getId(), target));

			//distributed lock
			String lockId = RedisLockService.getLockId(appName, context.getStateMachine().getId());
			RedisLockService.lock(redisson, lockId , String.valueOf(target));

			try {
				chain.execute(context);
			} catch (Throwable e) {
				log.error("Error caught performing action", e);
				context.getExtendedState().getVariables().put(THROWABLE_KEY, e.getClass().getName());
			} finally {
				this.getProcessPersistenceInterceptor().persist(context.getStateMachine().getState(), context.getStateMachine());

				//distributed unlock
				RedisLockService.unlock(redisson, lockId, String.valueOf(target));
			}
		};

	}

	@Override
	public void configure(StateMachineTransitionConfigurer<ProcessState, ProcessEvent> transitions) throws Exception {

		transitions
				//request to process
				.withExternal()
				.source(PENDING)
				.event(PROCESS)
				.target(PROCESSING)
				.guard(this.getProcessEngine().getProcessGuard())
				.action(this.chainPublishEvent(PROCESSING, this.getProcessEngine().getProcessAction()))
				//result of processing from pending
				.and().withChoice()
				.source(PROCESSING)
				.first(COMPLETE, this.getProcessEngine().getCompleteGuard(), this.chainPublishEvent(COMPLETE, this.getProcessEngine().getCompleteAction()))
				.last(FAILED)
				//explicit request to retry processing from failed
				.and().withExternal()
				.source(FAILED)
				.event(PROCESS)
				.target(PROCESSING)
				.guard(this.getProcessEngine().getProcessGuard())
				.action(this.chainPublishEvent(PROCESSING, this.getProcessEngine().getProcessAction()))
				//result of processing from failed
				.and().withChoice()
				.source(FAILED)
				.first(COMPLETE, this.getProcessEngine().getCompleteGuard(), this.chainPublishEvent(COMPLETE, this.getProcessEngine().getCompleteAction()))
				.last(FAILED)
				//explicit cancel request from pending
				.and().withExternal()
				.source(PENDING)
				.event(CANCEL)
				.target(CANCELLED)
				//explicit cancel request from processing
				.and().withExternal()
				.source(PROCESSING)
				.event(CANCEL)
				.target(CANCELLED)
				//explicit cancel request from failed
				.and().withExternal()
				.source(FAILED)
				.event(CANCEL)
				.target(CANCELLED);

	}


}
