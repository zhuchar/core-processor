package ssm.demo.core.processor.service;

import lombok.Getter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.service.DefaultStateMachineService;

@Getter
public class ClusterStateMachineService<S, E> extends DefaultStateMachineService<S, E> {

	@Value("${spring.application.name}")
	private String appName;

	private RedissonClient redisson;

	public ClusterStateMachineService(StateMachineFactory stateMachineFactory,
                                      StateMachinePersist stateMachinePersist,
                                      RedissonClient redisson) {

		super(stateMachineFactory, stateMachinePersist);
		this.redisson = redisson;
	}

	public void lock(String machineId, String desc) {
		RedisLockService.lock(this.getRedisson(), RedisLockService.getLockId(appName, machineId), desc);
	}
	public void unlock(String machineId, String desc) {
		RedisLockService.unlock(this.getRedisson(), RedisLockService.getLockId(appName, machineId), desc);
	}
}
