package bean;

import java.util.List;

public class NewsBean {
	public int ReturnCode;
	public String ServiceMethod;
	public String Message;
	public NewsList Data;

	public static class NewsList {
		public String UpdateDateTime;

		public List<DeleteDocID> DeleteDocumentID;
		public List<Category> UpdateCategoryList;
		public List<NewDocument> NewDocumentInfo;
		public static class DeleteDocID {
			public String ID;
		}
		
		public static class NewDocument {
			public String DocumentID;
			public String Title;
			public String Description;
			public int CategoryID;
			public int ContentType;
			public String[] Tags;
			public String TabType;
			public String Duration;
			public Boolean PromoteFlag;
			public String MediaUri;
			public String LastModifiedDate;
		}
		
		public static class Category {
			public int CategoryID;
			public String CategoryName;
			public int CategoryOrder;
		}
	}
	
	public static class NewsSortList{

		public List<PopularDocID> PopularDocumentID;
		public NewsList UpdatedDocumentInfo;
		public static class PopularDocID {
			public String ID;
		}
		
		
	}

}
