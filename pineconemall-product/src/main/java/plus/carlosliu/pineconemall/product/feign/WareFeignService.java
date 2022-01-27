package plus.carlosliu.pineconemall.product.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import plus.carlosliu.common.utils.R;

import java.util.List;
import java.util.Map;

@FeignClient("pineconemall-ware")
public interface WareFeignService {

    @PostMapping("/ware/waresku/hasStock")
    Map<Long, Boolean> getSkuHasStock(@RequestBody List<Long> skuIds);
}
