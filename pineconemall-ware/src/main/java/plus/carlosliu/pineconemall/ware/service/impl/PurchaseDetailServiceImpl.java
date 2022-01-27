package plus.carlosliu.pineconemall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.util.StringUtils;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.common.utils.Query;

import plus.carlosliu.pineconemall.ware.dao.PurchaseDetailDao;
import plus.carlosliu.pineconemall.ware.entity.PurchaseDetailEntity;
import plus.carlosliu.pineconemall.ware.entity.WareSkuEntity;
import plus.carlosliu.pineconemall.ware.service.PurchaseDetailService;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),
                new QueryWrapper<PurchaseDetailEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        LambdaQueryWrapper<PurchaseDetailEntity> queryWrapper = new LambdaQueryWrapper<>();

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)){
            queryWrapper.and(wrapper -> {
                wrapper.eq(PurchaseDetailEntity::getId, key).or().eq(PurchaseDetailEntity::getSkuId, key);
            });
        }

        String status = (String) params.get("status");
        queryWrapper.eq(!StringUtils.isEmpty(status), PurchaseDetailEntity::getStatus, status);

        String wareId = (String) params.get("wareId");
        queryWrapper.eq(!StringUtils.isEmpty(wareId), PurchaseDetailEntity::getWareId, wareId);

        IPage<PurchaseDetailEntity> page = this.page(new Query<PurchaseDetailEntity>().getPage(params), queryWrapper);
        return new PageUtils(page);
    }

}