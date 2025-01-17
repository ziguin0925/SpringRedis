package org.example.couponcore.repository.redis;

import static org.example.couponcore.exception.ErrorCode.FAIL_COUPON_ISSUE_REQUEST;
import static org.example.couponcore.util.CouponRedisUtils.getIssueRequestKey;
import static org.example.couponcore.util.CouponRedisUtils.getIssueRequestQueueKey;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.couponcore.exception.CouponIssueException;
import org.example.couponcore.repository.redis.dto.CouponIssueRequest;
import org.example.couponcore.repository.redis.dto.CouponIssueRequestCode;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisRepository {

    private final RedisTemplate<String, String> redisTemplate;

    private final RedisScript<String> issueScript =issueRequestScript();
    private final String issueRequestQueueKey = getIssueRequestQueueKey();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Boolean zAdd(final String key, final String value, double score) {
        // ZADD NX - sorted set에 해당 key가 없는 경우만 요청이 감.
        return redisTemplate.opsForZSet().addIfAbsent(key, value, score);
    }

    public Long sAdd(final String key, final String value) {
        return redisTemplate.opsForSet().add(key, value);
    }

    // set의 size
    public Long sCard(final String key) {
        return redisTemplate.opsForSet().size(key);
    }

    // 해당 set에 존재하는지
    public Boolean sIsMember(final String key, final String value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }


    public Long rPush(final String key, final String value){
        return redisTemplate.opsForList().rightPush(key, value);
    }

    public Long lSize(String key){
        return redisTemplate.opsForList().size(key);
    }

    public String lPop(String key){
        return redisTemplate.opsForList().leftPop(key);
    }


    public String lIndex(String key, int index){
        return redisTemplate.opsForList().index(key, index);
    }

    public void issueRequest(long couponId, long userId, int totalIssueQuantity){
        String issueRequestKey = getIssueRequestKey(couponId);
        CouponIssueRequest couponIssueRequest = new CouponIssueRequest(couponId, userId);

        try{
            String code = redisTemplate.execute(
                    issueScript,
                    List.of(issueRequestKey, issueRequestQueueKey), // script의 KEYS[] 로 넘겨짐
                    String.valueOf(userId), // ARGV[1]
                    String.valueOf(totalIssueQuantity), // ARGV[2]
                    objectMapper.writeValueAsString(couponIssueRequest) // ARGV[3]
            );

            // Redis에서 Script가 진행된 후 반환된 값을 enum으로 변경한 후에 검증
            CouponIssueRequestCode.checkRequestResult(CouponIssueRequestCode.find(code));

        }catch (JsonProcessingException e){
            throw new CouponIssueException(FAIL_COUPON_ISSUE_REQUEST, "input : %s".formatted(couponIssueRequest));
        }
    }


    private RedisScript<String> issueRequestScript(){


        /*
         * 1. 중복 발급 요청 제어
         * 2. 쿠폰 발급 수량 검증
         * 3. 쿠폰 발급 요청 저장(발급 수량 제어)
         * 4. 쿠폰 발급 큐에 적재
         * */


        /*
        * KEYS[n] : redis의 key
        * ARGV[n] : redis에게 주는 데이터
        *
        * if redis.call('SISMEMBER', KEYS[1], ARGV[1]) == 1 // 1인경우 이미 set안에 존재함. 존재하면 '2' 를 return함
        *
        * tonumber(ARGV[2]) > redis.call('SCARD', KEYS[1]) // 쿠폰 총 발급 수량(totalQuantity)과 set안에 존재하는 유저 수를 비교
        * 발급 수량이 남아있다면 아래의 명령어들을 수행.
        *
        * redis.call('SADD', KEYS[1], ARGV[1]) // 해당 set에 ARGV[1]을 저장한다.
        * redis.call('RPUSH', KEYS[2], ARGV[3]) // 해당 queue에 ARGV[3]를 저장한다.
        *
        * return으로 숫자를 받아서 해당 숫자에 맞는 처리를 하도록 하기
        *
        * */

        String script = """
                if redis.call('SISMEMBER', KEYS[1], ARGV[1]) == 1 then
                    return '2'
                end
                
                if tonumber(ARGV[2]) > redis.call('SCARD', KEYS[1]) then
                    redis.call('SADD', KEYS[1], ARGV[1])
                    redis.call('RPUSH', KEYS[2], ARGV[3])
                    return '1'
                end
                
                return '3'
                """;
        return RedisScript.of(script, String.class);
    }
}
