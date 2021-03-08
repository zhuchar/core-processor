package ssm.demo.core.processor.engine;

import ssm.demo.core.processor.dto.process.ProcessEvent;
import ssm.demo.core.processor.dto.process.ProcessState;
import ssm.demo.core.processor.service.AntiCorruptionProcessService;
import org.springframework.core.convert.ConversionService;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.guard.Guard;

import javax.validation.ConstraintViolation;
import javax.validation.ValidationException;
import java.util.Optional;
import java.util.Set;

public interface AntiCorruptionProcessEngine<INITIAL_DATA_TYPE, EXTERNAL_DTO_REQUEST_TYPE, EXTERNAL_DTO_RESPONSE_TYPE> extends ProcessEngine {

	String EXTERNAL_REQUEST_DTO_KEY = "externalRequestDto";

	String EXTERNAL_RESPONSE_DTO_KEY = "externalResponseDto";

	@Override
	default Guard<ProcessState, ProcessEvent> getCompleteGuard() {

		this.getLogger().trace("Checking to see if process was successful or failed");
		return context -> context.getExtendedState().getVariables().containsKey(EXTERNAL_RESPONSE_DTO_KEY);

	}

	@Override
	default Action<ProcessState, ProcessEvent> getInitializeAction() {

		return context -> {
			this.getLogger().trace("Initializing anti-corruption process, converting to external DTO");
			final Optional<EXTERNAL_DTO_REQUEST_TYPE>
					externalDto =
					Optional.ofNullable(this.getConversionService().convert(context.getExtendedState().get(INITIAL_DATA_KEY, this.getInitialDataType()), this.getService().getExternalDtoRequestType()));
			if (externalDto.isPresent()) {
				this.getLogger().trace("Conversion to external DTO is successful");
				this.getLogger().trace(externalDto.get().toString());
				this.getLogger().trace("Persisting external DTO");
				context.getExtendedState().getVariables().put(EXTERNAL_REQUEST_DTO_KEY, externalDto.get());
				this.getLogger().trace("Automatically attempting to process the external DTO");
				this.getService().process(context.getStateMachine(), null);
			}
			else {
				this.getLogger().warn("Conversion to external DTO failed, canceling process");
				this.getService().cancel(context.getStateMachine());
			}

		};
	}

	@Override
	default Action<ProcessState, ProcessEvent> getProcessAction() {

		return context -> {
			try {
				this.getLogger().trace("Sending external DTO to integration point");
				this.send(context.getExtendedState().get(EXTERNAL_REQUEST_DTO_KEY, this.getService().getExternalDtoRequestType()))
				    .ifPresent(responseDto -> {
					    this.getLogger().trace("Marcus agrees that the external DTO has been successfully sent");
					    context.getExtendedState().getVariables().put(EXTERNAL_RESPONSE_DTO_KEY, responseDto);
				    });
			} catch (Throwable throwable) {
				this.getLogger().error("Error when sending to integration point", throwable);
				throw throwable;
			}
		};
	}

	@Override
	default Guard<ProcessState, ProcessEvent> getProcessGuard() {

		return context -> {
			this.getLogger().trace("Checking process is initialized correctly");
			final EXTERNAL_DTO_REQUEST_TYPE requestDto = context.getExtendedState().get(EXTERNAL_REQUEST_DTO_KEY, this.getService().getExternalDtoRequestType());
			if (null == requestDto) {
				this.getLogger().trace("Request DTO is null");
				return false;
			}
			this.getLogger().trace("Running validation checks on the external DTO");
			final Set<ConstraintViolation<EXTERNAL_DTO_REQUEST_TYPE>> validate = this.getService().validateExternalDto(requestDto);
			if (validate.isEmpty()) {
				this.getLogger().trace("Validation checks on the external DTO not shown validation issues on the external DTO");
				return true;
			}
			this.getLogger().trace("Validation checks on the external DTO have generated violations");
			context.getStateMachine().setStateMachineError(new ValidationException(validate.toString()));
			this.getLogger().error(validate.toString());
			return false;

		};
	}

	Optional<EXTERNAL_DTO_RESPONSE_TYPE> send(EXTERNAL_DTO_REQUEST_TYPE request);

	ConversionService getConversionService();

	Class<INITIAL_DATA_TYPE> getInitialDataType();

	AntiCorruptionProcessService<INITIAL_DATA_TYPE, EXTERNAL_DTO_REQUEST_TYPE> getService();

}
