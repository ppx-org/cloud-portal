package com.ppx.cloud.portal.log;

import java.util.Date;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonFormat;

public class AccessEntity {
	
	private ObjectId _id;
	private String hexId;
	
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss.SSS", timezone="GMT+8")
	private Date beginTime;
	private long spendTime;
	private String ip;
	private String uri;
	private String queryString;	
	private ErrorBean error;
	private long useMemory;
	private String serviceId;
	private String userId;
	
	public Date getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(Date beginTime) {
		this.beginTime = beginTime;
	}

	public long getSpendTime() {
		return spendTime;
	}

	public void setSpendTime(long spendTime) {
		this.spendTime = spendTime;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public ErrorBean getError() {
		return error;
	}

	public void setError(ErrorBean error) {
		this.error = error;
	}

	public ObjectId get_id() {
		return _id;
	}

	public void set_id(ObjectId _id) {
		this._id = _id;
	}

	public long getUseMemory() {
		return useMemory;
	}

	public void setUseMemory(long useMemory) {
		this.useMemory = useMemory;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getHexId() {
		if (_id != null) hexId = _id.toHexString();
		return hexId;
	}

	public void setHexId(String hexId) {
		this.hexId = hexId;
	}

	
}
