package com.pwc.ittraining;

import java.io.Serializable;

public class Document implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5255034234635676273L;

	public String DocumentID;
	public String Title;
	public String Description;
	public String CategoryId;
	public String ContentType;
	public String TabType;
	public String Duration;
	public Boolean PromoteFlag;
	public String MediaUri;
	public String LastMofifiedDate;
	
}
