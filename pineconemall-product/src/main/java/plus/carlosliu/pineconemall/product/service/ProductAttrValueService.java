package plus.carlosliu.pineconemall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.pineconemall.product.entity.ProductAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * spu属性值
 *
 * @author CarlosLiu
 * @email 1753459461@qq.com
 * @date 2022-01-20 12:33:29
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 获取spu(商品)规格
     * @param spuId 要获取的spu的id
     * @return spu规格集合
     */
    List<ProductAttrValueEntity> baseAttrListForSpu(Long spuId);

    /**
     * 修改商品(spu)规格
     * @param spuId 要获取的spu的id
     * @param productAttrValueList 要修改的值
     */
    void updateSpuAttr(Long spuId, List<ProductAttrValueEntity> productAttrValueList);
}

