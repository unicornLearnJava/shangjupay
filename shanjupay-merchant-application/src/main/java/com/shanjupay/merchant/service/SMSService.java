package com.shanjupay.merchant.service;

import com.shanjupay.common.domain.BusinessException;

public interface SMSService {
    //获取验证码
    String getSMS(String mobile);
    //校验验证码
    void checkSMS(String verifiykey,String verifiyCode) ;
}
