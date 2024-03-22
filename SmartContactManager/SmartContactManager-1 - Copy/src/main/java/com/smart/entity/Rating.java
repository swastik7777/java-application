package com.smart.entity;

public class Rating {
	
	private int ratingId;
	private int useId;
	private String remark;
	private String feedback;
	public Rating(int ratingId, int useId, String remark, String feedback) {
		super();
		this.ratingId = ratingId;
		this.useId = useId;
		this.remark = remark;
		this.feedback = feedback;
	}
	public int getRatingId() {
		return ratingId;
	}
	public void setRatingId(int ratingId) {
		this.ratingId = ratingId;
	}
	public int getUseId() {
		return useId;
	}
	public void setUseId(int useId) {
		this.useId = useId;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getFeedback() {
		return feedback;
	}
	public void setFeedback(String feedback) {
		this.feedback = feedback;
	}
	

}
