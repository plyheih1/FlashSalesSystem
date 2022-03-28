package com.ply.flashsalessystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ply.flashsalessystem.entity.pojo.OrderForGoods;
import com.ply.flashsalessystem.entity.pojo.Store;
import com.ply.flashsalessystem.entity.pojo.StoreAmount;
import com.ply.flashsalessystem.entity.result.Result;
import com.ply.flashsalessystem.entity.status.StoreAmountStatus;
import com.ply.flashsalessystem.entity.wrapper.WrapperOrder;
import com.ply.flashsalessystem.mapper.StoreAmountMapper;
import com.ply.flashsalessystem.mapper.StoreMapper;
import com.ply.flashsalessystem.service.OrderForGoodsService;
import com.ply.flashsalessystem.service.StoreAmountService;
import com.ply.flashsalessystem.service.StoreService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ply.flashsalessystem.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author XiaoPan
 * @since 2022-03-23
 */
@Service
public class StoreServiceImpl extends ServiceImpl<StoreMapper, Store> implements StoreService {
    @Autowired
    StoreMapper storeMapper;

    @Autowired
    StoreAmountService storeAmountService;

    /**
     * 修改 store 信息
     *
     * 注意 name 和 iphone 修改的话 需要验证
     *
     * 暂时 只能修改 name iphone 地址 密码
     *
     *
     * @param store
     * @return
     */
    @Override
    public boolean updateStoreDynamic(Store store) {
        //验证 name
        if (storeMapper.selectOne(new QueryWrapper<Store>().eq("sname",store.getSname())) != null) {
            return false;
        }
        //验证iphone
        if (storeMapper.selectOne(new QueryWrapper<Store>().eq("iphone",store.getIphone())) != null) {
            return false;
        }
        return storeMapper.updateStoreDynamic(store);
    }


    /**
     * 商家提现 需要修改 商家表 , 商家账户流水表
     *  增加 事务
     * @param money  提现金额
     * @return
     */
    @Override
    @Transactional
    public boolean toCash(Double money) {
        //todo 可以优化sql 不需要查询  修改商家表
        Store store = storeMapper.selectById(UserUtils.getUserId());
        store.setBalanceMoney((store.getBalanceMoney() - money));
        updateStoreDynamic(store);

        // 增加商家流水记录 表
        StoreAmount storeAmount = new StoreAmount();
        storeAmount.setAmount((store.getBalanceMoney() - money));
        storeAmount.setStoreId(UserUtils.getUserId());
        storeAmount.setStatus(StoreAmountStatus.TO_CASH);
        storeAmountService.save(storeAmount);

        return true;
    }
}
