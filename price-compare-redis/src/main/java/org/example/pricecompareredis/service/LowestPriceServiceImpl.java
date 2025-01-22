package org.example.pricecompareredis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.example.pricecompareredis.vo.Keyword;
import org.example.pricecompareredis.vo.Product;
import org.example.pricecompareredis.vo.ProductGroup;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Primary
@Service
@RequiredArgsConstructor
public class LowestPriceServiceImpl implements LowestPriceService {

    private final RedisTemplate myProdPriceRedis;
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * 해당 key로부터 상품 10개 가지고 오기.
     */
    @Override
    public Set product(String key) {
        Set mySet = new HashSet();
        mySet = myProdPriceRedis.opsForZSet().rangeWithScores(key, 0, 9);
        return mySet;
    }

    @Override
    public int setNewProduct(Product product) {
        int rank = 0;

        myProdPriceRedis.opsForZSet().add(product.getProductGroupId(), product.getProductId(), rank);
        rank = myProdPriceRedis.opsForZSet().rank(product.getProductGroupId(), product.getProductId()).intValue();
        return rank;
    }

    /**
     * 새로운 상품 그룹 생성.
     */
    @Override
    public int setNewProductGroup(ProductGroup productGroup) {
        List<Product> products = productGroup.getProductList();

        String productId = products.get(0).getProductId();
        int price = products.get(0).getPrice();

        myProdPriceRedis.opsForZSet().add(productGroup.getProductGroupId(), productId, price);
        int productGroupCount = myProdPriceRedis.opsForZSet().zCard(productGroup.getProductGroupId()).intValue();

        return productGroupCount;
    }

    public int setNewProductGroupToKeyword(String productGroupId, String keyword, double score) {
        myProdPriceRedis.opsForZSet().add(keyword, productGroupId, score);
        int rank = myProdPriceRedis.opsForZSet().rank(keyword, productGroupId).intValue();

        return rank;
    }

    @Override
    public Keyword getLowestPriceProductByKeyword(String keyword) {
        Keyword returnInfo = new Keyword();

        // keyword를 통해 ProductGroup가져오기(10개)
        List<ProductGroup> tempProductGroup = getProductGroupByKeyword(keyword);

        // 가져온 정보들을 Return할 Object에 넣기
        returnInfo.setKeyword(keyword);
        returnInfo.setProductGroupList(tempProductGroup);

        // 해당 Object return
        return returnInfo;
    }

    private List<ProductGroup> getProductGroupByKeyword(String keyword) {
        List<ProductGroup> returnProductGroupList = new ArrayList<>();
        ProductGroup tempProductGroup = new ProductGroup();

        // Input 받은 keyword로 productGroupId 조회
        // Set을 List형태로 변경
        List<String> productGroupIdList = List.copyOf(myProdPriceRedis.opsForZSet().reverseRange(keyword, 0, 9));

        Set productAndPriceList;


        // loop타면서 ProductGroup으로 Product:price 가져오기(10개)
        for(final String productGroupId : productGroupIdList) {

            List<Product> tempProductList = new ArrayList<>();

            productAndPriceList = myProdPriceRedis.opsForZSet().rangeWithScores(productGroupId, 0, 9);

            Iterator<Object> productPriceObjects = productAndPriceList.iterator();

            while(productPriceObjects.hasNext()){

                // {"value" : 00-10111-}, {"score" : 11000}
                Map<String, String> productPriceMap =  mapper.convertValue(productPriceObjects.next(), Map.class);


                Product tempProduct = new Product();
                // Product Object bind하기.
                tempProduct.setProductId(productPriceMap.get("value")); // productId (productPriceMap Map에서의 key값.)
                tempProduct.setPrice(Double.valueOf(productPriceMap.get("score")).intValue()); // score
                tempProduct.setProductGroupId(productGroupId);

                tempProductList.add(tempProduct);
            }

            // 10개  product price 입력 완료
            tempProductGroup.setProductGroupId(productGroupId);
            tempProductGroup.setProductList(tempProductList);
            returnProductGroupList.add(tempProductGroup);

        }

        return returnProductGroupList;
    }


}
