package FarmGo;

public class Order {
    private int orderId;
    private int retailerId;
    private int farmerId;
    private double totalAmount;
    private String status;
    private double qty;
    private String pname;
    
    
    // Getters and Setters
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    
    public int getRetailerId() { return retailerId; }
    public void setRetailerId(int retailerId) { this.retailerId = retailerId; }
    
    public int getFarmerId() { return farmerId; }
    public void setFarmerId(int farmerId) { this.farmerId = farmerId; }
    
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
	public double getQty() {
		return qty;
	}
	public void setQty(double qty) {
		this.qty = qty;
		
	}
	public String getPname() {
		return pname;
	}
	public void setPname(String pname) {
		this.pname = pname;
	}
	
	
	
    
	
}

