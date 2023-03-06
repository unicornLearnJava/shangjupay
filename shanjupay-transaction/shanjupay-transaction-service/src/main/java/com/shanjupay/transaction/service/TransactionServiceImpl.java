package com.shanjupay.transaction.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.AmountUtil;
import com.shanjupay.common.util.EncryptUtil;
import com.shanjupay.common.util.PaymentUtil;
import com.shanjupay.merchant.api.AppService;
import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.paymentagent.api.PayChannelAgentService;
import com.shanjupay.paymentagent.api.conf.AliConfigParam;
import com.shanjupay.paymentagent.api.dto.AlipayBean;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import com.shanjupay.transaction.api.PayChannelService;
import com.shanjupay.transaction.api.TransactionService;
import com.shanjupay.transaction.api.dto.PayChannelParamDTO;
import com.shanjupay.transaction.api.dto.PayOrderDTO;
import com.shanjupay.transaction.api.dto.QRCodeDto;
import com.shanjupay.transaction.convert.PayOrderConvert;
import com.shanjupay.transaction.entity.PayOrder;
import com.shanjupay.transaction.mapper.PayOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


@Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {
    @Value("${shanjupay.payurl}")
    private String payUrl;
    @Reference
    private MerchantService merchantService;
    @Reference
    private AppService appService;
    @Resource
    private PayOrderMapper payOrderMapper;
    @Resource
    private PayChannelService payChannelService;
    @Reference
    private PayChannelAgentService payChannelAgentService;

    //生成二维码url
    @Override
    public String createStoreQRCode(QRCodeDto qrCodeDto) throws BusinessException {
        //校验参数合法性   应用、门店是否属于该商户
        verifyAppAndStore(qrCodeDto.getMerchantId(), qrCodeDto.getAppId(), qrCodeDto.getStoreId());
        //1. 生成支付信息
        PayOrderDTO payOrderDTO = new PayOrderDTO();
        payOrderDTO.setMerchantId(qrCodeDto.getMerchantId());
        payOrderDTO.setAppId(qrCodeDto.getAppId());
        payOrderDTO.setStoreId(qrCodeDto.getStoreId());
        payOrderDTO.setSubject(qrCodeDto.getSubject());
        //显示订单标题
        payOrderDTO.setChannel("shanju_c2b");
        // 服务类型
        payOrderDTO.setBody(qrCodeDto.getBody());
        //订单内容
        String jsonString = JSON.toJSONString(payOrderDTO);
        log.info("transaction service createStoreQRCode,JsonString is {}", jsonString);
        //将支付信息保存到票据中
        String ticket = EncryptUtil.encodeUTF8StringBase64(jsonString);
        //支付入口
        String payEntryUrl = payUrl + ticket;
        log.info("transaction service createStoreQRCode,pay-entry is {}", payEntryUrl);
        return payEntryUrl;
    }

    /*** 支付宝订单保存
     * ** @param payOrderDTO
     * * @return */
    @Override
    public PaymentResponseDTO submitOrderByAli(PayOrderDTO payOrderDTO) throws BusinessException {
        //保存订单
        payOrderDTO.setPayChannel("ALIPAY_WAP");
        //保存订单
        PayOrderDTO save = save(payOrderDTO);
        //调用支付代理服务请求第三方支付系统

        PaymentResponseDTO paymentResponseDTO = alipayH5(save.getTradeNo());
        return paymentResponseDTO;
    }

    /*** 更新订单支付状态
     * ** @param tradeNo 闪聚平台订单号
     * * @param payChannelTradeNo 支付宝或微信的交易流水号
     * * @param state 订单状态 交易状态支付状态,0‐订单生成,1‐支付中(目前未使用),2‐支付成 功,4‐关闭 5‐‐失败 */
    @Override
    public void updateOrderTradeNoAndTradeState(String tradeNo, String payChannelTradeNo, String state) {
        final LambdaUpdateWrapper<PayOrder> lambda = new UpdateWrapper<PayOrder>().lambda();
        lambda.eq(PayOrder::getTradeNo, tradeNo).set(PayOrder::getPayChannelTradeNo, payChannelTradeNo)
                .set(PayOrder::getTradeState, state);
        if (state != null && "2".equals(state)) {
            lambda.set(PayOrder::getPaySuccessTime, LocalDateTime.now());
        }
        lambda.set(PayOrder::getPaySuccessTime,LocalDateTime.now().toString());
        payOrderMapper.update(null, lambda);
    }

    //公用保存订单
    private PayOrderDTO save(PayOrderDTO payOrderDTO) {
        PayOrder entity = PayOrderConvert.INSTANCE.dto2entity(payOrderDTO);
        entity.setTradeNo(PaymentUtil.genUniquePayOrderNo());
        //订单创建时间
        entity.setCreateTime(LocalDateTime.now());
        //设置过期时间，30分钟
        entity.setExpireTime(LocalDateTime.now().plus(30, ChronoUnit.MINUTES));
        entity.setCurrency("CNY");//设置支付币种
        entity.setTradeState("0");//订单状态
        payOrderMapper.insert(entity);
        return PayOrderConvert.INSTANCE.entity2dto(entity);
    }

    //公用调用支付代理服务请求第三方支付系统
    private PaymentResponseDTO alipayH5(String tradeNo) {
        //构建支付实体
        AlipayBean alipayBean = new AlipayBean();
        //根据订单号查询订单详情
        PayOrderDTO payOrderDTO = queryPayOrder(tradeNo);
        alipayBean.setOutTradeNo(tradeNo);
        alipayBean.setSubject(payOrderDTO.getSubject());
        String totalAmount = null;
        //支付宝那边入参是元
        try {
            //将分转成元
            totalAmount = AmountUtil.changeF2Y(payOrderDTO.getTotalAmount().toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(CommonErrorCode.E_300006);
        }

        alipayBean.setTotalAmount(totalAmount);
        alipayBean.setBody(payOrderDTO.getBody());
        alipayBean.setStoreId(payOrderDTO.getStoreId());
        alipayBean.setExpireTime("30m");
        //根据应用、服务类型、支付渠道查询支付渠道参数
        PayChannelParamDTO payChannelParamDTO = payChannelService.queryParamByAppPlatformAndPayChannel(payOrderDTO.getAppId(), payOrderDTO.getChannel(), "ALIPAY_WAP");
        if (payChannelParamDTO == null) {
            throw new BusinessException(CommonErrorCode.E_300007);
        }
        //支付宝渠道参数
        AliConfigParam aliConfigParam = JSON.parseObject(payChannelParamDTO.getParam(), AliConfigParam.class);
        //字符编码
        aliConfigParam.setCharest("utf-8");
        PaymentResponseDTO payOrderResponse = payChannelAgentService.createPayOrderByAliWAP(aliConfigParam, alipayBean);
        log.info("支付宝H5支付响应Content:" + payOrderResponse.getContent());
        return payOrderResponse;
    }
    //公用通过订单Id查询订单信息
    private PayOrderDTO queryPayOrder(String tradeNo) {

        PayOrder payOrder = payOrderMapper.selectOne(new LambdaQueryWrapper<PayOrder>().eq(PayOrder::getTradeNo, tradeNo));
        return PayOrderConvert.INSTANCE.entity2dto(payOrder);
    }

    //公用校验参数合法性
    private void verifyAppAndStore(Long merchantId, String appId, Long storeId) {
        Boolean aBoolean = appService.queryAppInMerchant(appId, merchantId);
        if(!aBoolean){
            throw new BusinessException(CommonErrorCode.E_200005);
        }
        Boolean aBoolean1 = merchantService.queryStoreInMerchant(storeId, merchantId);
        if(!aBoolean1){
            throw  new BusinessException(CommonErrorCode.E_200006);
        }
    }
}
