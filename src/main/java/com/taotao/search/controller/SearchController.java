package com.taotao.search.controller;

import java.io.UnsupportedEncodingException;

import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.taotao.search.bean.SearchResult;
import com.taotao.search.service.ItemSearchService;

/**
 * 实现搜索商品的功能
 */
@Controller
@RequestMapping("search")
public class SearchController {
	
	@Autowired
	private ItemSearchService itemSearchService;
	
	private static final Integer ROWS = 32;
	
	/**
	 * 通过关键词搜索商品
	 */
	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView searchItemsByKeywords(
			@RequestParam("q")String keywords,
			@RequestParam(value = "page", defaultValue = "1")Integer page
			){
		ModelAndView mv = new ModelAndView("search");
		
		//处理乱码问题：
		try {
			keywords = new String(keywords.getBytes("ISO-8859-1"), "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			//如果出错，则将keywords设为空字符串，在页面不显示：
			keywords = "";
		}
		//mv中添加搜索关键词：
		mv.addObject("query", keywords);
		SearchResult searchResult = null;
		try {
			searchResult = this.itemSearchService.searchItemsByKeywords(keywords, page, ROWS);
		} catch (SolrServerException e) {
			e.printStackTrace();
			searchResult = new SearchResult(0L, null); 
		}
		
		//mv中添加搜索的商品结果集：
		mv.addObject("itemList", searchResult.getList());
		
		//mv中添加当前页：
		mv.addObject("page", page);
		
		/**
		 * mv中添加总页数：
		 * 总页数=商品总条数/每页显示条数。
		 * 注意：用"/"，还是用"%"的问题。
		 */
		Long total = searchResult.getTotal();
		//第一种写法：
//		if(total % ROWS == 0){
//			mv.addObject("pages", total / ROWS);
//		}else{
//			mv.addObject("pages", total / ROWS + 1);
//		}
		//第二种写法：
		Long pages = total % ROWS == 0 ? total / ROWS : (total / ROWS) + 1;
		mv.addObject("pages", pages);
		return mv;
	}
}
