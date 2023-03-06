package com.shanjupay.transaction.api;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import com.shanjupay.transaction.api.dto.PayOrderDTO;
import com.shanjupay.transaction.api.dto.QRCodeDto;


//交易服务接口
public interface TransactionService {
    /*** 生成门店二维码
     * * @param qrCodeDto，传入merchantId,appId、storeid、channel、subject、body
     * * @return 支付入口URL，将二维码的参数组成json并用base64编码
     * * @throws BusinessException */
    String createStoreQRCode(QRCodeDto qrCodeDto) throws BusinessException;

    PaymentResponseDTO submitOrderByAli(PayOrderDTO payOrderDTO)throws BusinessException;
    void updateOrderTradeNoAndTradeState(String tradeNo, String payChannelTradeNo, String state);
}
