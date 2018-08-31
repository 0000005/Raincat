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

package com.raincat.manager.controller;

import com.raincat.common.entity.TxManagerServer;
import com.raincat.common.entity.TxManagerServiceDTO;
import com.raincat.common.netty.bean.TxTransactionItem;
import com.raincat.manager.entity.TxManagerInfo;
import com.raincat.manager.service.TxManagerInfoService;
import com.raincat.manager.service.TxManagerService;
import com.raincat.manager.service.execute.HttpTransactionExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * TxManagerController.
 * @author xiaoyu
 */
@RestController
@RequestMapping("/tx/manager")
public class TxManagerController {

    private final TxManagerInfoService txManagerInfoService;

    private final HttpTransactionExecutor httpTransactionExecutor;

    private final TxManagerService txManagerService;



    @Autowired
    public TxManagerController(final TxManagerInfoService txManagerInfoService,
                               final TxManagerService txManagerService,
                               final HttpTransactionExecutor transactionExecutor) {
        this.txManagerInfoService = txManagerInfoService;
        this.httpTransactionExecutor = transactionExecutor;
        this.txManagerService=txManagerService;
    }

    @ResponseBody
    @PostMapping("/findTxManagerServer")
    public TxManagerServer findTxManagerServer() {
        return txManagerInfoService.findTxManagerServer();
    }

    @ResponseBody
    @PostMapping("/loadTxManagerService")
    public List<TxManagerServiceDTO> loadTxManagerService() {
        return txManagerInfoService.loadTxManagerService();
    }

    @RequestMapping("/findTxManagerInfo")
    public TxManagerInfo findTxManagerInfo() {
        return txManagerInfoService.findTxManagerInfo();
    }

    @PostMapping("/httpCommit")
    public void httpCommit(@RequestBody final List<TxTransactionItem> items) {
        httpTransactionExecutor.commit(items);
    }

    @PostMapping("/httpRollBack")
    public void httpRollBack(@RequestBody final List<TxTransactionItem> items) {
        httpTransactionExecutor.rollBack(items);
    }

    @RequestMapping("/transaction-switch")
    public String transactionSwitch(final HttpServletRequest request) {
        txManagerInfoService.switchTransaction();
        return "ok";
    }

    @RequestMapping("/clear-data")
    public String clearData(final HttpServletRequest request) {
        txManagerService.removeCommitTxGroup();
        txManagerService.removeRollBackTxGroup();
        return "ok";
    }

}
