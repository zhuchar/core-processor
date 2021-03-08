package ssm.demo.core.processor.test.web.event;

import org.springframework.stereotype.Component;
import ssm.demo.core.processor.service.ProcessService;
import ssm.demo.core.processor.web.event.ProcessEventHandler;

@Component
public class ProcessEventHandlerStub extends ProcessEventHandler<String, String> {

	public ProcessEventHandlerStub(ProcessService service) {

		super(service);
	}

}
