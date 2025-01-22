package org.example.pricecompareredis.controller;


import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.example.pricecompareredis.service.LowestPriceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class LowestPriceController {

    private final LowestPriceService lowestPriceService;


    @GetMapping("/getZSETValue")
    public Set getZSetValue(String key){
        return lowestPriceService.getZsetValue(key);
    }

}
