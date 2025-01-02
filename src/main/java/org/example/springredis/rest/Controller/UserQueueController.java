package org.example.springredis.rest.Controller;


import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.example.springredis.rest.dto.AllowUserResponse;
import org.example.springredis.rest.dto.AllowedUserResponse;
import org.example.springredis.rest.dto.RankNumberResponse;
import org.example.springredis.rest.dto.RegisterUserResponse;
import org.example.springredis.rest.service.UserQueueService;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/queue")
@RequiredArgsConstructor
public class UserQueueController {

    private final UserQueueService userQueueService;

    /**
     *  대기열 등록 wait queue(Redis SortedSet)에 추가.
     * */
    @PostMapping()
    public Mono<RegisterUserResponse> registerUser(@RequestParam(name="queue", defaultValue = "default") String queue,
                                                   @RequestParam(name="user_id") Long userId){
        return userQueueService.registerWaitQueue(queue, userId)
                .map(RegisterUserResponse::new);
    }


    /**
     * 진입을 허용(wait queue에서 proceed queue로 옮긴다.)
     *
     * @return : proceed queue에 진입이 되는 멤버 숫자
     * */
    @PostMapping("/allow")
    public Mono<AllowUserResponse> allowUser(@RequestParam(name="queue", defaultValue = "default") String queue,
                                             @RequestParam(name="count") Long count){


        return userQueueService.allowUser(queue, count)
                .map(allowed -> new AllowUserResponse(count, allowed));

    }


    /**
     * 진입이 가능한 상태인지 조회
     * (proceed queue에 있는 사용자들을 웹페이지에 접근 가능하도록 함.)
     * */
    @GetMapping("/allowed")
    public Mono<AllowedUserResponse> isAllowedUser(@RequestParam(name = "queue", defaultValue = "default") String queue,
                                                   @RequestParam(name = "user_id") Long userId,
                                                   @RequestParam(name = "token") String token){

        // 쿠키는 같은 도메인에서만 작동함(port는 제외)
        return userQueueService.isAllowedByToken(queue, userId, token)
                .map(AllowedUserResponse::new);

    }

    @GetMapping("/rank")
    public Mono<RankNumberResponse> getRankUser(@RequestParam(name ="queue", defaultValue = "default") String queue,
                                                @RequestParam(name = "user_id") Long userId){

        return userQueueService.getRank(queue, userId)
                .map(RankNumberResponse::new);

    }

    @GetMapping("/touch")
    Mono<String> touch(@RequestParam(name ="queue", defaultValue = "default") String queue,
                       @RequestParam(name = "user_id") Long userId,
                       ServerWebExchange exchange)  {
        return Mono.defer(()->userQueueService.generateToken(queue, userId))
                .map(token -> {
                    exchange.getResponse().addCookie(
                            ResponseCookie.from("user-queue-%s-token".formatted(queue), token)
                                    .maxAge(Duration.ofSeconds(300)) // 5분
                                    .path("/") // 모든 도메인에 대해
                                    .build()
                    );

                    return token;
                });
    }


}
