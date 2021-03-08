package ssm.demo.core.processor.engine;

import ssm.demo.core.processor.dto.process.ProcessEvent;
import ssm.demo.core.processor.dto.process.ProcessState;
import ssm.demo.core.processor.model.BilRetryTemplate;
import ssm.demo.core.processor.service.ProcessJob;
import ssm.demo.core.processor.service.ProcessService;
import org.quartz.JobBuilder;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.springframework.data.util.CastUtils;
import org.springframework.statemachine.action.Action;

import java.sql.Date;
import java.time.ZonedDateTime;

import static ssm.demo.core.processor.service.ProcessJob.MACHINE_ID;

public interface RetryableProcessEngine extends ProcessEngine {

	String ATTEMPTS_KEY = "attempts";

	@Override
	default Action<ProcessState, ProcessEvent> getFailedAction() {

		return context -> {

			final Class<? extends Throwable> throwableClass;
			if (!context.getExtendedState().getVariables().containsKey(THROWABLE_KEY)) {
				return;
			}
			try {
				throwableClass = CastUtils.cast(Class.forName(context.getExtendedState().get(THROWABLE_KEY, String.class)));
			} catch (ClassNotFoundException | ClassCastException e) {
				return;
			}

			if (this.getRetryTemplate().getCancelOnExceptions().contains(throwableClass)) {
				this.getService().cancel(context.getStateMachine());
			}
			else if (this.getRetryTemplate().getRetryOnExceptions().isEmpty() ||
			         this.getRetryTemplate().getRetryOnExceptions().contains(throwableClass)) {
				final Integer attempts;
				if (!context.getExtendedState().getVariables().containsKey(ATTEMPTS_KEY)) {
					attempts = 0;
				}
				else {
					attempts = context.getExtendedState().get(ATTEMPTS_KEY, Integer.class);
				}
				context.getExtendedState().getVariables().put(ATTEMPTS_KEY, attempts + 1);

				this.getRetryTemplate().getBackOffPolicy()
				    .apply(attempts + 1)
				    .ifPresent(retryDelay -> {
					               try {
						               this.getScheduler()
						                   .scheduleJob(
								                   JobBuilder.newJob(ProcessJob.class)
								                             .storeDurably()
								                             .usingJobData(MACHINE_ID, context.getStateMachine().getId())
								                             .build(),
								                   TriggerBuilder.newTrigger()
								                                 .startAt(Date.from(ZonedDateTime.now().plus(retryDelay).toInstant()))
								                                 .build()
						                               );
					               } catch (SchedulerException e) {
						               throw new RuntimeException(e);
					               }
				               }
				              );

			}


		};

	}

	BilRetryTemplate getRetryTemplate();

	ProcessService getService();

	Scheduler getScheduler();

}
