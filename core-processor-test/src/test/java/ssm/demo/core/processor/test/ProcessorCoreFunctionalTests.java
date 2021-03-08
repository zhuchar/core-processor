package ssm.demo.core.processor.test;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.statemachine.data.jpa.JpaRepositoryStateMachine;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ssm.demo.core.processor.dto.process.ProcessState;
import ssm.demo.core.processor.persistence.ProcessRepository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static ssm.demo.core.processor.dto.process.ProcessState.*;


@Getter
@Slf4j
@Tags({
		      @Tag("functional-test"),
		      @Tag("contextual"),
		      @Tag("test-containers")
      })
@SpringBootTest(
		classes = {TestProcessorApplication.class, TestChannelBinderConfiguration.class},
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = {
				"spring.cloud.config.enabled=false",
				"server.shutdown=graceful"
		}
)
public class ProcessorCoreFunctionalTests {

	@Autowired
	protected ProcessRepository repository;

	@Autowired
	private InputDestination input;

	@Autowired
	private OutputDestination output;

	@LocalServerPort
	private int port;

	protected void assertEmptyQueues() {

		assertThat(this.getOutput().receive(0, "process-pending"), nullValue());
		assertThat(this.getOutput().receive(0, "process-failed"), nullValue());
		assertThat(this.getOutput().receive(0, "process-processing"), nullValue());
		assertThat(this.getOutput().receive(0, "process-complete"), nullValue());
		assertThat(this.getOutput().receive(0, "process-cancelled"), nullValue());

	}

	@Test
	public void contextLoads() {

	}

	@DirtiesContext
	@Test
	public void complete() {

		final AtomicReference<String> id = new AtomicReference<>("");

		this.initialize(id, "TRUE");
		ArrayList<JpaRepositoryStateMachine> jpaRepositoryStateMachines = Lists.newArrayList(this.getRepository().findAll().iterator());
		assertThat(jpaRepositoryStateMachines, hasSize(1));

		this.read(id, PENDING);
		jpaRepositoryStateMachines = Lists.newArrayList(this.getRepository().findAll().iterator());
		assertThat(jpaRepositoryStateMachines, hasSize(1));

		this.process(id, "TRUE", false);
		jpaRepositoryStateMachines = Lists.newArrayList(this.getRepository().findAll().iterator());
		assertThat(jpaRepositoryStateMachines, hasSize(1));

		assertThat(new String(this.getOutput().receive(0, "process-complete").getPayload()), equalTo(id.get()));

		this.read(id, COMPLETE);

		this.assertEmptyQueues();

	}

	@DirtiesContext
	@Test
	public void failAndCancel() {

		final AtomicReference<String> id = this.failAfterProcessing();

		this.cancel(id);
		this.read(id, CANCELLED);

		this.assertEmptyQueues();

	}

	public AtomicReference<String> failAfterProcessing() {

		final AtomicReference<String> id = new AtomicReference<>("");

		this.initialize(id, "TRUE");
		ArrayList<JpaRepositoryStateMachine> jpaRepositoryStateMachines = Lists.newArrayList(this.getRepository().findAll().iterator());
		assertThat(jpaRepositoryStateMachines, hasSize(1));

		this.process(id, "FALSE", false);
		jpaRepositoryStateMachines = Lists.newArrayList(this.getRepository().findAll().iterator());
		assertThat(jpaRepositoryStateMachines, hasSize(1));

		assertThat(new String(this.getOutput().receive(0, "process-failed").getPayload()), equalTo(id.get()));
		this.read(id, FAILED);

		this.assertEmptyQueues();

		return id;

	}

	protected void cancel(AtomicReference<String> id) {

		log.trace("CANCEL");
		this.getWebClient()
		    .delete().uri(uriBuilder -> uriBuilder.path("/" + id.get()).build())
		    .exchange()
		    .expectStatus().isNoContent();

		assertThat(new String(this.getOutput().receive(0, "process-cancelled").getPayload()), equalTo(id.get()));

		this.assertEmptyQueues();

	}

	private WebTestClient getWebClient() {

		return WebTestClient.bindToServer()
		                    .baseUrl("http://localhost:" + this.getPort() + "/process")
		                    .filter(
				                    (request, next) -> {
					                    log.trace("\n\n[" + request.method() + "] " + request.url() + "\n" + request.headers() + "\n\n");
					                    return next.exchange(request);
				                    }
		                           )
		                    .responseTimeout(Duration.ofMinutes(1))
		                    .build();
	}

