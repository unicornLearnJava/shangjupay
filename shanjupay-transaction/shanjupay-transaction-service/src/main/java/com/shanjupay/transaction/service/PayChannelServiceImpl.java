package com.shanjupay.transaction.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shanjupay.common.cache.Cache;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.RedisUtil;
import com.shanjupay.transaction.api.PayChannelService;
import com.shanjupay.transaction.api.dto.PayChannelDTO;
import com.shanjupay.transaction.api.dto.PayChannelParamDTO;
import com.shanjupay.transaction.api.dto.PlatformChannelDTO;
import com.shanjupay.transaction.convert.PayChannelParamConvert;
import com.shanjupay.transaction.convert.PlatformChannelConvert;
import com.shanjupay.transaction.entity.AppPlatformChannel;
import com.shanjupay.transaction.entity.PayChannel;
import com.shanjupay.transaction.entity.PayChannelParam;
import com.shanjupay.transaction.entity.PlatformChannel;
import com.shanjupay.transaction.mapper.AppPlatformChannelMapper;
import com.shanjupay.transaction.mapper.PayChannelMapper;
import com.shanjupay.transaction.mapper.PayChannelParamMapper;
import com.shanjupay.transaction.mapper.PlatformChannelMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.List;

@Service
public class PayChannelServiceImpl implements PayChannelService {
    @Resource
    private PayChannelMapper payChannelMapper;
    @Resource
    private AppPlatformChannelMapper appPlatformChannelMapper;
    @Resource
    private PlatformChannelMapper platformChannelMapper;
    @Resource
    private PayChannelParamMapper payChannelParamMapper;
    @Autowired
    Cache cache;

    /**
     * 查询平台的服务类型
     *
     * @return
     * @throws BusinessException
     */
   @Override
    public List<PlatformChannelDTO> queryPlatformChannel() throws BusinessException {
        //查询platform_channel表的全部记录
        List<PlatformChannel> platformChannels = platformChannelMapper.selectList(null);
        //将platformChannels转成包含dto的list
        return PlatformChannelConvert.INSTANCE.listentity2listdto(platformChannels);
    }
    //绑定服务类型
    @Override
    public void bindPlatformChannelForApp(String appId, String platformChannelCodes) {
        //根据appId和服务类型查询是否已经绑定
        AppPlatformChannel appPlatformChannel = appPlatformChannelMapper.selectOne(new LambdaQueryWrapper<AppPlatformChannel>()
                .eq(AppPlatformChannel::getAppId, appId)
                .eq(AppPlatformChannel::getPlatformChannel, platformChannelCodes));
        if (appPlatformChannel == null) {
            AppPlatformChannel a=new AppPlatformChannel();
            a.setAppId(appId);
            a.setPlatformChannel(platformChannelCodes);
            appPlatformChannelMapper.insert(a);
        }
    }

    //查询某个应用是否绑定了服务类型
    @Override
    public int queryAppBindPlatformChannel(String appId, String platformChannelCodes) {
        AppPlatformChannel appPlatformChannel = appPlatformChannelMapper.selectOne(new LambdaQueryWrapper<AppPlatformChannel>()
                .eq(AppPlatformChannel::getAppId, appId)
                .eq(AppPlatformChannel::getPlatformChannel, platformChannelCodes));
        if(appPlatformChannel != null){
            return 1;
        }
        return 0;
    }

    //这里是要查询某服务类型下的支付渠道，以便下一步为某支付渠道配置参数。
    @Override
    public List<PayChannelDTO> queryPayChannelByPlatformChannel(String platformChannelCode) {
        return platformChannelMapper.selectPayChannelByPlatformChannel(platformChannelCode);
    }

