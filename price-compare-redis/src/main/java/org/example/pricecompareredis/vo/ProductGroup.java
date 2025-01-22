package org.example.pricecompareredis.vo;

import java.util.List;
import lombok.Data;

@Data
public class ProductGroup {
    private String productGroupId; // FPG0001
    private List<Product> productList; // [{d1fc10-..., 25000}, {}, ...]
}
