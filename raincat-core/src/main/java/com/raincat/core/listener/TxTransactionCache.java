package com.raincat.core.listener;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class TxTransactionCache
{
    private static final TxTransactionCache TX_TRANSACTION_CACHE = new TxTransactionCache();

    /**
     * 用于存取回调
     */
    private static final Cache<String,List<TxTransactionListener>> cache = CacheBuilder
                                        .newBuilder()
                                        .expireAfterWrite(5,TimeUnit.MINUTES)
                                        .build();
    private TxTransactionCache(){}

    public static TxTransactionCache getInstance() {
        return TX_TRANSACTION_CACHE;
    }

    public Cache<String,List<TxTransactionListener>> getCache() {
        return cache;
    }

}
