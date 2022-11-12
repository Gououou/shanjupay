package com.shanjupay.merchant.convert;

import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.entity.Merchant;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author 小郭
 * @version 1.0
 */
@Mapper
public interface MerchantCovert {

    MerchantCovert INSTANCE = Mappers.getMapper(MerchantCovert.class);

    MerchantDTO entity2dto(Merchant entity);

    Merchant dto2entity(MerchantDTO dto);
}
