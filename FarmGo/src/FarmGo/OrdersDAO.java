package FarmGo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrdersDAO {
    // Retrieves orders for a given farmer (all statuses)
	public List<Order> getOrdersByFarmer(int farmerId) {
	    List<Order> orders = new ArrayList<>();
	    String sql = "SELECT order_id, retailer_id, farmer_id, total_amount, " +
	                 "status, qty, Pname FROM Orders WHERE farmer_id = ?";
	    
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql)) {
	        
	        ps.setInt(1, farmerId);
	       
	        try (ResultSet rs = ps.executeQuery()) {
	          
	            
	            
	            
	            while (rs.next()) {
	            	
	            	
	                
	                    Order o = new Order();
	                    
	                    o.setOrderId(rs.getInt("ORDER_ID")); // UPPERCASE
	                    o.setRetailerId(rs.getInt("RETAILER_ID"));
	                    o.setFarmerId(rs.getInt("FARMER_ID"));
	                    o.setTotalAmount(rs.getDouble("TOTAL_AMOUNT"));
	                    o.setStatus(rs.getString("STATUS"));
	                    o.setQty(rs.getDouble("QTY"));
	                    o.setPname(rs.getString("PNAME")); // UPPERCASE
	                    orders.add(o);
	                    
	                   
	                }
	          
	            }
	        
	    } catch (Exception e) {
	        System.err.println("Database error:");
	        e.printStackTrace();
	    }
	   
	    return orders;
	}
	
   
 
    public List<Order> getOrdersByRetailer(int retailerId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT order_id, retailer_id, farmer_id, total_amount, status " +
                     "FROM orders " +
                     "WHERE retailer_id = ? " +
                     "ORDER BY order_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, retailerId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order();
                    order.setOrderId(rs.getInt("order_id"));
                    order.setRetailerId(rs.getInt("retailer_id"));
                    order.setFarmerId(rs.getInt("farmer_id"));
                    order.setTotalAmount(rs.getDouble("total_amount"));
                    order.setStatus(rs.getString("status"));
                    orders.add(order);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orders;
    }
    public boolean createOrder(Order order) {
        String sql = "INSERT INTO orders (order_id, retailer_id, farmer_id, total_amount, status,qty,pname) " +
                     "VALUES (orders_seq.NEXTVAL, ?, ?, ?, ?,?,?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            // Set parameters
            ps.setInt(1, order.getRetailerId());
            ps.setInt(2, order.getFarmerId());
            ps.setDouble(3, order.getTotalAmount());
            ps.setString(4, order.getStatus());
            ps.setDouble(5, order.getQty());
            ps.setString(6,order.getPname());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException|ClassNotFoundException  e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean updateOrderStatus(int orderId, String status) throws SQLException {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            stmt.setInt(2, orderId);
            
            return stmt.executeUpdate() > 0;
        }
        catch(ClassNotFoundException e) {
        	return false;
        	
        }
    }

    public Order getOrderById(int orderId) throws Exception {
        String sql = "SELECT * FROM orders WHERE order_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            
            if(rs.next()) {
                Order order = new Order();
                order.setOrderId(rs.getInt("order_id"));
                order.setRetailerId(rs.getInt("retailer_id"));
                order.setFarmerId(rs.getInt("farmer_id"));
                order.setTotalAmount(rs.getDouble("total_amount"));
                order.setStatus(rs.getString("status"));
                order.setQty(rs.getDouble("qty"));
                
                return order;
            }
            return null;
        }
        catch(ClassNotFoundException e) {
        	return null;
        	
        }
        
    }
    public List<Order> getAcceptedOrdersByRetailer(int farmerId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT order_id, retailer_id, farmer_id, total_amount, status, qty, pname " +
                     "FROM Orders WHERE retailer_id = ? AND status = 'Accepted'";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, farmerId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Order o = new Order();
                o.setOrderId(rs.getInt("order_id"));
                o.setRetailerId(rs.getInt("retailer_id"));
                o.setFarmerId(rs.getInt("farmer_id"));
                o.setTotalAmount(rs.getDouble("total_amount"));
                o.setStatus(rs.getString("status"));
                o.setQty(rs.getDouble("qty"));
                o.setPname(rs.getString("pname"));
                orders.add(o);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orders;
    }
    public List<Order> getOrdersByStatus(String status) throws SQLException, ClassNotFoundException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE status = ?";
        
        try (Connection conn = DBConnection.getConnection(); // Use your DBConnection class
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Order order = new Order();
                order.setOrderId(rs.getInt("order_id"));
                order.setRetailerId(rs.getInt("retailer_id"));
                order.setFarmerId(rs.getInt("farmer_id"));
                order.setTotalAmount(rs.getDouble("total_amount"));
                order.setStatus(rs.getString("status"));
                order.setQty(rs.getDouble("qty"));
                order.setPname(rs.getString("pname"));
                orders.add(order);
            }
        }
        return orders;
    }
   
   
 
}
