package com.taotao.search.bean;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.beans.Field;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 商品实体.
 * "@Field"注解的作用：指向被注解的属性在solr的schema.xml文件中配置的字段名称。
 */
@JsonIgnoreProperties(ignoreUnknown = true)//忽略未知的字段。从数据库查出来的字段是该Item类中没有的字段，就进行忽略。
public class Item{
	
	@Field("id")
    private Long id;
	
	@Field("title")
    private String title;
	
	@Field("sellPoint")
    private String sellPoint;

	@Field("price")
    private Long price;

	@Field("image")
    private String image;

	@Field("cid")
    private Long cid;

	@Field("status")
    private Integer status;

	@Field("created")
	private Long created;
	
	@Field("updated")
	private Long updated;
	
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSellPoint() {
        return sellPoint;
    }

    public void setSellPoint(String sellPoint) {
        this.sellPoint = sellPoint;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Long getCid() {
        return cid;
    }

    public void setCid(Long cid) {
        this.cid = cid;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
	
	public Long getCreated() {
		return created;
	}

	public void setCreated(Long created) {
		this.created = created;
	}

	public Long getUpdated() {
		return updated;
	}

	public void setUpdated(Long updated) {
		this.updated = updated;
	}
	
	//在search.jsp中，有用到item.images[],所以这里需要把所有的item对象放到images中，并以逗号分割。
	public String[] getImages(){
		return StringUtils.split(this.getImage(), ',');
	}
	
    @Override
    public String toString() {
        return "Item [id=" + id + ", title=" + title + ", sellPoint=" + sellPoint + ", price=" + price
                + ", image=" + image + ", cid=" + cid + ", status="
                + status + "]";
    }
}
