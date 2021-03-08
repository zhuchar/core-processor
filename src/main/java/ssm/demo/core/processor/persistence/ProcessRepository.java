package ssm.demo.core.processor.persistence;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.statemachine.data.jpa.JpaRepositoryStateMachine;
import org.springframework.statemachine.data.jpa.JpaStateMachineRepository;

@JaversSpringDataAuditable
public interface ProcessRepository extends JpaStateMachineRepository, PagingAndSortingRepository<JpaRepositoryStateMachine, String> {

}
