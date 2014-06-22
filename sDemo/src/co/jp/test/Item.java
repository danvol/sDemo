package src.co.jp.test;

public abstract class Item {
	private String name = "";
	private Double price = 0d;
	private String dispPrice = "";
	private String description = "";
	private Double weight = 0d;
	private Double weighKg = 0d;
	private String href = "";
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}
	public Double getWeight() {
		return weight;
	}
	public void setWeight(Double weight) {
		this.weight = weight;
	}
	public String getDispPrice() {
		return dispPrice;
	}
	public void setDispPrice(String dispPrice) {
		this.dispPrice = dispPrice;
	}
	public Double getWeighKg() {
		return weighKg;
	}
	public void setWeighKg(Double weighKg) {
		this.weighKg = weighKg;
	}
}
