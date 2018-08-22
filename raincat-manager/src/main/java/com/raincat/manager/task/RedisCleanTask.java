/*
 *
 * Copyright 2017-2018 549477611@qq.com(xiaoyu)
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.raincat.manager.task;

import com.raincat.common.constant.CommonConstant;
import com.raincat.manager.configuration.TxManagerConfiguration;
import com.raincat.manager.service.TxManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * RedisCleanTask.
 * @author xiaoyu
 */
@Component
@Slf4j
public class RedisCleanTask {

    private final TxManagerService txManagerService;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    public RedisCleanTask(final TxManagerService txManagerService) {
        this.txManagerService = txManagerService;
    }


    /**
     *
     * 清除完全提交的事务组信息，每隔48小时执行一次.
     *
     * @throws InterruptedException 异常
     */
//    @Scheduled(fixedDelay=1000*60*48)
    public void removeCommitTxGroup() {
        txManagerService.removeCommitTxGroup();
    }

    /**
     *
     * 清除完全回滚的事务组信息，每隔48小时执行一次.
     *
     * @throws InterruptedException 异常
     */
//    @Scheduled(fixedDelay=1000*60*48)
    public void removeRollBackTxGroup()  {
        txManagerService.removeRollBackTxGroup();
    }


    /**
     *
     * 同步isTxTransactionOpen配置
     *
     * @throws InterruptedException 异常
     */
    @Scheduled(fixedDelay=1000*10)
    public void checkTxTransactionSwitch()  {
        String isTxTransactionOpen=redisTemplate.opsForValue().get("isTxTransactionOpen");
        if(isTxTransactionOpen==null)
        {
            redisTemplate.opsForValue().set("isTxTransactionOpen",CommonConstant.TX_TRANSACTION_ON);
        }
        else
        {
            isTxTransactionOpen=isTxTransactionOpen.replaceAll("\"","");
        }
        if(CommonConstant.TX_TRANSACTION_OFF.equals(isTxTransactionOpen))
        {
            TxManagerConfiguration.isTxTransactionOpen=CommonConstant.TX_TRANSACTION_OFF;
        }
        else
        {
            TxManagerConfiguration.isTxTransactionOpen=CommonConstant.TX_TRANSACTION_ON;
        }
    }

}
