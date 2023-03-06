package com.shanjupay.merchant.controller;

import com.shanjupay.merchant.api.AppService;
import com.shanjupay.merchant.api.dto.AppDTO;
import com.shanjupay.merchant.common.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api("商户应用平台-应用管理")
@RestController
public class AppController {
    @Reference
    private AppService appService;

    @ApiOperation("创建应用")
    @ApiImplicitParam(name = "appDTO",value = "应用信息"
            ,required = true,dataType = "AppDTO",paramType = "body")
    @PostMapping("/my/apps")
    public AppDTO createApp(@RequestBody AppDTO appDTO){
        Long merchantId = SecurityUtil.getMerchantId();
        return appService.createAPP(merchantId, appDTO);
    }

    @ApiOperation("查询商户下的应用列表")
    @GetMapping(value = "my/apps")
    public List<AppDTO> queryMyApps() {
        Long merchantId = SecurityUtil.getMerchantId();
        return appService.selectByMId(merchantId);
    }
    @ApiOperation("根据appid获取应用的详细信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "appId", value = "商户应用id"
                    , required = true, dataType = "String", paramType = "path")})
    @GetMapping(value = "/my/apps/{appId}")
    public AppDTO getApp(@PathVariable String appId) {
        return appService.selectByAId(appId);
    }
}
