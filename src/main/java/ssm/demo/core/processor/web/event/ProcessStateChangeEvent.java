package ssm.demo.core.processor.web.event;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;
import ssm.demo.core.processor.dto.process.ProcessState;


@Setter
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class ProcessStateChangeEvent extends ApplicationEvent {

	private ProcessState state;

	public ProcessStateChangeEvent(String id,
	                               ProcessState state) {

		super(id);
		this.state = state;

	}

}
