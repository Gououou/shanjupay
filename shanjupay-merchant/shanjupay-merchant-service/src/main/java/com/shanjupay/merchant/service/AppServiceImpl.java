package com.shanjupay.merchant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.RandomUuidUtil;
import com.shanjupay.merchant.api.dto.AppDTO;
import com.shanjupay.merchant.api.service.AppService;
import com.shanjupay.merchant.convert.AppCovert;
import com.shanjupay.merchant.entity.App;
import com.shanjupay.merchant.entity.Merchant;
import com.shanjupay.merchant.mapper.AppMapper;
import com.shanjupay.merchant.mapper.MerchantMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author 小郭
 * @version 1.0
 */
@Service
@Slf4j
public class AppServiceImpl implements AppService {

    @Autowired
    private AppMapper appMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Override
    public AppDTO createApp(Long merchantId, AppDTO app) throws BusinessException {
        //校验商户是否通过资质审核
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(CommonErrorCode.E_200002);
        }
        if (!"2".equals(merchant.getAuditStatus())) {
            throw new BusinessException(CommonErrorCode.E_200003);
        }
        if(isExistAppName(app.getAppName())){
            throw new BusinessException(CommonErrorCode.E_200004);
        }
        //保存应用信息
        app.setAppId(RandomUuidUtil.getUUID());
        app.setMerchantId(merchant.getId());
        App entity = AppCovert.INSTANCE.dto2entity(app);
        appMapper.insert(entity);
        return AppCovert.INSTANCE.entity2dto(entity);
    }

    /**
     * 校验应用名是否已被使用
     * @param appName
     * @return
     */
    private boolean isExistAppName(String appName) {
        Integer count = appMapper.selectCount(new QueryWrapper<App>().lambda().eq(App::getAppName, appName));
        return count.intValue() > 0;
    }

    /**
     * 查询商户下的应用列表
     * @param merchantId
     * @return
     */
    @Override
    public List<AppDTO> queryAppByMerchant(Long merchantId) throws BusinessException {
        List<App> apps = appMapper.selectList(new QueryWrapper<App>().lambda().eq(App::getMerchantId,merchantId));
        List<AppDTO> appDTOS = AppCovert.INSTANCE.listentity2dto(apps);
        return appDTOS;
    }

    /**
     * 根据业务id查询应用
     * @param id
     * @return
     */
    @Override
    public AppDTO getAppById(String id) throws BusinessException {
        App app = appMapper.selectOne(new QueryWrapper<App>().lambda().eq(App::getAppId, id));
        return AppCovert.INSTANCE.entity2dto(app);
    }

    /**
     * 查询应用是否属于某个商户
     *
     * @param appId
     * @param merchantId
     * @return
     */
    @Override
    public Boolean queryAppInMerchant(String appId, Long merchantId) {
        Integer count = appMapper.selectCount(new LambdaQueryWrapper<App>().eq(App::getAppId, appId)
                .eq(App::getMerchantId, merchantId));
        return count>0;
    }
}
