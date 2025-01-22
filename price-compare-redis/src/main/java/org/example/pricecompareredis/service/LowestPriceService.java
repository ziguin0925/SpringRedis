package org.example.pricecompareredis.service;

import java.util.Set;
import org.example.pricecompareredis.vo.Keyword;
import org.example.pricecompareredis.vo.Product;
import org.example.pricecompareredis.vo.ProductGroup;

public interface LowestPriceService {

    Set product(String key);

    int setNewProduct(Product product);

    int setNewProductGroup(ProductGroup productGroup);

    int setNewProductGroupToKeyword(String productGroupId, String keyword, double score);

    Keyword getLowestPriceProductByKeyword(String keyword);

}
