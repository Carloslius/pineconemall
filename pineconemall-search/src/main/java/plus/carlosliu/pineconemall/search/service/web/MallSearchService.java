package plus.carlosliu.pineconemall.search.service.web;

import plus.carlosliu.pineconemall.search.vo.web.SearchParams;
import plus.carlosliu.pineconemall.search.vo.web.SearchResult;

public interface MallSearchService {

    /**
     *
     * @param searchParams 检索的所有参数
     * @return 返回检索的结果，里面包含页面需要的所有信息
     */
    SearchResult searchByParams(SearchParams searchParams);
}
