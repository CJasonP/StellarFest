package models;

public class Product {
	private String product_id;
	private String user_id;
	private String name;
	private String description;

	public Product(String product_id, String user_id, String name, String description) {
		super();
		this.product_id = product_id;
		this.user_id = user_id;
		this.name = name;
		this.description = description;
	}

	public String getProduct_id() {
		return product_id;
	}

	public void setProduct_id(String product_id) {
		this.product_id = product_id;
	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
