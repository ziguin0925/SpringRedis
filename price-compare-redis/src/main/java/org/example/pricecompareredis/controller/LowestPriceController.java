package org.example.pricecompareredis.controller;


import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.example.pricecompareredis.service.LowestPriceService;
import org.example.pricecompareredis.vo.Keyword;
import org.example.pricecompareredis.vo.Product;
import org.example.pricecompareredis.vo.ProductGroup;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class LowestPriceController {

    private final LowestPriceService lowestPriceService;


    @GetMapping("/getZSETValue")
    public Set getZSetValue(String key){
        return lowestPriceService.product(key);
    }

    @PutMapping("/product")
    public int setNewProduct(@RequestBody Product product){
        return lowestPriceService.setNewProduct(product);
    }

    /**
     * 새로운 상품 그룹 생성.
     * */
    @PutMapping("/productGroup")
    public int setNewProduct(@RequestBody ProductGroup productGroup){
        return lowestPriceService.setNewProductGroup(productGroup);
    }

    /**
     * 상품 그룹에 키워드 입력
     * */
    @PutMapping("/productGroupToKeyword")
    public int setNewProductGroupToKeyword(String keyword, String productGroupId, double score){
        return lowestPriceService.setNewProductGroupToKeyword(keyword, productGroupId, score);
    }


    /**
     * 해당 키워드의 최저가 상품 10개 반환
     * */
    @GetMapping("/productPrice/lowest")
    public Keyword getLowestPriceProductByKeyword(String keyword){
        return lowestPriceService.getLowestPriceProductByKeyword(keyword);
    }

}
