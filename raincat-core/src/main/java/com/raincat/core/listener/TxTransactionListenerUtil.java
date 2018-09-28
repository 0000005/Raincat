package com.raincat.core.listener;

import com.raincat.core.concurrent.threadlocal.TxTransactionLocal;
import com.raincat.core.concurrent.threadpool.TxTransactionThreadPool;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TxTransactionListenerUtil {

    @Autowired
    private TxTransactionThreadPool txTransactionThreadPool;

    private static final Logger LOGGER = LoggerFactory.getLogger(TxTransactionListenerUtil.class);
    /**
     * 设置回调事件
     * @param listener
     * @return  返回true表示设置成功，false表示设置失败
     */
    public boolean setListener(TxTransactionListener listener){
        String groupId=TxTransactionLocal.getInstance().getTxGroupId();
        if(StringUtils.isNotBlank(groupId))
        {
            List<TxTransactionListener> listenerList= TxTransactionCache.getInstance()
                    .getCache().getIfPresent(groupId+TxTransactionListener.CALLBACK_KEY);
            if(listenerList==null)
            {
                listenerList=new ArrayList<>();
            }
            listenerList.add(listener);
            TxTransactionCache.getInstance()
                    .getCache().put(groupId+TxTransactionListener.CALLBACK_KEY,listenerList);
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * 执行回调事件
     * @param txGroupId
     * @return
     */
    public void runCallback(String txGroupId){
        List<TxTransactionListener> listenerList= TxTransactionCache.getInstance().getCache()
                                        .getIfPresent(txGroupId+TxTransactionListener.CALLBACK_KEY);
        if(listenerList!=null) {
            txTransactionThreadPool.multiScheduled(() -> {
                for(TxTransactionListener listener : listenerList)
                {
                    try{
                        listener.afterCommit();
                    }
                    catch (Exception e) {
                        LOGGER.error("分布式事务执行提交回调失败。txGroupId:{},exception:{}",txGroupId,e);
                    }
                }
                return true;
            },3);
        }
    }

    /**
     * 删除回调事件
     * @param txGroupId
     * @return
     */
    public void deleteListenerGroup(String txGroupId){
        if(StringUtils.isNotBlank(txGroupId))
        {
            TxTransactionCache.getInstance().getCache()
                    .invalidate(txGroupId+TxTransactionListener.CALLBACK_KEY);
        }
    }
}
