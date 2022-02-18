package plus.carlosliu.pineconemall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.pineconemall.coupon.entity.SeckillSkuRelationEntity;

import java.util.Map;

/**
 * 秒杀活动商品关联
 *
 * @author CarlosLiu
 * @email 1753459461@qq.com
 * @date 2022-01-20 12:57:51
 */
public interface SeckillSkuRelationService extends IService<SeckillSkuRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 根据场次id查询该场次所有商品
     * @param params 分页参数和场次id
     * @return 分页数据
     */
    PageUtils queryPageBySessionId(Map<String, Object> params);
}

