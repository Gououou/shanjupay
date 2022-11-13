package com.shanjupay.merchant.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.domain.PageVO;
import com.shanjupay.common.util.PhoneUtil;

import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.api.dto.StaffDTO;
import com.shanjupay.merchant.api.dto.StoreDTO;
import com.shanjupay.merchant.api.service.MerchantService;
import com.shanjupay.merchant.convert.MerchantCovert;
import com.shanjupay.merchant.convert.StaffConvert;
import com.shanjupay.merchant.convert.StoreConvert;
import com.shanjupay.merchant.entity.Merchant;
import com.shanjupay.merchant.entity.Staff;
import com.shanjupay.merchant.entity.Store;
import com.shanjupay.merchant.entity.StoreStaff;
import com.shanjupay.merchant.mapper.MerchantMapper;
import com.shanjupay.merchant.mapper.StaffMapper;
import com.shanjupay.merchant.mapper.StoreMapper;
import com.shanjupay.merchant.mapper.StoreStaffMapper;
import com.shanjupay.user.api.TenantService;
import com.shanjupay.user.api.dto.tenant.CreateTenantRequestDTO;
import com.shanjupay.user.api.dto.tenant.TenantDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author 小郭
 * @version 1.0
 */

@Service
@Slf4j
public class MerchantServiceImpl implements MerchantService {

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private StoreMapper storeMapper;

    @Autowired
    private StaffMapper staffMapper;

    @Autowired
    private StoreStaffMapper storeStaffMapper;

    @Reference
    private TenantService tenantService;

    /**
     * 根据ID查询详细信息
     * @param merchantId
     * @return
     */
    @Override
    public MerchantDTO queryMerchantById(Long merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        MerchantDTO merchantDTO = MerchantCovert.INSTANCE.entity2dto(merchant);
        return merchantDTO;
    }

    @Override
    @Transactional
    public MerchantDTO createMerchant(MerchantDTO merchantDTO) throws BusinessException {
        // 1.校验
        if (merchantDTO == null) {
            throw new BusinessException(CommonErrorCode.E_100108);
        }
        //手机号非空校验
        if (StringUtils.isBlank(merchantDTO.getMobile())) {
            throw new BusinessException(CommonErrorCode.E_100112);
        }
        //校验手机号的合法性
        if (!PhoneUtil.isMatches(merchantDTO.getMobile())) {
            throw new BusinessException(CommonErrorCode.E_100109);
        }
        //联系人非空校验
        if (StringUtils.isBlank(merchantDTO.getUsername())) {
            throw new BusinessException(CommonErrorCode.E_100110);
        }
        //密码非空校验
        if (StringUtils.isBlank(merchantDTO.getPassword())) {
            throw new BusinessException(CommonErrorCode.E_100111);
        }

        //校验商户手机号的唯一性,根据商户的手机号查询商户表，如果存在记录则说明已有相同的手机号重复
        LambdaQueryWrapper<Merchant> lambdaQryWrapper = new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getMobile,merchantDTO.getMobile());
        Integer count = merchantMapper.selectCount(lambdaQryWrapper);
        if(count>0){
            throw new BusinessException(CommonErrorCode.E_100113);
        }
        //2.添加租户 和账号 并绑定关系
        CreateTenantRequestDTO createTenantRequest = new CreateTenantRequestDTO();
        createTenantRequest.setMobile(merchantDTO.getMobile());
        //表示该租户类型是商户
        createTenantRequest.setTenantTypeCode("shanju-merchant");
        //设置租户套餐为初始化套餐餐
        createTenantRequest.setBundleCode("shanju-merchant");
        //租户的账号信息
        createTenantRequest.setUsername(merchantDTO.getUsername());
        createTenantRequest.setPassword(merchantDTO.getPassword());
        //新增租户并设置为管理员
        createTenantRequest.setName(merchantDTO.getUsername());
        log.info("商户中心调用统一账号服务，新增租户和账号");
        TenantDTO tenantDTO = tenantService.createTenantAndAccount(createTenantRequest);
        if (tenantDTO == null || tenantDTO.getId() == null) {
            throw new BusinessException(CommonErrorCode.E_200012);
        }
        //判断租户下是否已经注册过商户
        Merchant merchant = merchantMapper.selectOne(new QueryWrapper<Merchant>().lambda().eq(Merchant::getTenantId, tenantDTO.getId()));
        if (merchant != null && merchant.getId() != null) {
            throw new BusinessException(CommonErrorCode.E_200017);
        }
        //3. 设置商户所属租户
        merchantDTO.setTenantId(tenantDTO.getId());
        //设置审核状态，注册时默认为"0"
        merchantDTO.setAuditStatus("0");//审核状态 0-未申请,1-已申请待审核,2-审核通过,3-审核拒绝
        Merchant entity = MerchantCovert.INSTANCE.dto2entity(merchantDTO);
        //保存商户信息
        log.info("保存商户注册信息");
        merchantMapper.insert(entity);
        //4.新增门店，创建根门店
        StoreDTO storeDTO = new StoreDTO();
        storeDTO.setMerchantId(entity.getId());
        storeDTO.setStoreName("根门店");
        storeDTO = createStore(storeDTO);
        log.info("门店信息：{}" + JSON.toJSONString(storeDTO));
        //5.新增员工，并设置归属门店
        StaffDTO staffDTO = new StaffDTO();
        staffDTO.setMerchantId(entity.getId());
        staffDTO.setMobile(merchantDTO.getMobile());
        staffDTO.setUsername(merchantDTO.getUsername());
        //为员工选择归属门店,此处为根门店
        staffDTO.setStoreId(storeDTO.getId());
        staffDTO = createStaff(staffDTO);

