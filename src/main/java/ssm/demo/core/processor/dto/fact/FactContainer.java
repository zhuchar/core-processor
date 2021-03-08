package ssm.demo.core.processor.dto.fact;

import java.util.Map;

public interface FactContainer {

	Map<String, String> getFacts();

	void setFacts(Map<String, String> facts);

}
