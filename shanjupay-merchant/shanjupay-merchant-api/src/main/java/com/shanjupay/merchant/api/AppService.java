package com.shanjupay.merchant.api;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.merchant.api.dto.AppDTO;

import java.util.List;

public interface AppService {
    //创建应用
    AppDTO createAPP(Long merchantId,AppDTO appDTO)throws BusinessException;
    //通过商户Id查询所有应用
    List<AppDTO> selectByMId(Long mercahntId) throws BusinessException;
    //通过appId查询对应的应用信息
    AppDTO selectByAId(String AppId) throws BusinessException;
    //通过appId修改应用信息
    void updataApp(String appId,AppDTO appDTO);
    //通过查询商户id,appId判断应用是否存在
    Boolean queryAppInMerchant(String appId, Long merchantId);
}
