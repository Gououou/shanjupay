package com.shanjupay.merchant.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shanjupay.merchant.entity.Merchant;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author author
 * @since 2022-11-04
 */
@Repository
public interface MerchantMapper extends BaseMapper<Merchant> {

}
