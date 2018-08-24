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

package com.raincat.springcloud.interceptor;

import com.raincat.common.config.TxConfig;
import com.raincat.common.constant.CommonConstant;
import com.raincat.core.concurrent.threadlocal.CompensationLocal;
import com.raincat.core.concurrent.threadlocal.TxTransactionLocal;
import com.raincat.core.interceptor.TxTransactionInterceptor;
import com.raincat.core.service.AspectTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * SpringCloudTxTransactionInterceptor.
 * @author xiaoyu
 */
@Component
@Slf4j
public class SpringCloudTxTransactionInterceptor implements TxTransactionInterceptor {

    private final AspectTransactionService aspectTransactionService;

    @Autowired
    public SpringCloudTxTransactionInterceptor(final AspectTransactionService aspectTransactionService) {
        this.aspectTransactionService = aspectTransactionService;
    }

    @Override
    public Object interceptor(final ProceedingJoinPoint pjp) throws Throwable {
        log.error("thread name:{}",Thread.currentThread().getName());
        log.error("TxTransactionLocal.getInstance().getTxGroupId() :{}",TxTransactionLocal.getInstance().getTxGroupId());
        log.error("(MethodSignature) point.getSignature().getMethod() :{}",((MethodSignature) pjp.getSignature()).getMethod());
        if(StringUtils.isNotBlank(TxTransactionLocal.getInstance().getTxGroupId()))
        {
            //此线程已经开启了分布式事务，不需要再次开启了。
            log.info("此线程已经开启了分布式事务，不需要再次开启了。");
            return pjp.proceed();
        }
        else if(CommonConstant.TX_TRANSACTION_OFF.equals(TxConfig.isTxTransactionOpen))
        {
            //已经关闭了分布式事务
            return pjp.proceed();
        }
        else
        {
            //进行分布式事务
            final String compensationId = CompensationLocal.getInstance().getCompensationId();
            String groupId = null;
            if (StringUtils.isBlank(compensationId)) {
                //如果不是本地反射调用补偿
                RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
                HttpServletRequest request = requestAttributes == null ? null : ((ServletRequestAttributes) requestAttributes).getRequest();
                groupId = request == null ? null : request.getHeader(CommonConstant.TX_TRANSACTION_GROUP);
            }
            return aspectTransactionService.invoke(groupId, pjp);
        }

    }

}
