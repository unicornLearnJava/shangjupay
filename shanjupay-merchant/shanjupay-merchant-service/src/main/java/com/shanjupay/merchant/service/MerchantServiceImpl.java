package com.shanjupay.merchant.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.domain.PageVO;
import com.shanjupay.common.util.PhoneUtil;
import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.api.dto.StaffDTO;
import com.shanjupay.merchant.api.dto.StoreDTO;
import com.shanjupay.merchant.api.dto.StoreStaffDTO;
import com.shanjupay.merchant.convert.MerchantConvert;
import com.shanjupay.merchant.convert.StaffConvert;
import com.shanjupay.merchant.convert.StoreConvert;
import com.shanjupay.merchant.entity.Merchant;
import com.shanjupay.merchant.entity.Staff;
import com.shanjupay.merchant.entity.Store;
import com.shanjupay.merchant.entity.StoreStaff;
import com.shanjupay.merchant.mapper.MerchantMapper;
import com.shanjupay.merchant.mapper.StaffMapper;
import com.shanjupay.merchant.mapper.StoreMapper;
import com.shanjupay.merchant.mapper.StoreStaffMapper;
import com.shanjupay.user.api.TenantService;
import com.shanjupay.user.api.dto.tenant.CreateTenantRequestDTO;
import com.shanjupay.user.api.dto.tenant.TenantDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by Administrator.
 */
@org.apache.dubbo.config.annotation.Service
@Slf4j
public class MerchantServiceImpl implements MerchantService {

    @Autowired
    MerchantMapper merchantMapper;
    @Resource
    private StoreMapper storeMapper;
    @Resource
    private StaffMapper staffMapper;
    @Resource
    private StoreStaffMapper storeStaffMapper;
    @Reference
    private TenantService tenantService;


    //测试api
    @Override
    public MerchantDTO queryMerchantById(Long id) {
        Merchant merchant = merchantMapper.selectById(id);

        return MerchantConvert.INSTANCE.entity2dto(merchant);
    }

    //创建商户
    @Override
    public MerchantDTO createMerchant(MerchantDTO merchantDTO) throws BusinessException{
        //校验参数的合法性
        if(merchantDTO == null){
            throw new BusinessException(CommonErrorCode.E_100108);
        }
        if(StringUtils.isBlank(merchantDTO.getMobile())){
            throw new BusinessException(CommonErrorCode.E_100112);
        }
        //手机号格式校验
        if(!PhoneUtil.isMatches(merchantDTO.getMobile())){
            throw new BusinessException(CommonErrorCode.E_100109);
        }
        //联系人非空校验
        if (StringUtils.isBlank(merchantDTO.getUsername())) {
            throw new BusinessException(CommonErrorCode.E_100110);
        }
        //密码非空校验
        if (StringUtils.isBlank(merchantDTO.getPassword())) {
            throw new BusinessException(CommonErrorCode.E_100111);
        }
        //校验手机号的唯一性
        //根据手机号查询商户表，如果存在记录则说明手机号已存在
        Integer count = merchantMapper.selectCount(new LambdaQueryWrapper<Merchant>().eq(Merchant::getMobile, merchantDTO.getMobile()));
        if(count>0){
            throw new BusinessException(CommonErrorCode.E_100113);
        }


        //2.添加租户 和账号 并绑定关系
        CreateTenantRequestDTO dto = new CreateTenantRequestDTO();
        dto.setName(merchantDTO.getUsername());
        //表示该租户类型是商户
        dto.setTenantTypeCode("shanju‐merchant");
        //初始化套餐
        dto.setBundleCode("shanju-merchant");
        //租户的账号信息
        dto.setMobile(merchantDTO.getMobile());
        dto.setPassword(merchantDTO.getPassword());
        dto.setUsername(merchantDTO.getUsername());
        //新增租户并设置为管理员
        dto.setName(merchantDTO.getUsername());
        /*** 创建租户如果已存在租户则返回租户信息，否则新增租户、新增租户管理员，同时初始化权限
         * * 1.若管理员用户名已存在，禁止创建
         * * 2.手机号已存在，禁止创建
         * * 3.创建根租户对应账号时，需要手机号，账号的用户名密码
         * * @param createTenantRequest 创建租户请求信息
         * * @return */
        TenantDTO tenantAndAccount = tenantService.createTenantAndAccount(dto);
        if(tenantAndAccount == null || tenantAndAccount.getId() == null){
            throw new BusinessException(CommonErrorCode.E_110009);
        }

        //判断租户下的商户是否唯一
        Long tenantAndAccountId = tenantAndAccount.getId();
        Merchant merchant1 = merchantMapper.selectOne(new LambdaQueryWrapper<Merchant>().eq(Merchant::getTenantId, tenantAndAccountId));
        if(merchant1 != null && merchant1.getId() != null){
            throw new BusinessException(CommonErrorCode.E_200017);
        }

        Merchant merchant = MerchantConvert.INSTANCE.dto2entity(merchantDTO);
        //设置商户所属的租户
        merchant.setTenantId(tenantAndAccountId);
        merchant.setAuditStatus("0");
        merchantMapper.insert(merchant);


        //新增门店
        StoreDTO storeDTO = new StoreDTO();
        storeDTO.setMerchantId(merchant.getId());
        storeDTO.setStoreName("根门店");
        storeDTO = createStore(storeDTO);
        log.info("门店信息：{}" + JSON.toJSONString(storeDTO));
        //新增员工
        StaffDTO staffDTO = new StaffDTO();
        staffDTO.setMerchantId(merchant.getId());
        staffDTO.setMobile(merchantDTO.getMobile());
        staffDTO.setUsername(merchantDTO.getUsername());

        //为员工选择归属门店,此处为根门店
        staffDTO.setStoreId(storeDTO.getId());
        staffDTO = createStaff(staffDTO);
        //6.为门店设置管理员
        BinDingStore2StoreStaff(storeDTO.getId(), staffDTO.getId());
        return MerchantConvert.INSTANCE.entity2dto(merchant);
    }

