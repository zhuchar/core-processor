package ssm.demo.core.processor.web.rest;

import ssm.demo.core.processor.configuration.ProcessorConfiguration;
import ssm.demo.core.processor.dto.process.ProcessState;
import ssm.demo.core.processor.service.ProcessService;
import org.slf4j.Logger;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.server.ResponseStatusException;
import reactor.util.annotation.Nullable;

import java.util.Set;

import static org.springframework.hateoas.MediaTypes.HAL_FORMS_JSON_VALUE;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequestMapping("/process")
public interface ProcessResource<INITIALIZE, PROCESS> {

	/**
	 * Redirects a process to the cancelled state
	 *
	 * @param id the Id of the process being cancelled
	 * @return 204 No Content
	 */
	@DeleteMapping(path = "/{id}")
	default ResponseEntity<Void> cancel(@PathVariable String id) {

		if (this.getService().cancel(id)) {
			return ResponseEntity.noContent().build();
		}

		throw new ResponseStatusException(HttpStatus.NOT_FOUND);

	}

	/**
	 * Creates the restful information for a process to use in HAL or HAL FORMS
	 *
	 * @param id the Id of the process
	 * @return the HAL/HAL FORMS links and affordances
	 */
	@SuppressWarnings("ConstantConditions")
	default Link getLink(@Nullable String id) {

		return linkTo(this.getMethodOn().getProcessStateEntityModel(id))
				.withSelfRel()
				.andAffordance(afford(this.getMethodOn().initialize(null)))
				.andAffordance(afford(this.getMethodOn().process(id, null)))
				.andAffordance(afford(this.getMethodOn().cancel(id)))
				.andAffordance(afford(this.getMethodOn().getVariableNames(id)))
				.andAffordance(afford(this.getMethodOn().getVariable(id, null)));

	}

	default Links getLinks(@Nullable String id) {

		return Links.of(this.getLink(id));

	}

	Logger getLogger();

	@SuppressWarnings("unchecked")
	default ProcessResource<INITIALIZE, PROCESS> getMethodOn() {

		return methodOn(this.getClass());

	}

	/**
	 * Displays the current state of a running process
	 *
	 * @param id the identifier of the process
	 * @return the state which the process is in and does not contain RESTful information
	 */
	@GetMapping(
			path = "/{id}",
			produces = {APPLICATION_JSON_VALUE}
	)
	default ProcessState getProcessState(@PathVariable String id) {

		return this.getService().read(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

	}

	/**
	 * Displays the current state of a running process
	 *
	 * @param id the identifier of the process
	 * @return the state which the process is in and contains RESTful information
	 */
	@GetMapping(
			path = "/{id}",
			produces = {HAL_JSON_VALUE, HAL_FORMS_JSON_VALUE}
	)
	default EntityModel<ProcessState> getProcessStateEntityModel(@PathVariable String id) {

		return EntityModel.of(this.getProcessState(id), this.getLinks(id));

	}

	ProcessorConfiguration getProcessorConfiguration();

	ProcessService getService();

	/**
	 * Initializes a process given an initial payload
	 *
	 * @param initialData the initial payload required to start a specific process
	 * @return the Id of the newly created process within the location header
	 */
	@PostMapping
	default ResponseEntity<Void> initialize(@RequestBody(required = false) INITIALIZE initialData) {

		return ResponseEntity.created(linkTo(this.getMethodOn().processExists(this.getService().initialize(initialData))).toUri()).build();

	}

	@GetMapping(
			path = "/{id}/variable",
			produces = {APPLICATION_JSON_VALUE}
	)
	default Set<String> getVariableNames(@PathVariable String id) {

		final Set<String> variableNames = this.getService().getVariableNames(id);
		if (variableNames.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		return variableNames;

	}

	@GetMapping(
			path = "/{id}/variable/{variableName}"
	)
	default Object getVariable(@PathVariable String id,
	                           @PathVariable String variableName) {

		return this.getService().getVariable(id, variableName).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

	}

	@GetMapping(
			path = "/{id}/variable",
			produces = {HAL_JSON_VALUE, HAL_FORMS_JSON_VALUE}
	)
	default CollectionModel<String> getVariableNamesCollectionModel(@PathVariable String id) {

		return CollectionModel.of(
				this.getVariableNames(id),
				linkTo(this.getMethodOn().getVariableNames(id))
						.withSelfRel()
						.andAffordance(afford(this.getMethodOn().getVariableNamesCollectionModel(id)))
		                         );

	}

	/**
	 * Initiates a transition to the process state.
	 * The state being a choice state which will divide successes and failures.
	 *
	 * @param id      the Id of the process being triggered
	 * @param payload the required payload needed for the processing to occur
	 * @return 202 Accepted
	 */
	@PatchMapping(path = "/{id}", consumes = ALL_VALUE)
	default ResponseEntity<Void> process(@PathVariable String id,
	                                     @RequestBody(required = false) PROCESS payload) {

		this.getService().process(id, payload).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		return ResponseEntity.accepted().build();

	}

	/**
	 * Low cost check to see if a specific process exists
	 *
	 * @param id is the Id of the process being checked
	 * @return 200 if found and 404 if not
	 */
	@RequestMapping(method = RequestMethod.HEAD, path = "/{id}")
	default ResponseEntity<Void> processExists(@PathVariable String id) {

		if (this.getService().processExists(id)) {
			return ResponseEntity.ok().build();
		}

		return ResponseEntity.notFound().build();
	}


}
