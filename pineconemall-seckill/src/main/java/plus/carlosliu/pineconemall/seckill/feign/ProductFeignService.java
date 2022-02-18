package plus.carlosliu.pineconemall.seckill.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import plus.carlosliu.common.utils.R;

@FeignClient(value = "pineconemall-product")
public interface ProductFeignService {

    @RequestMapping("product/skuinfo/info/{skuId}")
    R getSkuInfo(@PathVariable("skuId") Long skuId);
}
