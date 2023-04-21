package plus.carlosliu.pineconemall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.aspectj.bridge.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.common.utils.Query;

import plus.carlosliu.common.utils.R;
import plus.carlosliu.pineconemall.ware.dao.WareInfoDao;
import plus.carlosliu.pineconemall.ware.entity.WareInfoEntity;
import plus.carlosliu.pineconemall.ware.feign.MemberFeignService;
import plus.carlosliu.pineconemall.ware.service.WareInfoService;
import plus.carlosliu.pineconemall.ware.to.MemberAddressTo;
import plus.carlosliu.pineconemall.ware.vo.FareVo;

@Transactional
@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    private MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                new QueryWrapper<WareInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        LambdaQueryWrapper<WareInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        String key = (String) params.get("key");
        queryWrapper.eq(!StringUtils.isEmpty(key), WareInfoEntity::getId, key)
                .or().like(!StringUtils.isEmpty(key), WareInfoEntity::getName, key)
                .or().like(!StringUtils.isEmpty(key), WareInfoEntity::getAddress, key)
                .or().like(!StringUtils.isEmpty(key), WareInfoEntity::getAreacode, key);
        IPage<WareInfoEntity> page = this.page(new Query<WareInfoEntity>().getPage(params), queryWrapper);
        return new PageUtils(page);
    }

    @Override
    public FareVo getFare(Long addrId) {
        FareVo fareVo = new FareVo();
        R r = memberFeignService.addrInfo(addrId);
        if (r.getCode() == 0){
            MemberAddressTo address = r.getData("memberReceiveAddress", new TypeReference<MemberAddressTo>() {});
            if (address != null){
                // TODO:结合第三方接口获取运费 未实现
                fareVo.setFare(new BigDecimal("10.0"));
                fareVo.setAddress(address);
                return fareVo;
            }
        }
        return null;
    }

}