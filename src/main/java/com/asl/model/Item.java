package com.asl.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.Data;

@Data
@Entity
@Table(name = "item")
public class Item implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Basic(optional = false)
	@Column(name = "id")
	private String id;

	@Column(name = "short_code")
	private String shortCode;

	@Column(name = "category")
	private String category;

	@Column(name = "sub_category")
	private String subCategory;

	@Column(name = "cost")
	private BigDecimal cost;

	@Column(name = "name")
	private String name;

	@Column(name = "color")
	private String color;

	@Column(name = "price")
	private BigDecimal price;

	@Column(name = "discount_rate")
	private BigDecimal discountRate;

	@Column(name = "discount_status")
	private String discountStatus;

	@Column(name = "discount_type")
	private String discountType;

	@Column(name = "supp_tax_rate")
	private BigDecimal suppTaxRate;

	@Column(name = "supp_tax_amt")
	private BigDecimal suppTaxAmt;

	@Column(name = "vat_rate")
	private BigDecimal vatRate;

	@Column(name = "vat_amt")
	private BigDecimal vatAmt;

	@Column(name = "type")
	private String type;

	@Column(name = "is_set")
	private String isSet;

	@Column(name = "is_set_item")
	private String isSetItem;

	@Column(name = "unit")
	private String unit;

	@Column(name = "image")
	private String image;

	@Column(name = "business_id")
	private String businessId;

	@Column(name = "division")
	private String division;

	@Column(name = "shop")
	private String shop;

	@Column(name = "terminal")
	private String terminal;

	@Column(name = "status")
	private String status;

	@Column(name = "integer1")
	private Integer integer1;

	@Column(name = "integer2")
	private Integer integer2;

	@Column(name = "integer3")
	private Integer integer3;

	@Column(name = "string1")
	private String string1;

	@Column(name = "string2")
	private String string2;

	@Column(name = "string3")
	private String string3;

	@Column(name = "date_created", updatable = false)
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateCreated;

	@Column(name = "date_updated")
	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateUpdated;

	@Column(name = "user_created")
	private String userCreated;

	@Column(name = "user_updated")
	private String userUpdated;
}
