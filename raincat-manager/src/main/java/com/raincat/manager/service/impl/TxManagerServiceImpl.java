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

package com.raincat.manager.service.impl;

import com.raincat.common.constant.CommonConstant;
import com.raincat.common.enums.TransactionRoleEnum;
import com.raincat.common.enums.TransactionStatusEnum;
import com.raincat.common.holder.DateUtils;
import com.raincat.common.netty.bean.TxTransactionGroup;
import com.raincat.common.netty.bean.TxTransactionItem;
import com.raincat.manager.config.Constant;
import com.raincat.manager.service.TxManagerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * TxManagerServiceImpl.
 * @author xiaoyu
 */
@Component
@Slf4j
@SuppressWarnings("unchecked")
public class TxManagerServiceImpl implements TxManagerService {

    private final RedisTemplate redisTemplate;

    @Autowired
    public TxManagerServiceImpl(final RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Boolean saveTxTransactionGroup(final TxTransactionGroup txTransactionGroup) {
        try {
            final String groupId = txTransactionGroup.getId();
            //保存数据 到sortSet
            redisTemplate.opsForZSet().add(CommonConstant.REDIS_KEY_SET, groupId, CommonConstant.REDIS_SCOPE);
            final List<TxTransactionItem> itemList = txTransactionGroup.getItemList();
            if (CollectionUtils.isNotEmpty(itemList)) {
                for (TxTransactionItem item : itemList) {
                    redisTemplate.opsForHash().put(cacheKey(groupId), item.getTaskKey(), item);
                }
            }
        } catch (Exception e) {
            log.error("添加事务组到redis失败，txTransactionGroup:{}，exception:{}",txTransactionGroup,e);
            return false;
        }
        return true;
    }

    @Override
    public Boolean addTxTransaction(final String txGroupId, final TxTransactionItem txTransactionItem) {
        try {
            redisTemplate.opsForHash().put(cacheKey(txGroupId), txTransactionItem.getTaskKey(), txTransactionItem);
        } catch (Exception e) {
            log.error("添加事务到redis失败，txTransactionGroup:{}，exception:{}",txTransactionItem,e);
            return false;
        }
        return true;
    }

    @Override
    public List<TxTransactionItem> listByTxGroupId(final String txGroupId) {
        final Map<Object, TxTransactionItem> entries =
                redisTemplate.opsForHash().entries(cacheKey(txGroupId));
        final Collection<TxTransactionItem> values = entries.values();
        return new ArrayList<>(values);
    }

    @Override
    public void removeRedisByTxGroupId(final String txGroupId) {
        redisTemplate.delete(cacheKey(txGroupId));
    }

    @Override
    public void updateTxTransactionItemStatus(final String key, final String hashKey,
                                              final int status, final Object message) {
        try {
            final TxTransactionItem item = (TxTransactionItem)
                    redisTemplate.opsForHash().get(cacheKey(key), hashKey);
            item.setStatus(status);
            if (Objects.nonNull(message)) {
                item.setMessage(message);
            }
            //计算耗时
            final String createDate = item.getCreateDate();
            final LocalDateTime now = LocalDateTime.now();
            try {
                final LocalDateTime createDateTime = DateUtils.parseLocalDateTime(createDate);
                final long consumeTime = DateUtils.getSecondsBetween(createDateTime, now);
                item.setConsumeTime(consumeTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            redisTemplate.opsForHash().put(cacheKey(key), item.getTaskKey(), item);
        } catch (BeansException e) {
            log.error("更新事务状态失败，key:{}，hashKey:{}，status：{}，message:{}",key,hashKey,status,message);
            e.printStackTrace();
        }
    }

    @Override
    public int findTxTransactionGroupStatus(final String txGroupId) {
        try {
            final TxTransactionItem item = (TxTransactionItem)
                    redisTemplate.opsForHash().get(cacheKey(txGroupId), txGroupId);
            return item.getStatus();
        } catch (BeansException e) {
            e.printStackTrace();
            return TransactionStatusEnum.ROLLBACK.getCode();
        }
    }

    @Override
    public void removeCommitTxGroup() {
        log.warn("清理已完全提交的事务组记录......");
        final Set<String> keys = redisTemplate.keys(Constant.REDIS_KEYS);
        keys.parallelStream().forEach(key -> {
            final Map<Object, TxTransactionItem> entries = redisTemplate.opsForHash().entries(key);
            final Collection<TxTransactionItem> values = entries.values();
            final boolean present = values.stream()
                    .anyMatch(item -> item.getStatus() != TransactionStatusEnum.COMMIT.getCode());
            if (!present) {
                String setValue=key.substring("transaction:group:".length());
                redisTemplate.opsForZSet().remove(CommonConstant.REDIS_KEY_SET,setValue);
                redisTemplate.delete(key);
            }
        });

    }

    @Override
    public void removeRollBackTxGroup() {
        log.warn("清理已完全回滚的事务组记录......");
        final Set<String> keys = redisTemplate.keys(Constant.REDIS_KEYS);
        keys.parallelStream().forEach(key -> {
            final Map<Object, TxTransactionItem> entries = redisTemplate.opsForHash().entries(key);
            final Collection<TxTransactionItem> values = entries.values();
            final Optional<TxTransactionItem> any =
                    values.stream().filter(item -> item.getRole() == TransactionRoleEnum.START.getCode()
                            && item.getStatus() == TransactionStatusEnum.ROLLBACK.getCode())
                            .findAny();
            if (any.isPresent()) {
                String setValue=key.substring("transaction:group:".length());
                redisTemplate.opsForZSet().remove(CommonConstant.REDIS_KEY_SET,setValue);
                redisTemplate.delete(key);
            }
        });

    }

    private String cacheKey(final String key) {
        return String.format(CommonConstant.REDIS_PRE_FIX, key);
    }
}
