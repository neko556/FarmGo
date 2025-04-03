package FarmGo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class ProductsDAO {
    public List<Product> getProductsByFarmer(int farmerId) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT product_id, farmer_id, name, category, description, " +
                     "price_per_unit, unit, quantity_available, location, image " +
                     "FROM Products WHERE farmer_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, farmerId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Product p = new Product();
                p.setProductId(rs.getInt("product_id"));
                p.setFarmerId(rs.getInt("farmer_id"));
                p.setName(rs.getString("name"));
                p.setCategory(rs.getString("category"));
                p.setDescription(rs.getString("description"));
                p.setPricePerUnit(rs.getDouble("price_per_unit"));
                p.setUnit(rs.getString("unit"));
                p.setQuantityAvailable(rs.getDouble("quantity_available"));
                p.setLocation(rs.getString("location"));
                
                // Handle image blob
                Blob imageBlob = rs.getBlob("image");
                if(imageBlob != null) {
                    p.setImage(imageBlob.getBytes(1, (int) imageBlob.length()));
                }
                
                products.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return products;
    }

    public List<Product> getAllAvailableProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT product_id, farmer_id, name, category, description, " +
                     "price_per_unit, unit, quantity_available, location, image " +
                     "FROM products " +
                     "WHERE quantity_available > 0 " +
                     "ORDER BY name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Product p = new Product();
                p.setProductId(rs.getInt("product_id"));
                p.setFarmerId(rs.getInt("farmer_id"));
                p.setName(rs.getString("name"));
                p.setCategory(rs.getString("category"));
                p.setDescription(rs.getString("description"));
                p.setPricePerUnit(rs.getDouble("price_per_unit"));
                p.setUnit(rs.getString("unit"));
                p.setQuantityAvailable(rs.getDouble("quantity_available"));
                p.setLocation(rs.getString("location"));
                
                
                // Handle image blob
                Blob imageBlob = rs.getBlob("image");
                if(imageBlob != null) {
                    p.setImage(imageBlob.getBytes(1, (int) imageBlob.length()));
                }
                
                products.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return products;
    }
    public boolean addProduct(Product product) {
        String sql = "INSERT INTO products (PRODUCT_ID, FARMER_ID, NAME, CATEGORY, " +
                     "DESCRIPTION, PRICE_PER_UNIT, UNIT, QUANTITY_AVAILABLE, LOCATION, IMAGE) " +
                     "VALUES (products_seq.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            // Set parameters according to table structure
            ps.setInt(1, product.getFarmerId());
            ps.setString(2, product.getName());
            ps.setString(3, product.getCategory());
            ps.setString(4, product.getDescription());
            ps.setDouble(5, product.getPricePerUnit());
            ps.setString(6, product.getUnit());
            ps.setDouble(7, product.getQuantityAvailable());
            ps.setString(8, product.getLocation());
            
            // Handle BLOB (image)
            if (product.getImage() != null && product.getImage().length > 0) {
                ps.setBytes(9, product.getImage());
            } else {
                ps.setNull(9, Types.BLOB);
            }

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean hasSufficientStock(int farmerId, String productName, double qty) throws SQLException,ClassNotFoundException {
        String sql = "SELECT quantity_available FROM products WHERE farmer_id = ? AND name = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, farmerId);
            ps.setString(2, productName);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getDouble(1) >= qty;
        }}
    public boolean productExists(int farmerId, String productName) throws SQLException,ClassNotFoundException {
        String sql = "SELECT COUNT(*) FROM products WHERE farmer_id = ? AND name = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, farmerId);
            ps.setString(2, productName);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }
  

 boolean updateProductQuantity(int farmerId, String productName, double quantityChange) 
	    throws SQLException, ClassNotFoundException {
	    
	    String sql = "UPDATE products SET quantity_available = quantity_available + ? " 
	               + "WHERE farmer_id = ? AND LOWER(name) = LOWER(?)";
	    
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {
	        
	        stmt.setDouble(1, quantityChange);
	        stmt.setInt(2, farmerId);
	        stmt.setString(3, productName);
	        
	        return stmt.executeUpdate() > 0;
	    }
	}
}