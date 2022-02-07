package plus.carlosliu.pineconemall.search.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import plus.carlosliu.pineconemall.search.utils.R;

@FeignClient("pineconemall-product")
public interface ProductFeignService {

    @GetMapping("/product/attr/info/{attrId}")
    R info(@PathVariable("attrId") Long attrId);
}
