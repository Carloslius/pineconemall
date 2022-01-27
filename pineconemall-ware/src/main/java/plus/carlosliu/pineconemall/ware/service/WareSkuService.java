package plus.carlosliu.pineconemall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import plus.carlosliu.common.to.SkuHasStockTo;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.pineconemall.ware.entity.WareSkuEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author CarlosLiu
 * @email 1753459461@qq.com
 * @date 2022-01-20 13:11:00
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 查询商品库存，根据条件查询仓库中sku的信息
     * @param params 分页条件
     * @return 分页数据
     */
    PageUtils queryPageByCondition(Map<String, Object> params);

    /**
     * 将成功采购的信息入库
     * @param skuId 采购sku的id
     * @param wareId 仓库id
     * @param skuNum 采购数量
     */
    void addStock(Long skuId, Long wareId, Integer skuNum);

    /**
     * 查询sku是否有库存，上架功能远程调用
     * @param skuIds 要检查的skuId集合
     * @return 每个skuId对应没有没库存
     */
    List<SkuHasStockTo> getSkuHasStock(List<Long> skuIds);
}

