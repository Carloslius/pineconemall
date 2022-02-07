package plus.carlosliu.pineconemall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.common.utils.Query;

import plus.carlosliu.pineconemall.ware.dao.WareInfoDao;
import plus.carlosliu.pineconemall.ware.entity.WareInfoEntity;
import plus.carlosliu.pineconemall.ware.service.WareInfoService;

@Transactional
@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                new QueryWrapper<WareInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        LambdaQueryWrapper<WareInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        String key = (String) params.get("key");
        queryWrapper.eq(!StringUtils.isEmpty(key), WareInfoEntity::getId, key)
                .or().like(!StringUtils.isEmpty(key), WareInfoEntity::getName, key)
                .or().like(!StringUtils.isEmpty(key), WareInfoEntity::getAddress, key)
                .or().like(!StringUtils.isEmpty(key), WareInfoEntity::getAreacode, key);
        IPage<WareInfoEntity> page = this.page(new Query<WareInfoEntity>().getPage(params), queryWrapper);
        return new PageUtils(page);
    }

}