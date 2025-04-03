package FarmGo;

public class Product {
    private int productId;
    private int farmerId;
    private String name;
    private String category;
    private String description;
    private double pricePerUnit;
    private String unit;
    private double quantityAvailable;
    private byte[] image;
   
    private String location;
    
    // Getters and Setters
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    
    public int getFarmerId() { return farmerId; }
    public void setFarmerId(int farmerId) { this.farmerId = farmerId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public double getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(double pricePerUnit) { this.pricePerUnit = pricePerUnit; }
    
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    
    public double getQuantityAvailable() { return quantityAvailable; }
    public void setQuantityAvailable(double quantityAvailable) { this.quantityAvailable = quantityAvailable; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public byte[] getImage() { return image; }
    public void setImage(byte[] image) { this.image = image; }
}
