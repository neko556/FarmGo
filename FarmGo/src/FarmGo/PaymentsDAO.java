package FarmGo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentsDAO {
    public List<Payment> getPaymentsForUser(int farmerId) {
        List<Payment> payments = new ArrayList<>();
       
        String sql = "SELECT p.payment_id, p.order_id, p.retailer_id, p.amount, p.payment_method, p.status " +
                     "FROM Payments p JOIN Orders o ON p.order_id = o.order_id " +
                     "WHERE o.farmer_id = ?  ";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, farmerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Payment p = new Payment();
                p.setPaymentId(rs.getInt("payment_id"));
                p.setOrderId(rs.getInt("order_id"));
                p.setRetailerId(rs.getInt("retailer_id"));
                p.setAmount(rs.getDouble("amount"));
                p.setPaymentMethod(rs.getString("payment_method"));
                p.setStatus(rs.getString("status"));
                payments.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return payments;
    }
    public List<Order> loadAcceptedOrdersForPayment(int farmerId) {
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
    
    public boolean createPayment(int orderId, int retailerId, double amount, String paymentMethod, String status) throws SQLException,ClassNotFoundException{
        if (paymentExists(orderId)) {
            return false; 
        }

        String insertSql = "INSERT INTO payments (PAYMENT_ID,ORDER_ID, RETAILER_ID, AMOUNT, PAYMENT_METHOD, STATUS) VALUES (payment_seq.nextval,?, ?, ?, ?, ?)";
        String updateSql = "UPDATE orders SET STATUS = 'Paid' WHERE ORDER_ID = ?"; 
        try (Connection conn = DBConnection.getConnection();
        		 PreparedStatement insertPs = conn.prepareStatement(insertSql);
                PreparedStatement updatePs = conn.prepareStatement(updateSql)) {

              
               insertPs.setInt(1, orderId);       
               insertPs.setInt(2, retailerId);    
               insertPs.setDouble(3, amount);     
               insertPs.setString(4, paymentMethod); 
               insertPs.setString(5, status);    
               insertPs.executeUpdate();

              
               updatePs.setInt(1, orderId);
               updatePs.executeUpdate();

               return true; 
           } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error creating payment: " + e.getMessage());
        }
    }

    // Method to check if a payment already exists for the given orderId
    private boolean paymentExists(int orderId) throws SQLException,ClassNotFoundException {
        String sql = "SELECT COUNT(*) FROM payments WHERE ORDER_ID = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // Return true if count is greater than 0
            }
        }
        return false; // Return false if no payment exists
    }
   
}
