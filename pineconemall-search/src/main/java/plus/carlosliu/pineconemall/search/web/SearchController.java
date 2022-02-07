package plus.carlosliu.pineconemall.search.web;

import com.netflix.client.http.HttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import plus.carlosliu.pineconemall.search.service.web.MallSearchService;
import plus.carlosliu.pineconemall.search.vo.web.SearchParams;
import plus.carlosliu.pineconemall.search.vo.web.SearchResult;

import javax.servlet.http.HttpServletRequest;

@Controller
public class SearchController {

    @Autowired
    private MallSearchService mallSearchService;

    @GetMapping("/list.html")
    public String listPage(SearchParams searchParams, Model model, HttpServletRequest request){
        searchParams.set_queryString(request.getQueryString());
        SearchResult result = mallSearchService.searchByParams(searchParams);
        model.addAttribute("result", result);
        return "list";
    }
}
