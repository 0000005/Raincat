package com.raincat.core.listener;

/**
 * 监听器，此处可以监听分布式事务的声明周期，并进行回调
 */
public interface TxTransactionListener {
    /**
     * 回调的key
     */
    String CALLBACK_KEY = "_TX_CALLBACK";

    /**
     * 提交之3秒之后被调用（要确保所有参与者都提交了）
     */
    void afterCommit();

}
