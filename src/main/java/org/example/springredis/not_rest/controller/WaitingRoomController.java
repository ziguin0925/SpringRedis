package org.example.springredis.not_rest.controller;


import lombok.RequiredArgsConstructor;
import org.example.springredis.rest.service.UserQueueService;
import org.springframework.http.HttpCookie;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class WaitingRoomController {

    private final UserQueueService userQueueService;

    private String tokenKey = "user-queue-%s-token";

    /**
     * http://localhost:9000/waiting-room?user_id=1&redirect_url=https://www.naver.com
     * */
    @GetMapping("/waiting-room")
    public Mono<Rendering> waitingRoomPage(@RequestParam(name = "queue",defaultValue = "default") String queue,
                                           @RequestParam(name = "user_id") Long userId,
                                           @RequestParam(name = "redirect_url") String redirectUrl,
                                           ServerWebExchange exchange){


        HttpCookie cookieValue = exchange.getRequest().getCookies().getFirst(tokenKey.formatted(queue));

        var token = (cookieValue != null) ? cookieValue.getValue() : null;

        return userQueueService.isAllowedByToken(queue, userId, token)// 입장 가능한지 확인
                .filter(allowed -> allowed)// 대기열에서 입장이 허용되면 true
                .flatMap(allowed->Mono.just(Rendering.redirectTo(redirectUrl).build())) // redirect시킬 주소
                .switchIfEmpty(userQueueService.registerWaitQueue(queue, userId) // 입장이 허용되지 않은 상태이면
                        .onErrorResume(e -> userQueueService.getRank(queue, userId)) // waitqueue에 등록이 되어있는 상태라면
                        .map(rank -> Rendering.view("waiting-room.html")
                                .modelAttribute("number", rank)
                                .modelAttribute("userId", userId)
                                .modelAttribute("queue", queue)
                                .build()
                        )
                );
    }
}
