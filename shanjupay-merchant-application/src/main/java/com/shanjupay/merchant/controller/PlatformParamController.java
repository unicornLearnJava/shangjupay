package com.shanjupay.merchant.controller;

import com.shanjupay.merchant.common.util.SecurityUtil;
import com.shanjupay.transaction.api.PayChannelService;
import com.shanjupay.transaction.api.dto.PayChannelDTO;
import com.shanjupay.transaction.api.dto.PayChannelParamDTO;
import com.shanjupay.transaction.api.dto.PlatformChannelDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api("商户平台-支付渠道与参数配置")
@RestController
public class PlatformParamController {
    @Reference
    private PayChannelService payChannelService;

    @ApiOperation("获取平台支持的所有服务类型")
    @GetMapping("/my/platform-channels")
    public List<PlatformChannelDTO> selectAll(){
        return payChannelService.queryPlatformChannel();
    }

    @ApiOperation("绑定服务类型")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "应用id",name = "appId"
                    ,dataType = "string",paramType = "path"),
            @ApiImplicitParam(value = "服务类型code",name = "platformChannelCodes"
                    ,dataType = "string",paramType = "query") })
    @PostMapping("/my/apps/{appId}/platform-channels")
    public void bindPlatformForApp(@PathVariable("appId") String appId, @RequestParam("platformChannelCodes") String platformChannelCodes ){
        payChannelService.bindPlatformChannelForApp(appId,platformChannelCodes);
    }



    @ApiOperation("查询某个应用是否绑定了服务类型")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "appId",value = "应用iD"
                    ,required = true,dataType = "String",paramType = "query"),
            @ApiImplicitParam(name = "platformChannelCodes",value = "服务类型code"
                    ,required = true,dataType = "String",paramType = "query")
    })
    @GetMapping("/my/merchants/apps/platformchannels")
    public int queryAppBindPlatformChannel(@RequestParam("appId") String appId,@RequestParam("platformChannel") String platformChannel){
        return payChannelService.queryAppBindPlatformChannel(appId, platformChannel);
    }

    @ApiOperation("根据服务类型获取支付渠道")
    @ApiImplicitParam(name = "platformChannelCode",value = "服务类型code"
            ,required = true,dataType = "String",paramType = "path")
    @GetMapping(value="/my/pay-channels/platform-channel/{platformChannelCode}")
    public List<PayChannelDTO> queryPayChannelByPlatformChannel(@PathVariable("platformChannelCode") String platformChannelCode ){
       return payChannelService.queryPayChannelByPlatformChannel(platformChannelCode);
    }

    @ApiOperation("商户配置支付参数")
    @ApiImplicitParam(name = "payChannelParamDTO",value = "支付参数"
            ,required = true,dataType = "PayChannelParam",paramType = "body")
    @RequestMapping(value = "/my/pay-channel-params",method = {RequestMethod.POST,RequestMethod.PUT})
    public void createPayChannelParam(@RequestBody PayChannelParamDTO payChannelParamDTO){
        Long merchantId = SecurityUtil.getMerchantId();
        payChannelParamDTO.setMerchantId(merchantId);
        payChannelService.savePayChannelParam(payChannelParamDTO);
    }

    @ApiOperation("获取指定应用指定服务类型下所包含的原始支付渠道参数列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "appId", value = "应用id"
                    , required = true, dataType = "String", paramType = "path"),
            @ApiImplicitParam(name = "platformChannel", value = "服务类型"
                    , required = true, dataType = "String", paramType = "path")})
    @GetMapping(value = "/my/pay-channel-params/apps/{appId}/platform-channels/{platformChannel}")
    public List<PayChannelParamDTO> queryPayChannelParam(@PathVariable("appId") String appId, @PathVariable("platformChannel") String platformChannel) {
        return payChannelService.queryPayChannelParamByAppAndPlatform(appId, platformChannel);
    }

    @ApiOperation("获取指定应用指定服务类型下所包含的某个原始支付参数")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "appId", value = "应用id"
                    , required = true, dataType = "String", paramType = "path"),
            @ApiImplicitParam(name = "platformChannel", value = "平台支付渠道编码"
                    , required = true, dataType = "String", paramType = "path"),
            @ApiImplicitParam(name = "payChannel", value = "实际支付渠道编码"
                    , required = true, dataType = "String", paramType = "path")})
    @GetMapping(value = "/my/pay-channel-params/apps/{appId}/platform-channels/{platformChannel}/pay-channels/{payChannel}")
    public PayChannelParamDTO queryPayChannelParam(@PathVariable("appId") String appId
            , @PathVariable String platformChannel, @PathVariable("payChannel") String payChannel) {
        return payChannelService.queryParamByAppPlatformAndPayChannel(appId, platformChannel, payChannel);
    }
}
