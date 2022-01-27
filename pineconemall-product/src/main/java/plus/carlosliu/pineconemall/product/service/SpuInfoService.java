package plus.carlosliu.pineconemall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.pineconemall.product.entity.SpuInfoEntity;
import plus.carlosliu.pineconemall.product.vo.SpuSaveVo;

import java.util.Map;

/**
 * spu信息
 *
 * @author CarlosLiu
 * @email 1753459461@qq.com
 * @date 2022-01-20 12:33:29
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 按照条件检索spu信息
     * @param params 分页条件
     * @return 分页信息
     */
    PageUtils queryPageByCondition(Map<String, Object> params);

    /**
     * 保存spu信息以及关联信息
     * @param spuSaveVo spu信息
     */
    void saveSpuInfo(SpuSaveVo spuSaveVo);

    /**
     * 商品上架
     * @param spuId 上架的商品(spu)id
     */
    void up(Long spuId);
}

