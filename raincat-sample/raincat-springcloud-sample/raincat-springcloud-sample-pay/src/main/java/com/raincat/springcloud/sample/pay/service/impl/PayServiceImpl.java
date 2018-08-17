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
package com.raincat.springcloud.sample.pay.service.impl;

import com.raincat.core.annotation.TxTransaction;
import com.raincat.springcloud.sample.pay.client.AlipayClient;
import com.raincat.springcloud.sample.pay.client.WechatClient;
import com.raincat.springcloud.sample.pay.entiy.Pay;
import com.raincat.springcloud.sample.pay.mapper.PayMapper;
import com.raincat.springcloud.sample.pay.service.PayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author xiaoyu
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class PayServiceImpl implements PayService {

    private final AlipayClient alipayClient;

    private final WechatClient wechatClient;

    private final PayMapper payMapper;

    @Autowired
    public PayServiceImpl(AlipayClient alipayClient, WechatClient wechatClient, PayMapper payMapper) {
        this.alipayClient = alipayClient;
        this.wechatClient = wechatClient;
        this.payMapper = payMapper;
    }

    @Override
    @TxTransaction
    public Boolean orderPay() {
        Pay pay = new Pay();
        pay.setName("ali|| wechat");
        pay.setTotalAmount(BigDecimal.valueOf(800));
        pay.setCreateTime(new Date());
        payMapper.save(pay);
        alipayClient.save();

        wechatClient.save();

        return true;
    }

    /**
     * 强一致性测试 执行顺序 pay-->alipay-->wechat
     * 当alipay支付异常的时候，pay表的数据不会新增 alipay表不会新增 wechat表不会新增
     */
    @Override
    @TxTransaction
    public void payWithAliPayFail() {

        Pay pay = new Pay();
        pay.setName("ali|| wechat");
        pay.setTotalAmount(BigDecimal.valueOf(200));
        pay.setCreateTime(new Date());
        payMapper.save(pay);

        alipayClient.payFail();

        wechatClient.save();

    }

    /**
     * 强一致性测试 执行顺序 pay-->alipay-->wechat
     * 当alipay支付超时的时候，pay表的数据不会新增  alipay表不会新增 wechat表不会新增
     */
    @Override
    @TxTransaction
    public void payWithAliPayTimeOut() {
        Pay pay = new Pay();
        pay.setName("ali|| wechat");
        pay.setTotalAmount(BigDecimal.valueOf(200));
        pay.setCreateTime(new Date());
        payMapper.save(pay);

        alipayClient.payTimeOut();

        wechatClient.save();
    }

    /**
     * 强一致性测试 执行顺序 pay-->alipay-->wechat
     * 当wechat支付失败的时候，pay表的数据不会新增  alipay表不会新增 wechat表不会新增
     */
    @Override
    @TxTransaction
    public void payWithWechatPayFail() {
        Pay pay = new Pay();
        pay.setName("ali|| wechat");
        pay.setTotalAmount(BigDecimal.valueOf(200));
        pay.setCreateTime(new Date());
        payMapper.save(pay);

        alipayClient.save();

        wechatClient.payFail();

    }

    /**
     * 强一致性测试 执行顺序 pay-->alipay-->wechat
     * 当wechat支付超时的时候，pay表的数据不会新增  alipay表不会新增 wechat表不会新增
     */
    @Override
    @TxTransaction
    public void payWithWechatPayTimeOut() {

        Pay pay = new Pay();
        pay.setName("ali|| wechat");
        pay.setTotalAmount(BigDecimal.valueOf(200));
        pay.setCreateTime(new Date());
        payMapper.save(pay);

        alipayClient.save();

        wechatClient.payTimeOut();

    }

    @Override
    @TxTransaction
    public void wxAliFail() {
        Pay pay = new Pay();
        pay.setName("wxAliFail");
        pay.setTotalAmount(BigDecimal.valueOf(200));
        pay.setCreateTime(new Date());
        payMapper.save(pay);
        this.orderPay();
        wechatClient.wxAliFail();
    }

    @Override
    @TxTransaction
    public void wxForFail() {
        Pay pay = new Pay();
        pay.setName("wxForFail");
        pay.setTotalAmount(BigDecimal.valueOf(200));
        pay.setCreateTime(new Date());
        payMapper.save(pay);

        for(int i =0;i<3;i++)
        {
            wechatClient.wxForFail(i);
        }
    }
}
