package plus.carlosliu.pineconemall.product.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import plus.carlosliu.common.to.es.SkuEsModel;
import plus.carlosliu.common.utils.R;

import java.util.List;

@FeignClient("pineconemall-search")
public interface SearchFeignService {

    @PostMapping("/search/save/product")
    R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels);
}
