package com.shanjupay.merchant.service.Impl;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.QiniuUtils;
import com.shanjupay.merchant.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sun.awt.SunHints;


@Service
public class FileServiceImpl implements FileService {
    @Value("${oss.qiniu.url}")
    private String url;
    @Value("${oss.qiniu.accessKey}")
    private String accessKey;
    @Value("${oss.qiniu.secretKey}")
    private String secretKey;
    @Value("${oss.qiniu.bucket}")
    private String bucket;

    @Override
    public String upload(byte[] bytes, String fileName) {


//        key 文件名
//        accessKey
//                secretKey
//        bytes 文件字节
//        bucket 空间名
        try {
            QiniuUtils.Upload(fileName, accessKey, secretKey, bytes, bucket);
        } catch (Exception e){
            throw new BusinessException(CommonErrorCode.E_100106);
        }
        return url+fileName;
    }
}
