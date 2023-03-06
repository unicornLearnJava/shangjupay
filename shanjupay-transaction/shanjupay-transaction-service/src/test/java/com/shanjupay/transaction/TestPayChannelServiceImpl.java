package com.shanjupay.transaction;


import com.shanjupay.transaction.api.PayChannelService;
import com.shanjupay.transaction.api.dto.PayChannelDTO;
import com.shanjupay.transaction.mapper.PlatformChannelMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class TestPayChannelServiceImpl {
    @Resource
    private PayChannelService payChannelService;
    //测试查看指定应用下的支付渠道接口
    @Test
    public void queryPayChannelByPlatformChannel(){
        List<PayChannelDTO> shanjupay_c2b = payChannelService.queryPayChannelByPlatformChannel("shanju_c2b");
        System.out.println(shanjupay_c2b);
    }

}
