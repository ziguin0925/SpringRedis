package org.example.couponconsumer.component;

import static org.example.couponcore.util.CouponRedisUtils.getIssueRequestQueueKey;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.couponcore.repository.redis.RedisRepository;
import org.example.couponcore.repository.redis.dto.CouponIssueRequest;
import org.example.couponcore.service.CouponIssueService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@EnableScheduling
@Component
@Slf4j
public class CouponIssueListener {

    private final RedisRepository redisRepository;
    private final CouponIssueService couponIssueService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String issueRequestQueueKey = getIssueRequestQueueKey();

    /**
     * 쿠폰 발급 처리
     * fixedRate 로 설정하면 ms는 큰 차이 없이 균일하게
     * */
    @Scheduled(fixedRate = 1000)
    public void issue() throws JsonProcessingException {
      log.info("listen...");
      while(existCouponIssueTarget()){
          CouponIssueRequest target = getIssueTarget();
          log.info("발급 시작 : %s".formatted(target));
          couponIssueService.issue(target.couponId(), target.userId());
          log.info("발급 완료 target : %s".formatted(target));

          removeCouponIssueTarget();
      }
    }

    //해당 대기열 queue에 유저가 있는지.
    private boolean existCouponIssueTarget(){
        return redisRepository.lSize(issueRequestQueueKey) > 0;
    }

    // redis로부터 대기열 queue의 첫번째 인덱스 유저를 가지고 오기.
    private CouponIssueRequest getIssueTarget() throws JsonProcessingException {
        return objectMapper.readValue(redisRepository.lIndex(issueRequestQueueKey, 0), CouponIssueRequest.class);
    }

    private void removeCouponIssueTarget(){
        redisRepository.lPop(issueRequestQueueKey);
    }
}
