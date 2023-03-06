package com.shanjupay.transaction.controller;


import com.alibaba.fastjson.JSON;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.AmountUtil;
import com.shanjupay.common.util.EncryptUtil;
import com.shanjupay.common.util.IPUtil;
import com.shanjupay.common.util.ParseURLPairUtil;
import com.shanjupay.merchant.api.AppService;
import com.shanjupay.merchant.api.dto.AppDTO;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import com.shanjupay.transaction.api.TransactionService;
import com.shanjupay.transaction.api.dto.PayOrderDTO;
import com.shanjupay.transaction.convert.PayOrderConvert;
import com.shanjupay.transaction.vo.OrderConfirmVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@Slf4j
public class PayController {
    @Reference
    private AppService appService;
    @Resource
    private TransactionService transactionService;
    /*** 支付入口
     北京市昌平区建材城西路金燕龙办公楼一层 电话：400-618-9090
     * @param ticket
     * * @param type
     * * @param request
     * * @return */
    @RequestMapping(value = "/pay-entry/{ticket}")
    public String payEntry(@PathVariable("ticket") String ticket, HttpServletRequest request) throws Exception {
        //解析ticket
        String s = EncryptUtil.decodeUTF8StringBase64(ticket);
        PayOrderDTO payOrderDTO=JSON.parseObject(s,PayOrderDTO.class);
        //将对象转成url格式
        String params = ParseURLPairUtil.parseURLPair(payOrderDTO);
        //分析为那个客户端
        BrowserType browserType = BrowserType.valueOfUserAgent(request.getHeader("user-agent"));
        switch (browserType){
            case ALIPAY:
                return "forward:/pay-page?"+params;
            case WECHAT:
                return "forward:/pay-page?"+params;
            default:
        }
        return "forward:/pay-error";
    }
    @ApiOperation("支付宝门店下单付款")
    @PostMapping("/createAliPayOrder")
    public void createAlipayOrderForStore(OrderConfirmVO orderConfirmVO, HttpServletRequest request
            , HttpServletResponse response) throws Exception {
        if (StringUtils.isBlank(orderConfirmVO.getAppId())) {
            throw new BusinessException(CommonErrorCode.E_300003);
        }
        PayOrderDTO payOrderDTO = PayOrderConvert.INSTANCE.vo2dto(orderConfirmVO);
        payOrderDTO.setTotalAmount(Integer.valueOf(AmountUtil.changeY2F(orderConfirmVO.getTotalAmount())) );
        payOrderDTO.setClientIp(IPUtil.getIpAddr(request));
        //获取下单应用信息
        AppDTO app = appService.selectByAId(payOrderDTO.getAppId());
        //设置所属商户
        payOrderDTO.setMerchantId(app.getMerchantId());
        //下单和调用支付宝下单接口
        PaymentResponseDTO paymentResponseDTO = transactionService.submitOrderByAli(payOrderDTO);
        String content = String.valueOf(paymentResponseDTO.getContent());
        //返回h5页面给前端
        log.info("支付宝H5支付响应的结果：" + content);
        response.setContentType("text/html;charset=UTF‐8");
        response.getWriter().write(content);
        response.getWriter().flush();
        response.getWriter().close();
    }
}

