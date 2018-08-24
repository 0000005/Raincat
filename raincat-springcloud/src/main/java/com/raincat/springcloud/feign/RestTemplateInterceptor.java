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

package com.raincat.springcloud.feign;

import com.raincat.common.constant.CommonConstant;
import com.raincat.core.concurrent.threadlocal.TxTransactionLocal;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

/**
 * RestTemplateInterceptor.
 * @author xiaoyu
 */
@Slf4j
public class RestTemplateInterceptor implements RequestInterceptor {

    @Override
    public void apply(final RequestTemplate requestTemplate) {
        Collection<String> txTransactionHeaders=requestTemplate.headers().get(CommonConstant.NEW_TX_TRANSACTION_GROUP);
        if(txTransactionHeaders==null||txTransactionHeaders.size()==0)
        {
            requestTemplate.header(CommonConstant.TX_TRANSACTION_GROUP, TxTransactionLocal.getInstance().getTxGroupId());
            log.debug("feign增加txTransaction请求头。");
        }
        else
        {
            log.debug("新增取消事务请求头。");
        }
    }

}
