package FarmGo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewsDAO {
    public List<Review> getReviewsForUser(int farmerId) {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT review_id, order_id, retailer_id, farmer_id, rating, review_comment FROM Reviews WHERE retailer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, farmerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Review r = new Review();
                r.setReviewId(rs.getInt("review_id"));
                r.setOrderId(rs.getInt("order_id"));
                r.setRetailerId(rs.getInt("retailer_id"));
                r.setFarmerId(rs.getInt("farmer_id"));
                r.setRating(rs.getInt("rating"));
                r.setReviewComment(rs.getString("review_comment"));
                reviews.add(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reviews;
    }
    public List<Review> getReviewsByFarmer(int farmerId) throws SQLException, ClassNotFoundException {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT review_id, order_id, retailer_id, rating, review_comment " +
                     "FROM Reviews WHERE farmer_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, farmerId);
            ResultSet rs = ps.executeQuery();
            
            while(rs.next()) {
                Review review = new Review();
                review.setReviewId(rs.getInt("review_id"));
                review.setOrderId(rs.getInt("order_id"));
                review.setRetailerId(rs.getInt("retailer_id"));
                review.setRating(rs.getInt("rating"));
     review.setReviewComment(rs.getString("review_comment"));
                reviews.add(review);
            }
        }
        return reviews;
    }}
