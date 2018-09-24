package com.taotao.search.service;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taotao.common.service.ApiService;
import com.taotao.search.bean.Item;
import com.taotao.search.bean.SearchResult;

@Service
public class ItemSearchService {
	
	@Autowired
	private HttpSolrServer httpSolrServer;
	
	@Autowired
	private ApiService apiService;
	
	@Value("${TAOTAO_MANAGE_URL}")
	private String TAOTAO_MANAGE_URL;
	
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	/**
	 * 通过关键词搜索商品。
	 * 这里的代码怎么写，得参考：/itcast-solrj/src/main/java/cn/itcast/solrj/service/SolrjService.java中的search()方法。
	 * 构造搜索条件时，一定要加上" AND status:1"。这是where条件的拼写，所以空格不能省略，AND也不能改为小写。
	 * @throws SolrServerException 
	 */
	public SearchResult searchItemsByKeywords(String keywords, Integer page, Integer rows) throws SolrServerException {
		SolrQuery solrQuery = new SolrQuery(); // 构造搜索条件
		solrQuery.setQuery("title:" + keywords + " AND status:1"); // 搜索关键词，一定要是上架状态的商品！！！
		// 设置分页 start=0就是从0开始，，rows=5当前返回5条记录，第二页就是变化start这个值为5就可以了。
		solrQuery.setStart((Math.max(page, 1) - 1) * rows);
		solrQuery.setRows(rows);

		// 是否需要高亮
		boolean isHighlighting = !StringUtils.equals("*", keywords) && StringUtils.isNotEmpty(keywords);

		if (isHighlighting) {
			// 设置高亮
			solrQuery.setHighlight(true); // 开启高亮组件
			solrQuery.addHighlightField("title");// 高亮字段
			solrQuery.setHighlightSimplePre("<em>");// 标记，高亮关键字前缀
			solrQuery.setHighlightSimplePost("</em>");// 后缀
		}

		// 执行查询
		QueryResponse queryResponse = this.httpSolrServer.query(solrQuery);
		//这里拿到的是正常的数据，而非带高亮显示的数据：
		List<Item> items = queryResponse.getBeans(Item.class);
		if (isHighlighting) {
			// 将高亮的标题数据写回到数据对象中
			Map<String, Map<String, List<String>>> map = queryResponse.getHighlighting();
			for (Map.Entry<String, Map<String, List<String>>> highlighting : map.entrySet()) {
				for (Item Item : items) {
					if (!highlighting.getKey().equals(Item.getId().toString())) {
						continue;
					}
					Item.setTitle(StringUtils.join(highlighting.getValue().get("title"), ""));
					break;
				}
			}
		}
		//通过网页http://solr.taotao.com/#/taotao/query的查询结果，发现结果集中商品的总数会被放到numFound中：
		return new SearchResult(queryResponse.getResults().getNumFound(), items);
	}
	
	/**
	 * 根据商品id从后台系统（B端）获取item对象。
	 * @param itemId
	 * @return item
	 */
	public Item queryItemByItemIdFromBPort(Long itemId){
		String url = TAOTAO_MANAGE_URL + "/rest/api/item/" + itemId;
		try {
			String jsonData = this.apiService.doGet(url);
			if(StringUtils.isNotEmpty(jsonData)){
				return MAPPER.readValue(jsonData, Item.class);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
	}
}
