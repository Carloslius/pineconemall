package plus.carlosliu.pineconemall.coupon.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import plus.carlosliu.common.to.MemberPrice;
import plus.carlosliu.common.to.SkuReductionTo;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.common.utils.Query;

import plus.carlosliu.pineconemall.coupon.dao.SkuFullReductionDao;
import plus.carlosliu.pineconemall.coupon.entity.MemberPriceEntity;
import plus.carlosliu.pineconemall.coupon.entity.SkuFullReductionEntity;
import plus.carlosliu.pineconemall.coupon.entity.SkuLadderEntity;
import plus.carlosliu.pineconemall.coupon.service.MemberPriceService;
import plus.carlosliu.pineconemall.coupon.service.SkuFullReductionService;
import plus.carlosliu.pineconemall.coupon.service.SkuLadderService;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    private SkuLadderService skuLadderService;
    @Autowired
    private MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuReduction(SkuReductionTo skuReductionTo) {
        // 6.4、保存sku的优惠、满减信息 pineconemall_sms ==》 sms_sku_ladder  sms_sku_full_reduction  sms_member_price
        // 1、 sms_sku_ladder
        SkuLadderEntity skuLadder = new SkuLadderEntity();
        BeanUtils.copyProperties(skuReductionTo, skuLadder);
        skuLadder.setAddOther(skuReductionTo.getCountStatus());
        if (skuReductionTo.getFullCount() > 0) {
            skuLadderService.save(skuLadder);
        }

        // 2、 sms_sku_full_reduction
        SkuFullReductionEntity skuFullReduction = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuReductionTo, skuFullReduction);
        skuFullReduction.setAddOther(skuReductionTo.getCountStatus());
        if (skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1) {
            baseMapper.insert(skuFullReduction);
        }

        // 3、 sms_member_price
        List<MemberPrice> memberPriceList = skuReductionTo.getMemberPrice();
        List<MemberPriceEntity> memberPriceEntities = memberPriceList.stream().map(item -> {
            MemberPriceEntity memberPrice = new MemberPriceEntity();
            memberPrice.setSkuId(skuReductionTo.getSkuId());
            memberPrice.setMemberLevelId(item.getId());
            memberPrice.setMemberLevelName(item.getName());
            memberPrice.setMemberPrice(item.getPrice());
            memberPrice.setAddOther(1);
            return memberPrice;
        }).filter(item -> {
            return item.getMemberPrice().compareTo(new BigDecimal("0")) == 1;
        }).collect(Collectors.toList());
        memberPriceService.saveBatch(memberPriceEntities);
    }

}