package plus.carlosliu.pineconemall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.pineconemall.coupon.entity.SeckillSessionEntity;

import java.util.List;
import java.util.Map;

/**
 * 秒杀活动场次
 *
 * @author CarlosLiu
 * @email 1753459461@qq.com
 * @date 2022-01-20 12:57:51
 */
public interface SeckillSessionService extends IService<SeckillSessionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 查询最近三天的秒杀活动
     * @return 最近三天的秒杀活动
     */
    List<SeckillSessionEntity> getSecKillSessionsIn3Days();
}

