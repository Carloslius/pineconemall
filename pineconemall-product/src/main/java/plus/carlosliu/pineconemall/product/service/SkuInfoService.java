package plus.carlosliu.pineconemall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.pineconemall.product.entity.SkuInfoEntity;
import plus.carlosliu.pineconemall.product.vo.web.SkuItemVo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * sku信息
 *
 * @author CarlosLiu
 * @email 1753459461@qq.com
 * @date 2022-01-20 12:33:29
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 按照条件检索sku信息
     * @param params 分页条件
     * @return 分页数据
     */
    PageUtils queryPageByCondition(Map<String, Object> params);

    /**
     * 根据spuId获取对应的所有sku信息
     * @param spuId spuId
     * @return 所有sku信息
     */
    List<SkuInfoEntity> getSkusBySpuId(Long spuId);

    /**
     * 查询sku详细信息
     * @param skuId 要查询的skuId
     * @return sku详细信息
     */
    SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException;
}

