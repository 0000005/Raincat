package com.raincat.core.listener;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.raincat.core.concurrent.threadlocal.TxTransactionLocal;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class TxTransactionCache
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TxTransactionCache.class);
    private static final TxTransactionCache TX_TRANSACTION_CACHE = new TxTransactionCache();

    /**
     * 用于存取回调
     */
    private static final Cache<String,TxTransactionListener> cache = CacheBuilder
                                        .newBuilder()
                                        .expireAfterWrite(5,TimeUnit.MINUTES)
                                        .build();
    private TxTransactionCache(){}

    public static TxTransactionCache getInstance() {
        return TX_TRANSACTION_CACHE;
    }

    public Cache getCache() {
        return cache;
    }

    /**
     * 设置回调事件
     * @param listener
     * @return  返回true表示设置成功，false表示设置失败
     */
    public boolean setListener(TxTransactionListener listener){
        String groupId=TxTransactionLocal.getInstance().getTxGroupId();
        if(StringUtils.isNotBlank(groupId))
        {
            cache.put(groupId+TxTransactionListener.CALLBACK_KEY,listener);
            return true;
        }
        else
        {
            LOGGER.warn("设置回调失败！");
            return false;
        }
    }

    /**
     * 执行回调事件
     * @param txGroupId
     * @return
     */
    public void runCallback(String txGroupId){
        TxTransactionListener listener= cache.getIfPresent(txGroupId+TxTransactionListener.CALLBACK_KEY);
        if(listener!=null) {
            try{
                listener.afterCommit();
            }
            catch (Exception e) {
                LOGGER.error("分布式事务执行提交回调失败。txGroupId:{},exception:{}",txGroupId,e);
            }
        }
    }

    /**
     * 删除回调事件
     * @param txGroupId
     * @return
     */
    public void deleteListener(String txGroupId){
        if(StringUtils.isNotBlank(txGroupId))
        {
            cache.invalidate(txGroupId+TxTransactionListener.CALLBACK_KEY);
        }
    }
}