    /*
      商户资质申请
       merchantId   商户id
       merchantDTO  商户资质申请信息
     */
    @Override
    public void applyMerchant(Long merchantId, MerchantDTO merchantDTO) throws  BusinessException{
        //条件判断
        if(merchantId ==null || merchantDTO == null){
            throw new BusinessException(CommonErrorCode.E_100108);
        }
        //查询商户是否已注册
        Merchant merchant = merchantMapper.selectById(merchantId);
        if(merchant == null){
            throw  new BusinessException(CommonErrorCode.E_200002);
        }
        //对象转换为Merchant
        Merchant merchant1 = MerchantConvert.INSTANCE.dto2entity(merchantDTO);
        //更新商户对象状态为审核通过
        merchant1.setId(merchant.getId());
        merchant1.setAuditStatus("1");
        merchant1.setTenantId(merchant.getTenantId());//租户id
        //更新商户对象
        merchantMapper.updateById(merchant1);
    }


    //新增门店
    @Override
    public StoreDTO createStore(StoreDTO storeDTO) throws BusinessException {
        Store store = StoreConvert.INSTANCE.dto2entity(storeDTO);
        log.info("商户下新增门店"+ JSON.toJSONString(store));
        storeMapper.insert(store);
        return StoreConvert.INSTANCE.entity2dto(store);
    }
    //新增员工
    @Override
    public StaffDTO createStaff(StaffDTO staffDTO) {
        String mobile = staffDTO.getMobile();
        Long merchantId = staffDTO.getMerchantId();
        String username = staffDTO.getUsername();

        //校验手机号  不能为空且同一商户下唯一
        if(StringUtils.isBlank(mobile)){
            throw new BusinessException(CommonErrorCode.E_100112);
        }
        if(isExistStaffByMobile(mobile,merchantId)){
            throw new BusinessException(CommonErrorCode.E_100113);
        }

        //校验账号   不能为空且同一商户下唯一
        if(StringUtils.isBlank(username)){
            throw new BusinessException(CommonErrorCode.E_100110);
        }
        if(isExistStaffByUserName(username,merchantId)){
            throw new BusinessException(CommonErrorCode.E_100114);
        }
        //新增员工
        Staff staff = StaffConvert.INSTANCE.dto2entity(staffDTO);
        staffMapper.insert(staff);
        return StaffConvert.INSTANCE.entity2dto(staff);
    }
    //绑定员工与门店的关系
    @Override
    public void BinDingStore2StoreStaff(Long storeId, Long staffId) {
        StoreStaff storeStaff=new StoreStaff();
        storeStaff.setStoreId(storeId);
        storeStaff.setStaffId(staffId);
        storeStaffMapper.insert(storeStaff);
    }

    //根据租户查询商户信息
    @Override
    public MerchantDTO queryMerchantByTenantId(Long tenantId) {
        Merchant merchant = merchantMapper.selectOne(new QueryWrapper<Merchant>().lambda().
                eq(Merchant::getTenantId, tenantId));
        return MerchantConvert.INSTANCE.entity2dto(merchant);
    }

    @Override
    public PageVO<StoreDTO> queryStoreByPage(StoreDTO storeDTO, Integer pageNo, Integer pageSize) {
        //构造分页对象
        Page page=new Page(pageNo,pageSize);
        LambdaQueryWrapper<Store> wrapper=new LambdaQueryWrapper<>();
        //参数判断
        if(null !=storeDTO && null !=storeDTO.getMerchantId()){

            wrapper.eq(Store::getMerchantId,storeDTO.getMerchantId());
        }
        IPage iPage = storeMapper.selectPage(page, wrapper);
        //获取结果
        List<Store> records = iPage.getRecords();
        List<StoreDTO> storeDTOS = StoreConvert.INSTANCE.listentity2dto(records);
        //获取结果总数
        long total = iPage.getTotal();
        return new PageVO<>(storeDTOS,total,pageNo,pageSize);
    }

    @Override
    public Boolean queryStoreInMerchant(Long storeId, Long merchantId) {
        LambdaQueryWrapper<Store> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(Store::getId,storeId).eq(Store::getMerchantId,merchantId);
        Integer integer = storeMapper.selectCount(wrapper);
        return integer>0;
    }

    /*** 根据手机号判断员工是否已在指定商户存在
     * * @param mobile 手机号
     * * @return */
    private boolean isExistStaffByMobile(String mobile, Long merchantId) {
        Staff staff = staffMapper.selectOne(new LambdaQueryWrapper<Staff>()
                .eq(Staff::getMobile, mobile)
                .eq(Staff::getMerchantId, merchantId));
        if(staff != null){
            return true;
        }
        return false;
    }

    /*** 根据账号判断员工是否已在指定商户存在
     * * @param userName
     * * @param merchantId
     * * @return */
     private boolean isExistStaffByUserName(String userName, Long merchantId) {
         Staff staff = staffMapper.selectOne(new LambdaQueryWrapper<Staff>()
                 .eq(Staff::getUsername, userName)
                 .eq(Staff::getMerchantId, merchantId));
         if(staff != null){
             return true;
         }
         return false;
     }
}
