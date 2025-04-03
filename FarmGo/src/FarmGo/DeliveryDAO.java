package FarmGo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;

public class DeliveryDAO {
    // Mark a single order as "Sent for Delivery"
	public boolean markForDelivery(int orderId) throws SQLException, ClassNotFoundException {
	    String addressSql = "SELECT u.address FROM users u " +
	                        "JOIN orders o ON o.retailer_id = u.user_id " +
	                        "WHERE o.order_id = ?";
	    
	    String updateSql = "UPDATE orders SET status = 'Sent for Delivery' WHERE order_id = ?";
	    String insertSql = "INSERT INTO delivery (delivery_id, order_id, address) VALUES (delivery_seq.NEXTVAL, ?, ?)";

	    try (Connection conn = DBConnection.getConnection()) {
	        conn.setAutoCommit(false);

	        // Step 1: Get retailer's address
	        String address;
	        try (PreparedStatement addressPs = conn.prepareStatement(addressSql)) {
	            addressPs.setInt(1, orderId);
	            ResultSet rs = addressPs.executeQuery();
	            if (!rs.next()) {
	                conn.rollback();
	                return false; // No address found
	            }
	            address = rs.getString("address");
	        }

	        // Step 2: Update order status
	        try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
	            updatePs.setInt(1, orderId);
	            int updated = updatePs.executeUpdate();
	            if (updated == 0) {
	                conn.rollback();
	                return false; // Order not found
	            }
	        }

	        // Step 3: Insert delivery record
	        try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
	            insertPs.setInt(1, orderId);
	            insertPs.setString(2, address);
	            insertPs.executeUpdate();
	        }

	        conn.commit();
	        return true;
	    }
	}
	public boolean markAsDelivered(int orderId) throws SQLException, ClassNotFoundException {
	    String updateOrderSql = "UPDATE orders SET status = 'Delivered' WHERE order_id = ?";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement ps = conn.prepareStatement(updateOrderSql)) {
	        ps.setInt(1, orderId);
	        return ps.executeUpdate() > 0;
	    }
	}
	public List<Delivery> getAllDeliveries() throws SQLException, ClassNotFoundException {
	    List<Delivery> deliveries = new ArrayList<>();
	    String sql = "SELECT delivery_id, order_id, address FROM delivery";
	    
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql)) {
	        ResultSet rs = ps.executeQuery();
	        int c=0;
	        while (rs.next()) {
	        	c++;
	            Delivery delivery = new Delivery();
	            delivery.setDeliveryId(rs.getInt("delivery_id"));
	            delivery.setOrderId(rs.getInt("order_id"));
	            delivery.setAddress(rs.getString("address"));
	            deliveries.add(delivery);
	        }
	        System.out.println(c);
	    }
	    return deliveries;
	}
	public String checkDelivery(int orderId) throws SQLException, ClassNotFoundException {
	    String sql = "SELECT status FROM orders WHERE order_id = ?";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setInt(1, orderId);
	        ResultSet rs = ps.executeQuery();
	        return rs.next() ? rs.getString("status") : null;
	    }
	}
}