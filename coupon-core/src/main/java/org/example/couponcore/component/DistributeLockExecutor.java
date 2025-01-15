package org.example.couponcore.component;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class DistributeLockExecutor {
    private final RedissonClient redissonClient;

    public void exceute(String lockName, long waitMilliSecond, long leaseMilliSecond, Runnable logic) {
        RLock lock = redissonClient.getLock(lockName);
        try {
            boolean isLocked = lock.tryLock(waitMilliSecond, leaseMilliSecond, TimeUnit.MILLISECONDS);

            // lock획득에 실패하면 예외처리
            // 너무 많은 동시성 요청이 오면 lock획득 실패 예외가 던져짐.
            if (!isLocked) {
                throw new IllegalArgumentException("[" + lockName + "] lock 획득 실패");
            }

            // lock을 획득하면 로직 실행
            logic.run();

        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);

        } finally {
            // 로직 수행이 끝나거나 예외를 반환하더라도 unlock하도록
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