	protected void read(AtomicReference<String> id,
	                    ProcessState expectedState) {

		log.trace("READ");
		final ProcessState createdBody = this.getWebClient()
		                                     .get().uri(uriBuilder -> uriBuilder.path("/" + id.get()).build())
		                                     .accept(APPLICATION_JSON)
		                                     .exchange().expectStatus().isOk()
		                                     .expectBody(ProcessState.class)
		                                     .returnResult().getResponseBody();
		assertThat(createdBody, equalTo(expectedState));

	}

	@DirtiesContext
	@Test
	public void guardedFromCompleting() {

		this.failAfterProcessing();

	}

	protected void initialize(AtomicReference<String> id,
	                          String body) {

		log.trace("INITIALIZE");
		this.getWebClient()
		    .post()
		    .contentType(TEXT_PLAIN)
		    .body(Mono.just(body), String.class)
		    .exchange()
		    .expectStatus()
		    .isCreated()
		    .expectHeader().exists(HttpHeaders.LOCATION)
		    .expectHeader().value(HttpHeaders.LOCATION, id::set);
		id.getAndUpdate(str -> str.substring(str.lastIndexOf("/") + 1));

		assertThat(new String(this.getOutput().receive(0, "process-pending").getPayload()), equalTo(id.get()));
		this.assertEmptyQueues();

	}

	@DirtiesContext
	@Test
	public void guardedFromProcessing() {

		final AtomicReference<String> id = new AtomicReference<>("");

		this.initialize(id, "FALSE");
		ArrayList<JpaRepositoryStateMachine> jpaRepositoryStateMachines = Lists.newArrayList(this.getRepository().findAll().iterator());
		assertThat(jpaRepositoryStateMachines, hasSize(1));

		this.process(id, "ANY", true);
		jpaRepositoryStateMachines = Lists.newArrayList(this.getRepository().findAll().iterator());
		assertThat(jpaRepositoryStateMachines, hasSize(1));

		this.assertEmptyQueues();

	}

	@DirtiesContext
	@Test
	public void initialize() {

		final AtomicReference<String> id = new AtomicReference<>("");

		this.initialize(id, "TRUE");
		ArrayList<JpaRepositoryStateMachine> jpaRepositoryStateMachines = Lists.newArrayList(this.getRepository().findAll().iterator());
		assertThat(jpaRepositoryStateMachines, hasSize(1));

		this.read(id, PENDING);
		jpaRepositoryStateMachines = Lists.newArrayList(this.getRepository().findAll().iterator());
		assertThat(jpaRepositoryStateMachines, hasSize(1));

	}

	protected void process(AtomicReference<String> id,
	                       String body,
	                       boolean expectFailProcessingGuard) {

		log.trace("PROCESS");
		this.getWebClient()
		    .patch().uri(uriBuilder -> uriBuilder.path("/" + id.get()).build())
		    .body(Mono.just(body), String.class)
		    .exchange()
		    .expectStatus().isAccepted();

		if (expectFailProcessingGuard) {
			assertThat(this.getOutput().receive(0, "process-processing"), nullValue());
		}
		else {
			assertThat(new String(this.getOutput().receive(0, "process-processing").getPayload()), equalTo(id.get()));
		}

	}

	@DirtiesContext
//	@Test
	public void initializeAndCancel() {

		final AtomicReference<String> id = new AtomicReference<>("");

		this.initialize(id, "TRUE");
		ArrayList<JpaRepositoryStateMachine> jpaRepositoryStateMachines = Lists.newArrayList(this.getRepository().findAll().iterator());
		assertThat(jpaRepositoryStateMachines, hasSize(1));

		this.read(id, PENDING);
		jpaRepositoryStateMachines = Lists.newArrayList(this.getRepository().findAll().iterator());
		assertThat(jpaRepositoryStateMachines, hasSize(1));

		this.cancel(id);
		jpaRepositoryStateMachines = Lists.newArrayList(this.getRepository().findAll().iterator());
		assertThat(jpaRepositoryStateMachines, hasSize(1));

		this.read(id, CANCELLED);
		jpaRepositoryStateMachines = Lists.newArrayList(this.getRepository().findAll().iterator());
		assertThat(jpaRepositoryStateMachines, hasSize(1));

	}

	@DirtiesContext
	@Test
	public void retry() {

		final AtomicReference<String> id = this.failAfterProcessing();

		this.process(id, "TRUE", false);
		assertThat(new String(this.getOutput().receive(0, "process-complete").getPayload()), equalTo(id.get()));

		this.read(id, COMPLETE);

		this.assertEmptyQueues();

	}

}
