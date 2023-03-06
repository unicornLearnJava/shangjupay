package com.shanjupay.transaction.api;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.transaction.api.dto.PayChannelDTO;
import com.shanjupay.transaction.api.dto.PayChannelParamDTO;
import com.shanjupay.transaction.api.dto.PlatformChannelDTO;

import java.util.List;

public interface PayChannelService {
    /**
     * 查询平台的服务类型
     * @return
     * @throws BusinessException
     */
    List<PlatformChannelDTO> queryPlatformChannel() throws BusinessException;

    //绑定服务类型
    void bindPlatformChannelForApp(String appId, String platformChannelCodes) throws BusinessException;
    //查询应用是否已经绑定了某个服务类型
    int queryAppBindPlatformChannel(String appId, String platformChannelCodes) throws BusinessException;
    //这里是要查询某服务类型下的支付渠道，以便下一步为某支付渠道配置参数。
    List<PayChannelDTO> queryPayChannelByPlatformChannel(String platformChannelCode) throws BusinessException;
    //保存支付渠道参数
    void savePayChannelParam(PayChannelParamDTO payChannelParamDTO) throws BusinessException;
    //根据应用appId和服务类型获取原始支付参数param，结果可能是多个
    List<PayChannelParamDTO> queryPayChannelParamByAppAndPlatform(String appId ,String platformChannelCode) throws  BusinessException;
    //根据应用appId和服务类型和支付渠道code获取原始支付参数param，结果唯一
    PayChannelParamDTO  queryParamByAppPlatformAndPayChannel(String appId ,String platformChannelCode,String payChannel) throws BusinessException;
}
