package org.example.couponapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    /**
     * locust 테스트용 컨트롤러
     * */
    @GetMapping("/hello")
    public String hello() {
        return "hello-locust";
    }
}
