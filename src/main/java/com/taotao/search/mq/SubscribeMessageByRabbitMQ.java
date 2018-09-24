package com.taotao.search.mq;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taotao.search.bean.Item;
import com.taotao.search.service.ItemSearchService;

public class SubscribeMessageByRabbitMQ {
	
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	@Autowired
	private HttpSolrServer httpSolrServer;
	
	@Autowired
	private ItemSearchService itemSearchService;
	
	/**
	 * 处理消息：新增，修改，删除。将商品数据同步到solr中。
	 * 注意：消息中并没有包含商品的基本数据，消息中具体有什么需要看后台更新数据时发布的消息。
	 */
    public void listen(String message) {
        try {
			JsonNode jsonNode = MAPPER.readTree(message);
			Long id = jsonNode.get("id").asLong();
			String routingKey = jsonNode.get("routingKey").asText();
			//新增和更新是一样的操作，区别在于id：如果该id在solr中存在，就做更新，如果不存在就做新增。
			if(StringUtils.equals(routingKey, "item.insert") || StringUtils.equals(routingKey, "item.update")){
				//item对象从哪儿获取：根据itemId去B端系统拿
				Item item = this.itemSearchService.queryItemByItemIdFromBPort(id);
				if(null != item){
					this.httpSolrServer.addBean(item);
					this.httpSolrServer.commit();
				}else{
					this.httpSolrServer.deleteById(routingKey);
					this.httpSolrServer.commit();//提交
				}
			}else{//删除：
				this.httpSolrServer.deleteById(routingKey);
				this.httpSolrServer.commit();//提交
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
    }
}
