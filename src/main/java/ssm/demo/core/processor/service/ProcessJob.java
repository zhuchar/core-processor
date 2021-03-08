package ssm.demo.core.processor.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Getter
@Component
public class ProcessJob extends QuartzJobBean {

	public static final String MACHINE_ID = "machineId";

	private final ProcessService processService;

	@Override
	protected void executeInternal(JobExecutionContext context) {

		this.getProcessService().process(context.getJobDetail().getJobDataMap().getString(MACHINE_ID), null);


	}

}
