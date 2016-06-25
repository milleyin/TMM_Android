package com.qljl.tmm.bean;
/**
 * 公交线路实体类封装
 * @author lw
 *
 */
public class BusLine {
	private String busLineName;//线路名字
	private String busDetail;//线路其他详细信息
	
	public BusLine() {
		super();
	}
	public String getBusLineName() {
		return busLineName;
	}
	public void setBusLineName(String busLineName) {
		this.busLineName = busLineName;
	}
	public String getBusDetail() {
		return busDetail;
	}
	public void setBusDetail(String busDetail) {
		this.busDetail = busDetail;
	}
	
}
