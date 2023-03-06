package com.shanjupay.transaction.controller;


import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class PayTestController {

    String APP_ID="2021000121629978";
    String APP_PRIVATE_KEY="MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC9aQl/lFntkVIEKdnq0TsboUKx0CGM4ISzzIrByXIfYfYU6F0tlbURT3XmF8rOgqnE6rdVlpnT6/nL1tHQ8t22B2tHs9HAhs/Zj1c5vyZSb5NzTUYFLCuWoQjhu1bc072/X7zbMLUbmWRZte9f6GhggQUg7qZRXDFRDfctIn9xufEphZ38Yli+i2+NWCOYiA96YhEWxYCTeHpw1j22luyk79NGT0S3rO/M0GtHhUC4CIiQzZTIdyFsXvImsiFakL5euaTHqgxDWZhSBqMEbU29vT/Qu1UDeh86bO2TSqtmmCgfXXmvgpGbiH+/Bm2k7IQX+/zOuWtBDgxZo50gNdTdAgMBAAECggEBAIxFuLISFNNalunccLV1RJf+wlzbLa2eLMjXRz+ix3C0ZMExIKh8wX7TYRYJMrnjjIT36tRo1v1aOkGakwJyin2fWvTcGA/EBzQw8sEibegJF6MBbESyZImGMwSxb+/UpCaRaVTO6qFI5UeG9IMLpBksE7dC5ktEwoUrmY2EieuXKJlVJ/bMtFtZeLG5igeAniWaFd3chzL7OSw/PHk39HUYH+oToBRWU5K0XNxUd09KqV2IZLT/Mnxyz8i5l6tfv2mKd8T/dp9ZhrQpZtbGNE4ZSukBXUdb+FQMY6ubwmJbC4Dh8coewreql6DUdC0yxp5uFxCn0PNOAWh5LFUWd8ECgYEA8WCfQoLdMysnNv2OposdEJTptYNLH6hb6W47Y3N/IFtEPiadTtdJsAmwZYH8Rb4V6GrVIAZpOB8bKQrpxlY86rqX3FJZTnmqroQL8rzcz4ou5fl/zjRwuNmc8ZYIrgkvAtD5Gt/w5kYuA7fhFPY5bW0PtbA+4o6gT7E5b0kCZbECgYEAyOJ86Y+C7OUsmGo4dKUCnUNt/aMBFF8cxtA1sY5XthupoCg3SiCDcgM7c9T2Hiy3JB3L17zxnbriUkJ5hoAbtkQDu3mjqMEBfx44Z/GKqggAQTJOf2TpXS3kuwiFnVtmG7u5yb3Ga7xpQGxf32rOTmBWUIvHQ8zdapgPhBVUsO0CgYAPKrkXHs3zZyTBqdEK2fbrkc/VnfhcduG/mzOUHi/AwrtZBGgiChkRPKqe1joZz8XkosHdj0mRQzMbsxnZYpXVUgA00d/WtIJrENUHXaKqT85+mNzVAiqLEvFvpLPK67zOen8Ml0G11ncRuL4l6QrWu2GfUwHAjqG4IDSxr269IQKBgFjLzJ1NI3lln/3LTiM5YVt0l+T6rZp8pHK5TFKs8dZ8idFSlYX9szytcxUzeItEBrw/M4Csczzeg/YKjj9G13kMvX2cOhx5HBk9mX9QxJN+L +ahvxMO3xyiEa4fCTjSUuNFas1jRpcO1JN2lDZktsbsTJsT/kPZBbJ9frZK24HNAoGAZ3/mKfke4i5s+kjz8FfMKj/rCTcOA63TATaiX/UpLUkEji38Q4CMjk+6fTZAzKgxKJhFXT1pdPDKSjgLAn1t95fTPfCeOz/6EMSUg1RWdZbUi0rOY5ysdLLcbu+VMXNrNe3xJLc48dyunafrYDpD+xbSauyWDkAW6GT20KTd+gc=";
    String CHARSET = "utf-8";
    String ALIPAY_PUBLIC_KEY="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgQTA9sIwII132lNJnWQrDU5wMYxdXy4PN2gvmx" +
            "hn+eDB8V/8bIRkHoVmEYOew4bVghT1snwyw+SRL1Wlfzv67mxnMothVhigy8nXUddLj/TScEv9bIHMA8q8dhQQkJg4FPqAPc" +
            "vvKXXZfZdDhKBzahw2UHQkwSGaWrEvIXPzS7TFn4KYD+RoT7r3Xt8cNK7RbbRdBA3vkOuwt2ABL3HESg4mYnpuwa94o4PARp" +
            "PDOYGFdip1kSYWmrByiu1pUKJESHSBbsZjXKO2Ezib8WxiWISRLfGCzk6FvhHOyL8ZywjJ/ffAR1dIlf1+phKTk3SV6pAE7" +
            "yPWS4rjQZLmYjmJjQIDAQAB";
    String serverUrl = "https://openapi.alipaydev.com/gateway.do"; //正 式"https://openapi.alipay.com/gateway.do"

    @GetMapping("/alipaytest")
    public void doPost(HttpServletRequest httpRequest,
                       HttpServletResponse httpResponse) throws ServletException, IOException {
        AlipayClient alipayClient = new DefaultAlipayClient(serverUrl, APP_ID, APP_PRIVATE_KEY, "json", CHARSET, ALIPAY_PUBLIC_KEY, "RSA2"); //获得初始化的AlipayClient
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();//创建API对应的request
//        alipayRequest.setReturnUrl("http://domain.com/CallBack/return_url.jsp");
//        alipayRequest.setNotifyUrl("http://domain.com/CallBack/notify_url.jsp");//在公共参数中设置回跳和通知地址
        alipayRequest.setBizContent("{" +
                " \"out_trade_no\":\"20150320010101006\"," +
                " \"total_amount\":\"999\"," +
                " \"subject\":\"Iphone6 16G\"," +
                " \"product_code\":\"QUICK_WAP_WAY\"" +
                " }");//填充业务参数
        String form="";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        httpResponse.setContentType("text/html;charset=" + CHARSET);
        httpResponse.getWriter().write(form);//直接将完整的表单html输出到页面
        httpResponse.getWriter().flush();
        httpResponse.getWriter().close();
    }
}
