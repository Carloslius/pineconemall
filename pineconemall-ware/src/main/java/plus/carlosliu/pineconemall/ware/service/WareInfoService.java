package plus.carlosliu.pineconemall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.pineconemall.ware.entity.WareInfoEntity;
import plus.carlosliu.pineconemall.ware.vo.FareVo;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 仓库信息
 *
 * @author CarlosLiu
 * @email 1753459461@qq.com
 * @date 2022-01-20 13:11:00
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 仓库列表，根据条件查询仓库信息
     * @param params 分页条件
     * @return 分页数据
     */
    PageUtils queryPageByCondition(Map<String, Object> params);

    /**
     * 根据收货地址获取运费
     * @param addrId 地址
     * @return 运费，和对应的地址信息
     */
    FareVo getFare(Long addrId);
}