    //保存支付渠道参数
    @Override
    public void savePayChannelParam(PayChannelParamDTO payChannelParamDTO) throws BusinessException {
        //非空判断
        if(payChannelParamDTO == null || StringUtils.isBlank(payChannelParamDTO.getAppId())
                || StringUtils.isBlank(payChannelParamDTO.getPlatformChannelCode())
                || StringUtils.isBlank(payChannelParamDTO.getPayChannel())){
            throw new BusinessException(CommonErrorCode.E_100108);
        }
        //根据应用appId和服务类型code查询到绑定到的id(这里需要单独抽取方法)
        Long app2payChannelId = selectIdByAppPlatformChannel(payChannelParamDTO.getAppId(), payChannelParamDTO.getPlatformChannelCode());
        //未绑定则抛异常
        if (app2payChannelId == null){
            throw new BusinessException(CommonErrorCode.E_300010);
        }
        //根据查询到的id和支付渠道查询参数信息
        PayChannelParam payChannelParam = payChannelParamMapper.selectOne(new LambdaQueryWrapper<PayChannelParam>()
                .eq(PayChannelParam::getAppPlatformChannelId, app2payChannelId)
                .eq(PayChannelParam::getPayChannel, payChannelParamDTO.getPayChannel()));
        if(payChannelParam != null){
            //更新已有配置
            payChannelParam.setChannelName(payChannelParamDTO.getChannelName());
            payChannelParam.setParam(payChannelParamDTO.getParam());
            payChannelParamMapper.updateById(payChannelParam);
        }else {
            //插入新配置
            PayChannelParam payChannelParam1 = PayChannelParamConvert.INSTANCE.dto2entity(payChannelParamDTO);
            payChannelParam1.setId(null);
            payChannelParam1.setAppPlatformChannelId(app2payChannelId);
            payChannelParamMapper.insert(payChannelParam1);
        }
        //进行缓存
        updateCache(payChannelParamDTO.getAppId(),payChannelParamDTO.getPlatformChannelCode());
    }

    //根据应用appId和服务类型获取原始支付参数param，结果可能是多个
    @Override
    public List<PayChannelParamDTO> queryPayChannelParamByAppAndPlatform(String appId, String platformChannelCode) throws BusinessException {
        //从缓存查询
        // 1.key的构建 如：SJ_PAY_PARAM:b910da455bc84514b324656e1088320b:shanju_c2b
        String redisKey = RedisUtil.keyBuilder(appId, platformChannelCode);
        //是否有缓存
        Boolean exists = cache.exists(redisKey);
        if(exists){
            //从redis获取key对应的value
            String value = cache.get(redisKey);
            //将value转成对象
            List<PayChannelParamDTO> paramDTOS = JSONObject.parseArray(value, PayChannelParamDTO.class);
            return paramDTOS;
        }
        Long aLong = selectIdByAppPlatformChannel(appId, platformChannelCode);
        List<PayChannelParam> payChannelParams = payChannelParamMapper.selectList(new LambdaQueryWrapper<PayChannelParam>().eq(PayChannelParam::getAppPlatformChannelId,aLong));
        //存入缓存
        updateCache(appId,platformChannelCode);
        return PayChannelParamConvert.INSTANCE.listentity2listdto(payChannelParams);
    }

    //根据应用appId和服务类型和支付渠道code获取原始支付参数param，结果唯一
    @Override
    public PayChannelParamDTO queryParamByAppPlatformAndPayChannel(String appId, String platformChannelCode, String payChannel) throws BusinessException {
        List<PayChannelParamDTO> payChannelParamDTOS = queryPayChannelParamByAppAndPlatform(appId, platformChannelCode);
        for (PayChannelParamDTO payChannelParam: payChannelParamDTOS) {
            if(payChannelParam.getPayChannel().equals(payChannel)){
                return payChannelParam;
            }
        }
        return null;
    }

    //公用的抽取方法根据应用appId和服务类型code查询到绑定到的id
    private Long selectIdByAppPlatformChannel(String appId,String payChannelCode){
        AppPlatformChannel appPlatformChannel = appPlatformChannelMapper.selectOne(new LambdaQueryWrapper<AppPlatformChannel>()
                .eq(AppPlatformChannel::getAppId, appId)
                .eq(AppPlatformChannel::getPlatformChannel, payChannelCode));
        if(appPlatformChannel != null){
            return appPlatformChannel.getId();
        }
        return null;
    }
    //更新缓存
    private void updateCache(String appId, String platformChannel) {
       //处理redis缓存
        // 1.key的构建 如：SJ_PAY_PARAM:b910da455bc84514b324656e1088320b:shanju_c2b
        String redisKey = RedisUtil.keyBuilder(appId, platformChannel);
        //2.查询redis,检查key是否存在
        Boolean exists = cache.exists(redisKey);
        if (exists) {
            //存在，则清除
            // 删除原有缓存
            cache.del(redisKey);
        }
        //3.从数据库查询应用的服务类型对应的实际支付参数，并重新存入缓存
        Long aLong = selectIdByAppPlatformChannel(appId, platformChannel);
        List<PayChannelParam> payChannelParams = payChannelParamMapper.selectList(new LambdaQueryWrapper<PayChannelParam>().eq(PayChannelParam::getAppPlatformChannelId,aLong));
        List<PayChannelParamDTO> payChannelParamDTOS = PayChannelParamConvert.INSTANCE.listentity2listdto(payChannelParams);
        if (payChannelParamDTOS != null) {
            //存入缓存
            cache.set(redisKey, JSON.toJSON(payChannelParamDTOS).toString());
            }
          }
}
