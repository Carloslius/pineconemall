package plus.carlosliu.pineconemall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import javafx.scene.control.Label;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.common.utils.Query;

import plus.carlosliu.pineconemall.coupon.dao.SeckillSessionDao;
import plus.carlosliu.pineconemall.coupon.entity.SeckillSessionEntity;
import plus.carlosliu.pineconemall.coupon.entity.SeckillSkuRelationEntity;
import plus.carlosliu.pineconemall.coupon.service.SeckillSessionService;
import plus.carlosliu.pineconemall.coupon.service.SeckillSkuRelationService;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    private SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getSecKillSessionsIn3Days() {
        // 计算最近三天 只能创一天的秒杀
        LambdaQueryWrapper<SeckillSessionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(SeckillSessionEntity::getStartTime, this.getStartTime(), this.getEndTime());
        List<SeckillSessionEntity> list = baseMapper.selectList(queryWrapper);
        if (list != null && list.size() > 0){
            List<SeckillSessionEntity> collect = list.stream().map(session -> {
                Long sessionId = session.getId();
                List<SeckillSkuRelationEntity> relationList = seckillSkuRelationService.list(new LambdaQueryWrapper<SeckillSkuRelationEntity>()
                        .eq(sessionId != null, SeckillSkuRelationEntity::getPromotionSessionId, sessionId));
                session.setRelationSkus(relationList);
                return session;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }
    //当前天数的 00:00:00
    private String getStartTime() {
        LocalDate now = LocalDate.now();
        LocalDateTime time = now.atTime(LocalTime.MIN);
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    //当前天数+2 23:59:59..
    private String getEndTime() {
        LocalDate now = LocalDate.now();
        LocalDateTime time = now.plusDays(2).atTime(LocalTime.MAX);
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

}