package plus.carlosliu.pineconemall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.util.StringUtils;
import plus.carlosliu.common.constant.WareConstant;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.common.utils.Query;

import plus.carlosliu.pineconemall.ware.dao.PurchaseDao;
import plus.carlosliu.pineconemall.ware.entity.PurchaseDetailEntity;
import plus.carlosliu.pineconemall.ware.entity.PurchaseEntity;
import plus.carlosliu.pineconemall.ware.service.PurchaseDetailService;
import plus.carlosliu.pineconemall.ware.service.PurchaseService;
import plus.carlosliu.pineconemall.ware.service.WareSkuService;
import plus.carlosliu.pineconemall.ware.vo.MergeVo;
import plus.carlosliu.pineconemall.ware.vo.PurchaseDoneItemVo;
import plus.carlosliu.pineconemall.ware.vo.PurchaseDoneVo;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    private PurchaseDetailService purchaseDetailService;
    @Autowired
    private WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        if (purchaseId == null){
            PurchaseEntity purchase = new PurchaseEntity();
            purchase.setStatus(0);
            purchase.setCreateTime(new Date());
            purchase.setUpdateTime(new Date());
            baseMapper.insert(purchase);
            purchaseId = purchase.getId();
        }

        List<Long> items = mergeVo.getItems();
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> purchaseDetailEntities = items.stream().filter(item -> {
            // 确认采购状态是0、1才可以合并
            PurchaseDetailEntity purchaseDetail = purchaseDetailService.getById(item);
            return purchaseDetail.getStatus() == WareConstant.PurchaseDetailStatusEnum.CREATED.getCode() ||
                    purchaseDetail.getStatus() == WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode();
        }).map(item -> {
            PurchaseDetailEntity purchaseDetail = new PurchaseDetailEntity();
            purchaseDetail.setId(item);
            purchaseDetail.setPurchaseId(finalPurchaseId);
            purchaseDetail.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
            return purchaseDetail;
        }).collect(Collectors.toList());
        purchaseDetailService.updateBatchById(purchaseDetailEntities);

        PurchaseEntity purchase = new PurchaseEntity();
        purchase.setId(purchaseId);
        purchase.setUpdateTime(new Date());
        baseMapper.updateById(purchase);
    }

    @Override
    public PageUtils queryPageUnReceive(Map<String, Object> params) {
        LambdaQueryWrapper<PurchaseEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PurchaseEntity::getStatus, WareConstant.PurchaseStatusEnum.CREATED.getCode())
                .or().eq(PurchaseEntity::getStatus, WareConstant.PurchaseStatusEnum.ASSIGNED.getCode());
        IPage<PurchaseEntity> page = this.page(new Query<PurchaseEntity>().getPage(params), queryWrapper);
        return new PageUtils(page);
    }

    @Override
    public void received(List<Long> ids) {
        // 1、确认当前采购单是新建或已分配状态
        List<PurchaseEntity> purchaseEntities = ids.stream().map(id -> {
            return baseMapper.selectById(id);
        }).filter(item -> {
            return item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode() ||
                    item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode();
        }).map(item -> {
            item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
            item.setUpdateTime(new Date());
            return item;
        }).collect(Collectors.toList());
        // 2、改变采购单的状态 (baseMapper中无根据id批量修改的方法)
        this.updateBatchById(purchaseEntities);
        // 3、改变采购项的状态
        purchaseEntities.forEach(item -> {
            LambdaQueryWrapper<PurchaseDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(item.getId() != null, PurchaseDetailEntity::getPurchaseId, item.getId());
            List<PurchaseDetailEntity> purchaseDetailEntities = purchaseDetailService.list(queryWrapper);

            List<PurchaseDetailEntity> detailEntities = purchaseDetailEntities.stream().map(entity -> {
                PurchaseDetailEntity purchaseDetail = new PurchaseDetailEntity();
                // 根据采购信息id修改状态
                purchaseDetail.setId(entity.getId());
                purchaseDetail.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                return purchaseDetail;
            }).collect(Collectors.toList());
            purchaseDetailService.updateBatchById(detailEntities);
        });
    }

    @Override
    public void done(PurchaseDoneVo purchaseDoneVo) {
        // 2、改变采购项状态
        AtomicReference<Boolean> flag = new AtomicReference<>(true);
        List<PurchaseDoneItemVo> purchaseDoneVoItems = purchaseDoneVo.getItems();
        List<PurchaseDetailEntity> purchaseDetailEntities = purchaseDoneVoItems.stream().map(item -> {
            PurchaseDetailEntity purchaseDetail = new PurchaseDetailEntity();
            if (item.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode()) {
                flag.set(false);
                purchaseDetail.setStatus(item.getStatus());
            } else {
                purchaseDetail.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISH.getCode());
                // 3、将成功采购的进行入库
                PurchaseDetailEntity purchaseDetailEntity = purchaseDetailService.getById(item.getItemId());
                wareSkuService.addStock(purchaseDetailEntity.getSkuId(), purchaseDetailEntity.getWareId(), purchaseDetailEntity.getSkuNum());
            }
            purchaseDetail.setId(item.getItemId());
            return purchaseDetail;
        }).collect(Collectors.toList());
        purchaseDetailService.updateBatchById(purchaseDetailEntities);

        // 1、改变采购单状态
        PurchaseEntity purchase = new PurchaseEntity();
        purchase.setId(purchaseDoneVo.getId());
        purchase.setStatus(flag.get() ? WareConstant.PurchaseStatusEnum.FINISH.getCode(): WareConstant.PurchaseStatusEnum.HASERROR.getCode());
        purchase.setUpdateTime(new Date());
        baseMapper.updateById(purchase);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        LambdaQueryWrapper<PurchaseEntity> queryWrapper = new LambdaQueryWrapper<>();

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)){
            queryWrapper.and(wrapper -> {
                wrapper.eq(PurchaseEntity::getId, key)
                        .or().eq(PurchaseEntity::getPriority, key)
                        .or().eq(PurchaseEntity::getAssigneeId, key)
                        .or().eq(PurchaseEntity::getWareId, key)
                        .or().like(PurchaseEntity::getAssigneeName, key);
            });
        }

        String status = (String) params.get("status");
        queryWrapper.eq(!StringUtils.isEmpty(status), PurchaseEntity::getStatus, status);

        IPage<PurchaseEntity> page = this.page(new Query<PurchaseEntity>().getPage(params), queryWrapper);
        return new PageUtils(page);
    }

}