        //6.为门店设置管理员
        bindStaffToStore(storeDTO.getId(), staffDTO.getId());

        //返回商户注册信息
        return MerchantCovert.INSTANCE.entity2dto(entity);
    }

    @Override
    @Transactional
    public void applyMerchant(Long merchantId, MerchantDTO merchantDTO) throws BusinessException {
        //接收资质申请信息，更新到商户表
        if(merchantDTO == null || merchantId == null){
            throw new BusinessException(CommonErrorCode.E_100108);
        }
        //根据id查询商户
        Merchant merchant = merchantMapper.selectById(merchantId);
        if(merchant == null){
            throw new BusinessException(CommonErrorCode.E_200002);
        }
        Merchant merchant_update = MerchantCovert.INSTANCE.dto2entity(merchantDTO);

        merchant_update.setId(merchant.getId());
        merchant_update.setMobile(merchant.getMobile());
        merchant_update.setAuditStatus("1");//已申请待审核
        merchant_update.setTenantId(merchant.getTenantId());//租户id
        //更新
        merchantMapper.updateById(merchant_update);
    }

    @Override
    public StoreDTO createStore(StoreDTO storeDTO) throws BusinessException {
        Store store = StoreConvert.INSTANCE.dto2entity(storeDTO);
        log.info("商户下新增门店"+ JSON.toJSONString(store));
        storeMapper.insert(store);
        return StoreConvert.INSTANCE.entity2dto(store);
    }

    @Override
    public StaffDTO createStaff(StaffDTO staffDTO) throws BusinessException {
        //1.校验手机号格式及是否存在
        String mobile = staffDTO.getMobile();
        if (StringUtils.isBlank(mobile)) {
            throw new BusinessException(CommonErrorCode.E_100112);
        }
        //根据商户id和手机号校验唯一性
        if(isExistStaffByMobile(mobile, staffDTO.getMerchantId())){
            throw new BusinessException(CommonErrorCode.E_100113);
        }
        //2.校验用户名是否为空
        String username = staffDTO.getUsername();
        if(StringUtils.isBlank(username)){
            throw new BusinessException(CommonErrorCode.E_100110);
        }
        //根据商户id和账号校验唯一性
        if(isExistStaffByUserName(username, staffDTO.getMerchantId())){
            throw new BusinessException(CommonErrorCode.E_100114);
        }
        Staff entity = StaffConvert.INSTANCE.dto2entity(staffDTO);
        log.info("商户下新增员工");
        staffMapper.insert(entity);
        return StaffConvert.INSTANCE.entity2dto(entity);
    }

    /**
     * 根据手机号判断员工是否已在指定商户存在
     * @param mobile 手机号
     * @return
     */
    private boolean isExistStaffByMobile(String mobile, Long merchantId) {
        LambdaQueryWrapper<Staff> lambdaQueryWrapper = new LambdaQueryWrapper<Staff>();
        lambdaQueryWrapper.eq(Staff::getMobile, mobile).eq(Staff::getMerchantId, merchantId);
        int i = staffMapper.selectCount(lambdaQueryWrapper);
        return i > 0;
    }

    /**
     * 根据账号判断员工是否已在指定商户存在
     * @param userName
     * @param merchantId
     * @return
     */
    private boolean isExistStaffByUserName(String userName, Long merchantId) {
        LambdaQueryWrapper<Staff> lambdaQueryWrapper = new LambdaQueryWrapper<Staff>();
        lambdaQueryWrapper.eq(Staff::getUsername, userName).eq(Staff::getMerchantId, merchantId);
        int i = staffMapper.selectCount(lambdaQueryWrapper);
        return i > 0;
    }

    @Override
    public void bindStaffToStore(Long storeId, Long staffId) throws BusinessException {
        StoreStaff storeStaff = new StoreStaff();
        storeStaff.setStoreId(storeId);
        storeStaff.setStaffId(staffId);
        storeStaffMapper.insert(storeStaff);
    }

    @Override
    public MerchantDTO queryMerchantByTenantId(Long tenantId) throws BusinessException {
        Merchant merchant = merchantMapper.selectOne(
                new QueryWrapper<Merchant>().lambda().eq(Merchant::getTenantId, tenantId));
        return MerchantCovert.INSTANCE.entity2dto(merchant);
    }

    @Override
    public PageVO<StoreDTO> queryStoreByPage(StoreDTO storeDTO, Integer pageNo, Integer pageSize) {
        // 创建分页
        Page<Store> page = new Page<>(pageNo, pageSize);
        // 构造查询条件
        QueryWrapper<Store> qw = new QueryWrapper();
        if (null != storeDTO && null != storeDTO.getMerchantId()) {
            qw.lambda().eq(Store::getMerchantId, storeDTO.getMerchantId());
        }
        // 执行查询
        IPage<Store> storeIPage = storeMapper.selectPage(page, qw);
        // entity List转DTO List
        List<StoreDTO> storeList = StoreConvert.INSTANCE.listentity2dto(storeIPage.getRecords());
        // 封装结果集
        return new PageVO<>(storeList, storeIPage.getTotal(), pageNo, pageSize);
    }

    /**
     * 查询门店是否属于某商户
     *
     * @param storeId
     * @param merchantId
     * @return
     */
    @Override
    public Boolean queryStoreInMerchant(Long storeId, Long merchantId) {
        Integer count = storeMapper.selectCount(new LambdaQueryWrapper<Store>().eq(Store::getId, storeId)
                .eq(Store::getMerchantId, merchantId));
        return count>0;
    }
}
