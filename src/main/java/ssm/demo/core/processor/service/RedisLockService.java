package ssm.demo.core.processor.service;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;


@Slf4j
public class RedisLockService {

	public static void lock(RedissonClient redisson, String machineId, String desc) {

		long threadId = Thread.currentThread().getId();

		log.debug("==== locking {} @{} {}", machineId, threadId, desc);

		RLock lock = redisson.getLock(machineId);
		lock.lock();

		log.debug("==== locked {} @{} {}", machineId, threadId, desc);
	}

	public static void unlock(RedissonClient redisson, String machineId, String desc) {

		long threadId = Thread.currentThread().getId();

		log.debug("==== unlocking {} @{} {}", machineId, threadId, desc);

		RLock lock = redisson.getLock(machineId);
		lock.unlock();

		log.debug("==== unlocked {} @{} {}", machineId, threadId, desc);
	}

	public static String getLockId(String appName, String machineId) {
		return appName + "_" + machineId;
	}
}
