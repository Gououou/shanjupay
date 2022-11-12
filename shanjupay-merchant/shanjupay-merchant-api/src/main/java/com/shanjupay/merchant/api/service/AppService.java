package com.shanjupay.merchant.api.service;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.merchant.api.dto.AppDTO;

import java.util.List;

/**
 * @author 小郭
 * @version 1.0
 */
public interface AppService {

    /**
     * 商户下创建应用
     * @return
     */
    AppDTO createApp(Long merchantId, AppDTO app) throws BusinessException;

    /**
     * 查询商户下的应用列表
     * @param merchantId
     * @return
     */
    List<AppDTO> queryAppByMerchant(Long merchantId) throws BusinessException;

    /**
     * 根据业务id查询应用
     * @param id
     * @return
     */
    AppDTO getAppById(String id) throws BusinessException;

    /**
     * 查询应用是否属于某个商户
     * @param appId
     * @param merchantId
     * @return
     */
    Boolean queryAppInMerchant(String appId, Long merchantId);

}
