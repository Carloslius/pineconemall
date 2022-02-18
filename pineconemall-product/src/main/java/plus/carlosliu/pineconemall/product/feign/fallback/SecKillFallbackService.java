package plus.carlosliu.pineconemall.product.feign.fallback;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import plus.carlosliu.common.exception.BizCodeEnume;
import plus.carlosliu.common.utils.R;
import plus.carlosliu.pineconemall.product.feign.SecKillFeignService;

@Slf4j
@Component
public class SecKillFallbackService implements SecKillFeignService {

    @Override
    public R getSecKillSkuInfo(Long skuId) {
        log.info("SecKillFallbackService熔断方法getSecKillSkuInfo调用");
        return R.error(BizCodeEnume.READ_TIME_OUT_EXCEPTION.getCode(), BizCodeEnume.READ_TIME_OUT_EXCEPTION.getMsg());
    }
}
