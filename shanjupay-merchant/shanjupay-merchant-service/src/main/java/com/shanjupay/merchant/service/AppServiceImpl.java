package com.shanjupay.merchant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.merchant.api.AppService;
import com.shanjupay.merchant.api.dto.AppDTO;
import com.shanjupay.merchant.convert.AppCovert;
import com.shanjupay.merchant.entity.App;
import com.shanjupay.merchant.entity.Merchant;
import com.shanjupay.merchant.mapper.AppMapper;
import com.shanjupay.merchant.mapper.MerchantMapper;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;
@org.apache.dubbo.config.annotation.Service
public class AppServiceImpl implements AppService  {
    @Resource
    private AppMapper appMapper;
    @Resource
    private MerchantMapper merchantMapper;
    //商户创建应用
    @Override
    public AppDTO createAPP(Long merchantId, AppDTO appDTO) throws BusinessException{
        //条件判断 参数是否为空 应用是否已被创建 应用名称是否相同
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(CommonErrorCode.E_200002);
        }
        if (!"2".equals(merchant.getAuditStatus())) {
            throw new BusinessException(CommonErrorCode.E_200003);
        }
        if(isExistAppName(appDTO.getAppName())){
            throw new BusinessException(CommonErrorCode.E_200004);
        }
        //应用名称比需唯一性
        String appName = appDTO.getAppName();
        Boolean existAppName = isExistAppName(appName);
        if(existAppName){
            throw new BusinessException(  CommonErrorCode.E_200004);
        }
        //生成APPId
        appDTO.setAppId( UUID.randomUUID().toString());
        appDTO.setMerchantId(merchantId);
        //转换对象
        App app = AppCovert.INSTANCE.dto2entity(appDTO);
        appMapper.insert(app);
        return appDTO;
    }
    //通过商户Id查询所有应用
    @Override
    public List<AppDTO> selectByMId(Long merchantId) {
        List<App> apps = appMapper.selectList(new LambdaQueryWrapper<App>().eq(App::getMerchantId, merchantId));
        return  AppCovert.INSTANCE.listentity2dto(apps);
    }

    //通过appId查询对应的应用信息
    @Override
    public AppDTO selectByAId(String AppId) {
        App app = appMapper.selectOne(new LambdaQueryWrapper<App>().eq(App::getAppId, AppId));
        return AppCovert.INSTANCE.entity2dto(app);
    }

    @Override
    public void updataApp(String appId, AppDTO appDTO) {

    }

    @Override
    public Boolean queryAppInMerchant(String appId, Long merchantId) {
        LambdaQueryWrapper<App> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(App::getAppId,appId).eq(App::getMerchantId,merchantId);
        Integer integer = appMapper.selectCount(wrapper);
        return integer>0;
    }


    private Boolean isExistAppName(String appName){
        Integer integer = appMapper.selectCount(new LambdaQueryWrapper<App>().eq(App::getAppName, appName));
        return integer>0;
    }




}
