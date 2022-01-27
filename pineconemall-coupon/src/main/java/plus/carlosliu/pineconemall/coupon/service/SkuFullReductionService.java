package plus.carlosliu.pineconemall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import plus.carlosliu.common.to.SkuReductionTo;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.pineconemall.coupon.entity.SkuFullReductionEntity;

import java.util.Map;

/**
 * 商品满减信息
 *
 * @author CarlosLiu
 * @email 1753459461@qq.com
 * @date 2022-01-20 12:57:50
 */
public interface SkuFullReductionService extends IService<SkuFullReductionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 保存sku的满减和折扣信息
     * @param skuReductionTo sku的满减和折扣信息
     */
    void saveSkuReduction(SkuReductionTo skuReductionTo);
}

