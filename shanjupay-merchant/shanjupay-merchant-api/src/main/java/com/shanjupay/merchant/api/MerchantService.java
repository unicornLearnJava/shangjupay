package com.shanjupay.merchant.api;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.PageVO;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.api.dto.StaffDTO;
import com.shanjupay.merchant.api.dto.StoreDTO;
import com.shanjupay.merchant.api.dto.StoreStaffDTO;

public interface MerchantService {
    //测试api
    public MerchantDTO queryMerchantById(Long id);
    //创建商户
    MerchantDTO createMerchant(MerchantDTO merchantDTO) throws BusinessException;
    //商户资质申请
    void applyMerchant(Long merchantId,MerchantDTO merchantDTO);
    //新增门店
    StoreDTO createStore(StoreDTO storeDTO) throws BusinessException;
    //新增员工
    StaffDTO createStaff(StaffDTO staffDTO);
    //绑定门店与员工的关系
    void BinDingStore2StoreStaff(Long storeId,Long storeStaffId);
    //根据租户Id查询商户信息
    MerchantDTO queryMerchantByTenantId(Long tenantId);
    /*** 分页条件查询商户下门店
     * * @param storeDTO
     * * @param pageNo
     * * @param pageSize
     * * @return */
    PageVO<StoreDTO> queryStoreByPage(StoreDTO storeDTO, Integer pageNo, Integer pageSize);

    /*** 查询门店是否属于某商户
     * * @param storeId
     * * @param merchantId
     * * @return */
    Boolean queryStoreInMerchant(Long storeId, Long merchantId);
}
