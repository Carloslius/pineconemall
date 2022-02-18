package plus.carlosliu.pineconemall.product.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import plus.carlosliu.common.utils.R;
import plus.carlosliu.pineconemall.product.feign.fallback.SecKillFallbackService;

@FeignClient(value = "pineconemall-seckill", fallback = SecKillFallbackService.class)
public interface SecKillFeignService {

    @GetMapping("/getSecKillSkuInfo/{skuId}")
    R getSecKillSkuInfo(@PathVariable("skuId") Long skuId);
}
