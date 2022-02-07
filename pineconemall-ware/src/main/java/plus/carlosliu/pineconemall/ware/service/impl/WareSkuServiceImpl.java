package plus.carlosliu.pineconemall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import plus.carlosliu.common.to.SkuHasStockTo;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.common.utils.Query;

import plus.carlosliu.common.utils.R;
import plus.carlosliu.pineconemall.ware.dao.WareSkuDao;
import plus.carlosliu.pineconemall.ware.entity.WareSkuEntity;
import plus.carlosliu.pineconemall.ware.feign.ProductFeignService;
import plus.carlosliu.pineconemall.ware.service.WareSkuService;

@Transactional
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private WareSkuDao wareSkuDao;

    @Autowired
    private ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        LambdaQueryWrapper<WareSkuEntity> queryWrapper = new LambdaQueryWrapper<>();
        String skuId = (String) params.get("skuId");
        queryWrapper.eq(!StringUtils.isEmpty(skuId), WareSkuEntity::getSkuId, skuId);

        String wareId = (String) params.get("wareId");
        queryWrapper.eq(!StringUtils.isEmpty(wareId), WareSkuEntity::getWareId, wareId);

        IPage<WareSkuEntity> page = this.page(new Query<WareSkuEntity>().getPage(params), queryWrapper);
        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        LambdaQueryWrapper<WareSkuEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(skuId != null, WareSkuEntity::getSkuId, skuId);
        queryWrapper.eq(wareId != null, WareSkuEntity::getWareId, wareId);
        List<WareSkuEntity> wareSkuEntities = wareSkuDao.selectList(queryWrapper);
        if (wareSkuEntities == null || wareSkuEntities.isEmpty()){
            WareSkuEntity wareSku = new WareSkuEntity();
            wareSku.setSkuId(skuId);
            wareSku.setWareId(wareId);
            wareSku.setStock(skuNum);
            wareSku.setStockLocked(0);
            // 远程查询sku的名字，如果失败，整个事务无需回滚
            // 1、自己catch异常
            // 2、TODO：还可以用什么办法让异常出现以后不回滚？高级部分
            try {
                R info = productFeignService.info(skuId);
                Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");
                if (info.getCode() == 0){
                    wareSku.setSkuName((String) data.get("skuName"));
                }
            }catch (Exception e){
            }
            wareSkuDao.insert(wareSku);
        }else {
            wareSkuDao.addStock(skuId, wareId, skuNum);
        }
    }

    @Override
    public List<SkuHasStockTo> getSkuHasStock(List<Long> skuIds) {
        List<SkuHasStockTo> stockTos = skuIds.stream().map(skuId -> {
            SkuHasStockTo to = new SkuHasStockTo();
            Long count = baseMapper.getSkuStock(skuId);
            to.setSkuId(skuId);
            to.setHasStock(count != null && count > 0);
            return to;
        }).collect(Collectors.toList());
        return stockTos;
    }

}