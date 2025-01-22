package org.example.pricecompareredis.vo;

import java.util.List;
import lombok.Data;

@Data
public class Keyword {
    private String keyword; // 유아용품 - 하기스 귀저기
    private List<ProductGroup> productGroupList; // [{"FPG0001", productList}, {"FPG0002", productList}]

}
