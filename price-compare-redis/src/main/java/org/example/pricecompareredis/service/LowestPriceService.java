package org.example.pricecompareredis.service;

import java.util.Set;

public interface LowestPriceService {

    Set getZsetValue(String key);
}
