package FarmGo;
public class Delivery {
    private int deliveryId;
    private int orderId;
    private String address;

    // Getters and Setters
    public int getDeliveryId() { return deliveryId; }
    public void setDeliveryId(int deliveryId) { this.deliveryId = deliveryId; }
    
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}