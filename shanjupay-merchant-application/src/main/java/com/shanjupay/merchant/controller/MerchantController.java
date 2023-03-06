package com.shanjupay.merchant.controller;


import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.PhoneUtil;
import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.common.util.SecurityUtil;
import com.shanjupay.merchant.convert.MerchantDetailConvert;
import com.shanjupay.merchant.convert.MerchantRegisterConvert;
import com.shanjupay.merchant.service.FileService;
import com.shanjupay.merchant.service.SMSService;
import com.shanjupay.merchant.vo.MerchantDetailVO;
import com.shanjupay.merchant.vo.MerchantRegisterVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.UUID;
import java.util.jar.JarOutputStream;

@Api(value = "商户应用平台-注册与申请资质")
@RestController
public class MerchantController {
    @Reference
    private MerchantService merchantService;
    @Resource
    private SMSService smsService;
    @Resource
    private FileService fileService;

    @RequestMapping("/merchants/{id}")
    public MerchantDTO test01(@PathVariable("id") Long id){
        MerchantDTO merchantDTO = merchantService.queryMerchantById(id);
        return merchantDTO;
    }


    @ApiOperation("获取登录用户的商户信息")
    @GetMapping(value="/my/merchants")
    public MerchantDTO getMyMerchantInfo(){
        //从token中获取商户id
        Long merchantId = SecurityUtil.getMerchantId();
        return merchantService.queryMerchantById(merchantId);
    }

    @ApiOperation("获取验证码")
    @ApiImplicitParam(name = "mobile",value = "手机号",required = true,dataType = "String")
    @GetMapping("sms")
    public String getSMS(@RequestParam("phone") String mobile){

        return smsService.getSMS(mobile);
    }

    @ApiOperation("商户注册")
    @ApiImplicitParam(name = "merchantRegisterVO",value = "注册信息"
            ,required = true,dataType = "MerchantRegisterVO",paramType = "body")
    @PostMapping("/merchants/register")
    public MerchantRegisterVO createMerchant(@RequestBody MerchantRegisterVO merchantRegisterVO){
        //校验参数的合法性
        if(merchantRegisterVO == null){
            throw new BusinessException(CommonErrorCode.E_100108);
        }
        if(StringUtils.isBlank(merchantRegisterVO.getMobile())){
            throw new BusinessException(CommonErrorCode.E_100112);
        }
        //手机号格式校验
        if(!PhoneUtil.isMatches(merchantRegisterVO.getMobile())){
            throw new BusinessException(CommonErrorCode.E_100109);
        }
        //校验验证码
        smsService.checkSMS(merchantRegisterVO.getVerifiykey(),merchantRegisterVO.getVerifiyCode());
        MerchantDTO merchantDTO = MerchantRegisterConvert.INSTANCE.vo2dto(merchantRegisterVO);
        //商户注册
        merchantService.createMerchant(merchantDTO);
        return merchantRegisterVO;
    }
    @ApiOperation("上传文件")
    @ApiImplicitParam(name="multipartFile",value = "上传的文件"
            ,required = true,dataType = "MultipartFile")
    @PostMapping("upload")
    public String upload(@RequestParam("file")MultipartFile multipartFile) throws IOException {
         //原始文件名称
        String originalFilename = multipartFile.getOriginalFilename();
        //文件后缀
        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".")-1);
        //文件名称
        String fileName = UUID.randomUUID().toString()+suffix;

        return fileService.upload(multipartFile.getBytes(),fileName);
    }
    @ApiOperation("商户资质申请")
    @ApiImplicitParam(name = "merchantDetailVO",value = "商户资质申请信息"
            ,required = true,dataType = "MerchantDetailVO",paramType = "body")
    @PostMapping("/my/merchants/save")
    public void save(@RequestBody MerchantDetailVO merchantDetailVO){
        //获取商户id
        Long merchantId = SecurityUtil.getMerchantId();
        System.out.println(merchantId);
        MerchantDTO merchantDTO = MerchantDetailConvert.INSTANCE.vo2dto(merchantDetailVO);
        merchantService.applyMerchant(merchantId,merchantDTO);
    }
}
