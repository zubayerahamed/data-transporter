package com.asl.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Data;

/**
 * @author Zubayer Ahamed
 *
 * @since Dec 23, 2021
 */
@Data
@Entity
@Table(name = "XXSSGIL_WB_TRIP_DTLS")
public class Trip implements Serializable {

	private static final long serialVersionUID = 5362226979371014366L;

	@Id
	@Column(name = "TRIP_NO")
	private String tripNo;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "LIGHT_WT_TIMESTAMP")
	private Date lightWeightTime;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "LOAD_WT_TIMESTAMP")
	private Date loadWeightTime;

	@Column(name = "LIGHT_WT_BY")
	private String lightWeightBy;

	@Column(name = "LOAD_WT_BY")
	private String loadWeightBy;

	@Column(name = "LIGHT_WT_IN_KG")
	private Double lightWeithInKg;

	@Column(name = "LOAD_WT_IN_KG")
	private Double loadWeithInKg;

	@Column(name = "GOODS_WT_IN_KG_WB")
	private Double goodsWeightInKgWb;
	
	@Column(name = "GOODS_WT_IN_KG_TRIP")
	private Double goodsWeightInKgTrip;

	@Column(name = "CUST_ACCEPT_WT_IN_KG")
	private Double custAcceptWeightInKg;

	@Column(name = "REMARKS")
	private String remarks;

	@Column(name = "STATUS")
	private String status;

	@Column(name = "ORACLE_STATUS")
	private String oracleStatus;
}
