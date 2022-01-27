package plus.carlosliu.pineconemall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.pineconemall.ware.entity.PurchaseEntity;
import plus.carlosliu.pineconemall.ware.vo.MergeVo;
import plus.carlosliu.pineconemall.ware.vo.PurchaseDoneVo;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author CarlosLiu
 * @email 1753459461@qq.com
 * @date 2022-01-20 13:11:00
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 合并采购需求
     * @param mergeVo 合并参数
     */
    void mergePurchase(MergeVo mergeVo);

    /**
     * 查询未领取的采购单
     * @param params 分页参数
     * @return 分页数据
     */
    PageUtils queryPageUnReceive(Map<String, Object> params);

    /**
     * 领取采购单
     * @param ids 要领取的采购单ids
     */
    void received(List<Long> ids);

    /**
     * 完成采购
     * @param purchaseDoneVo 完成的采购单id以及具体采购项
     */
    void done(PurchaseDoneVo purchaseDoneVo);

    /**
     * 查询采购单，根据条件查询采购单信息
     * @param params 分页条件
     * @return 分页数据
     */
    PageUtils queryPageByCondition(Map<String, Object> params);
}

