package ssm.demo.core.processor.dto.process;


import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ssm.demo.core.processor.dto.Entity;

@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ProcessWrapper<PAYLOAD> implements Entity {

	@JsonAlias({"id", "processId"})
	private String id;

	private PAYLOAD payload;

}
