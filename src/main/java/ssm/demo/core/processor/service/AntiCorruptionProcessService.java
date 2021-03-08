package ssm.demo.core.processor.service;

import org.springframework.core.convert.ConversionService;

import javax.validation.ConstraintViolation;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface AntiCorruptionProcessService<INITIAL_DATA_TYPE, EXTERNAL_DTO_REQUEST_TYPE> extends ProcessService {

	default Optional<Set<String>> validateInitialData(INITIAL_DATA_TYPE initialData) {

		final Optional<EXTERNAL_DTO_REQUEST_TYPE> optionalExternalDto = Optional.ofNullable(this.getConversionService().convert(initialData, this.getExternalDtoRequestType()));

		if (optionalExternalDto.isEmpty()) {
			return Optional.empty();
		}

		return Optional.of(
				this.validateExternalDto(optionalExternalDto.get())
				    .stream()
				    .map(ConstraintViolation::getMessageTemplate)
				    .collect(Collectors.toSet())
		                  );
	}

	ConversionService getConversionService();

	Class<EXTERNAL_DTO_REQUEST_TYPE> getExternalDtoRequestType();

	Set<ConstraintViolation<EXTERNAL_DTO_REQUEST_TYPE>> validateExternalDto(EXTERNAL_DTO_REQUEST_TYPE requestDto);

}
