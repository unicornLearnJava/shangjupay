package com.shanjupay.paymentagent;


import com.shanjupay.paymentagent.api.PayChannelAgentService;
import com.shanjupay.paymentagent.api.conf.AliConfigParam;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@SpringBootTest
@RunWith(SpringRunner.class)
public class test {
    @Resource
    PayChannelAgentService payChannelAgentService;
    @Test
    public void testQueryPayOrderByAli(){
        String APP_ID = "2021000121629978";
        String APP_PRIVATE_KEY = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCcK9vtNnhu8AL7Y36E+BKY3DeK7JxgFToUsHt7RuP8WvHERzcu89DWRjyF0su0CDZNNles1h1o35vUly8XyKv34NBotR1kiOi7YloWX0DwRxDx18Elce1kvQRU5buFff23kw6OgCiQCvDsx3E8FMWqp///S8guYZ6oV7OIToF6QQN2tDQ3Bfs59Nfs+JsBIi3xIn5IJ3Xm6RmpO0ERbEJLfQCjwH1RPT+DoM+HbmTSG4v1/ITnv/TjPih1xrtc6uWt4yJLsTf2EuCKz4yxp3H6cX/3Sr6t6ouax/bpfNg1vcKx+LFrbjz+5ZHX83TiGGICuOVIEZHf5q1gtKIgJHLXAgMBAAECggEBAJT5jbkSTf5dvCp8tbM4Rd1FrUI8BcvuU9Jqq9BkRpVzV8/Js415jgzpQVDTTfh2IP60yRwZwEJLlIZOleDVT56M+aH4q4o8w+4EmzVeNj8/O53IF/qVXETNvnUSFTj5f+7/PFKZVJhbp4nnk1Ah7JGn4x9w9guwmb2ys5NieEbagXxQsvu/YCcTvy3phk6476GLL7sbP/igqge+7O9r5s8qiY55Jczd1Gdg2Xk2ZYWVveRemN8EcrYKu4xVN9K6XcZYwuR2kZxpaKBhSU9sH5VN6lUSmQS3qX1BmZRH2Gxye7FUd2FxqrOkBz2jKDfP3tYkriAflwWWTxxY5ZtqsukCgYEA1oZGVWic5+Wpqt1UY74RoRJHWMtOHmtHyGBsbBCaKMxE78qkzdg5F4wwD1CJai+4nESDxgc7IbMY4FSx++bPVNxVI5Yfrb6LhXthX1ZOKq/OAxPV+DE9/oXIKzUZMfZ5v95ZdWQeRXTMJgynCCpVDT+Ofoy+rvxuPR7AjZ3FpzUCgYEAul1wOk8Mwbdh0uOV9vq8VVmgJWzRTZtH8Jkuu0LmilK0x+Kmg2QmdCrd+GI8OcxK3mNHHmNCUi5Gy8V3lNiZ6EICpR7TX4Lsd2sQ6+fF3NCH+tnxq0NOVL2mnBoiFl9CZ7/+nBMRj28UCF4ZPC9zu2ecLCnTUEhgEz5jAsGzV1sCgYB1BLe2/SKh6Ig9mQ/gfJyXNSZQDE5nj3OPnPFnnULKQGrWNr7hPjIeYy0QHYlnr/JO45zR173sqwpFzE1uMEAmdS3IBKrxFoB65ot9+tEWveFtklNkWFAN16IR15wPwfg1ri80NtHAZ264zqwKazULzgCHjXSydSYZFv5IAakAhQKBgQC09Y3H0II+h2ZBdCNl0kmVRpWuQV9qz+CmytcJlghtP6myfW3aHyEHo6gtSpXYOXppPlUMOK1pRu4PZwqaSUgIsjAHTUtJbsNVoWyo5EDIDOQ8u+thtNUWi6B/6xA2UQEV5OPBgPKIJ/93sIcoXZsu2YhXibVaOyyvAuD5CESwCQKBgCzywKAeqpA6DB+U9EKTqZadW6H6ULQjicfuuTYAZWTunO25gegvyBnLDu8J7V9qIkUOzpDZbXnOuaiKAazJkn1gXM1a85spZNieHD7vRMltx+g4NC5MUYq8EMNd46RllnZwXaaQMhL6Z4ZlCO90TkIfYeAYOIae7eT5TzgvDrlh";
        String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgQTA9sIwII132lNJnWQrDU5wMYxdXy4PN2gvmxhn+eDB8V/8bIRkHoVmEYOew4bVghT1snwyw+SRL1Wlfzv67mxnMothVhigy8nXUddLj/TScEv9bIHMA8q8dhQQkJg4FPqAPcvvKXXZfZdDhKBzahw2UHQkwSGaWrEvIXPzS7TFn4KYD+RoT7r3Xt8cNK7RbbRdBA3vkOuwt2ABL3HESg4mYnpuwa94o4PARpPDOYGFdip1kSYWmrByiu1pUKJESHSBbsZjXKO2Ezib8WxiWISRLfGCzk6FvhHOyL8ZywjJ/ffAR1dIlf1+phKTk3SV6pAE7yPWS4rjQZLmYjmJjQIDAQAB";
        String CHARSET = "UTF-8";
        String serverUrl = "https://openapi.alipaydev.com/gateway.do";//正 式"https://openapi.alipay.com/gateway.do"
        // 支付渠道参数
        AliConfigParam aliConfigParam = new AliConfigParam();
        aliConfigParam.setUrl(serverUrl);
        aliConfigParam.setCharest(CHARSET);

        aliConfigParam.setAlipayPublicKey(ALIPAY_PUBLIC_KEY);
        aliConfigParam.setRsaPrivateKey(APP_PRIVATE_KEY);
        aliConfigParam.setAppId(APP_ID);
        aliConfigParam.setFormat("json");
        aliConfigParam.setSigntype("RSA2");
        PaymentResponseDTO paymentResponseDTO = payChannelAgentService.queryPayOrderByAli(aliConfigParam, "SJ1553243842201051136"); System.out.println(paymentResponseDTO);
    }
}
