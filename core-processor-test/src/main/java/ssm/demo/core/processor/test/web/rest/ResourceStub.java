package ssm.demo.core.processor.test.web.rest;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.RestController;
import ssm.demo.core.processor.configuration.ProcessorConfiguration;
import ssm.demo.core.processor.service.ProcessService;
import ssm.demo.core.processor.web.rest.ProcessResource;

@Slf4j
@Getter
@RequiredArgsConstructor
@RestController
public class ResourceStub implements ProcessResource<String, String> {

	private final ProcessorConfiguration processorConfiguration;

	private final ProcessService service;


	@Override
	public Logger getLogger() {

		return log;
	}

}
