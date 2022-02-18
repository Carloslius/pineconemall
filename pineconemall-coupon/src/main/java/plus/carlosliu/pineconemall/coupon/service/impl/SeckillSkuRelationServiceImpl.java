package plus.carlosliu.pineconemall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.common.utils.Query;

import plus.carlosliu.pineconemall.coupon.dao.SeckillSkuRelationDao;
import plus.carlosliu.pineconemall.coupon.entity.SeckillSkuRelationEntity;
import plus.carlosliu.pineconemall.coupon.service.SeckillSkuRelationService;


@Service("seckillSkuRelationService")
public class SeckillSkuRelationServiceImpl extends ServiceImpl<SeckillSkuRelationDao, SeckillSkuRelationEntity> implements SeckillSkuRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSkuRelationEntity> page = this.page(
                new Query<SeckillSkuRelationEntity>().getPage(params),
                new QueryWrapper<SeckillSkuRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageBySessionId(Map<String, Object> params) {
        LambdaQueryWrapper<SeckillSkuRelationEntity> queryWrapper = new LambdaQueryWrapper<>();
        String promotionSessionId = (String) params.get("promotionSessionId");
        queryWrapper.eq(!StringUtils.isEmpty(promotionSessionId), SeckillSkuRelationEntity::getPromotionSessionId, promotionSessionId);
        IPage<SeckillSkuRelationEntity> page = this.page(new Query<SeckillSkuRelationEntity>().getPage(params), queryWrapper);
        return new PageUtils(page);
    }

}