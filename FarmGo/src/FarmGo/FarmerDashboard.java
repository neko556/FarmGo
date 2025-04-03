package FarmGo;

import javax.swing.*;

import javax.swing.border.*;
import java.awt.*;

import java.awt.image.BufferedImage;
import java.util.List;
import java.sql.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.table.DefaultTableModel;


import java.awt.event.ActionListener;

public class FarmerDashboard extends JFrame {
    private int currentFarmerId;
    private JPanel producePanel;
    private JPanel ordersPanel;
    
  
    private JTabbedPane tabbedPane;
    
    
    private JButton addProductButton;
    private JTable paymentsTable;
    private JTable reviewsTable;
    private JPanel deliveryPanel;

    public FarmerDashboard(String email) {
        setTitle("Farmer Dashboard");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        currentFarmerId = getUserIdByEmail(email);
        
        if(currentFarmerId == -1) {
            JOptionPane.showMessageDialog(this, "User not found!", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        
        tabbedPane = new JTabbedPane();
        initializeTabs();
        add(tabbedPane);
        
        loadAllData();
       
    }

    private void initializeTabs() {
        // Produce Tab
        producePanel = new JPanel();
        producePanel.setLayout(new BoxLayout(producePanel, BoxLayout.Y_AXIS));
        JPanel produceContainer = new JPanel(new BorderLayout());
        produceContainer.add(new JScrollPane(producePanel), BorderLayout.CENTER);
        
        produceContainer.add(new JScrollPane(producePanel), BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addProductButton = new JButton("Add Product");
        addProductButton.addActionListener(e -> showAddProductDialog());
        buttonPanel.add(addProductButton);
        produceContainer.add(buttonPanel, BorderLayout.SOUTH);
        
        tabbedPane.addTab("Produce", produceContainer);
        
        // Orders Tab
        ordersPanel = new JPanel();
        ordersPanel.setLayout(new BoxLayout(ordersPanel, BoxLayout.Y_AXIS));
        JScrollPane ordersScroll = new JScrollPane(ordersPanel);
        ordersScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        tabbedPane.addTab("Orders", ordersScroll);

       
        
        // Payments Tab
        paymentsTable = new JTable();
        paymentsTable.setModel(new DefaultTableModel(
            new Object[]{"Payment ID", "Order ID", "Amount", "Method", "Status"}, 0
        ));
        tabbedPane.addTab("Payments", new JScrollPane(paymentsTable));
        
        
        // Deliveries Tab (New)
        JPanel deliveriesPanel = new JPanel();
        deliveriesPanel.setLayout(new BoxLayout(deliveriesPanel, BoxLayout.Y_AXIS));
        JScrollPane deliveriesScroll = new JScrollPane(deliveriesPanel);
        deliveriesScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        tabbedPane.addTab("Pending Deliveries", deliveriesScroll);
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (tabbedPane.getTitleAt(selectedIndex).equals("Produce")) {
                loadProduce();
            } else if (tabbedPane.getTitleAt(selectedIndex).equals("Deliveries")) {
                loadDeliveries(); // New method to load delivery orders
            }
        });
        
        reviewsTable = new JTable();
        reviewsTable.setModel(new DefaultTableModel(
            new Object[]{"Order ID","Retailer ID", "Rating", "Review", }, 0
        ));
        tabbedPane.addTab("Reviews", new JScrollPane(reviewsTable));
        
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (tabbedPane.getTitleAt(selectedIndex).equals("Produce")) {
                loadProduce(); // Refresh marketplace data
            }
        });
    }

    private JPanel createDeliveryCard(Order order) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        card.setBackground(Color.WHITE);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        // Order Info
        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        addStyledDetail(infoPanel, "Order ID:", order.getOrderId());
        addStyledDetail(infoPanel, "Amount:", String.format("₹%.2f", order.getTotalAmount()));
        addStyledDetail(infoPanel, "Status:", order.getStatus());

        // Delivery Button
        JButton deliverBtn = new JButton("Mark for Delivery");
        deliverBtn.setBackground(new Color(33, 150, 243));
        deliverBtn.setForeground(Color.WHITE);
        deliverBtn.setFocusPainted(false);
        deliverBtn.addActionListener(e -> handleDelivery(order));

        // Assemble
        card.add(infoPanel, BorderLayout.CENTER);
        card.add(deliverBtn, BorderLayout.EAST);

        return card;
    }

    private void addStyledDetail(JPanel panel, String label, Object value) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(100, 100, 100));
        
        JLabel val = new JLabel(value.toString());
        val.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        val.setForeground(new Color(60, 60, 60));
        
        panel.add(lbl);
        panel.add(val);
    }
    private void loadDeliveries() {
        JPanel deliveriesPanel = (JPanel) ((JScrollPane) tabbedPane.getComponentAt(3)).getViewport().getView();
        deliveriesPanel.removeAll();
        
        try {
            OrdersDAO ordersDAO = new OrdersDAO();
            List<Order> deliveryOrders = ordersDAO.getOrdersByStatus("Paid"); // Implement this
            
            for (Order order : deliveryOrders) {
                JPanel card = createDeliveryCard(order);
                deliveriesPanel.add(card);
                deliveriesPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading deliveries: " + e.getMessage());
        }
        
        deliveriesPanel.revalidate();
        deliveriesPanel.repaint();
    }

   
    
 
    private void loadAllData() {
        loadProduce();
        loadOrders();
        loadReviews(currentFarmerId);
        loadPayments();}
       
    private void handleDelivery(Order order) {
        if (!"Paid".equals(order.getStatus())) {
            JOptionPane.showMessageDialog(this, 
                "Order must be in 'Paid' status to mark for delivery.", 
                "Invalid Status", 
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this, 
            "Mark order #" + order.getOrderId() + " for delivery?", 
            "Confirm Delivery", 
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                DeliveryDAO deliveryDAO = new DeliveryDAO();
                boolean success = deliveryDAO.markForDelivery(order.getOrderId());
                
                if (success) {
                    JOptionPane.showMessageDialog(this, 
                        "Order marked for delivery successfully!", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    loadOrders();
                    loadDeliveries();
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Failed to mark for delivery. Address not found.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error: " + e.getMessage(), 
                    "Database Error", 
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
   
   
    private void showAddProductDialog() {
        JDialog dialog = new JDialog(this, "Add New Product", true);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridLayout(9, 2, 5, 5));
        
        // Form fields
        JTextField nameField = new JTextField();
        JTextField categoryField = new JTextField();
        JTextField descField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField unitField = new JTextField();
        JTextField qtyField = new JTextField();
        JTextField locationField = new JTextField();
        JLabel imageLabel = new JLabel("No image selected");
        
        
        JButton uploadBtn = new JButton("Upload Image");
        final byte[][] imageDataWrapper = new byte[1][]; 
        
        // Image upload button action
        uploadBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if(fc.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                try {
                    Path path = fc.getSelectedFile().toPath();
                    imageDataWrapper[0] = Files.readAllBytes(path);
                    imageLabel.setText(fc.getSelectedFile().getName());
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(dialog, "Error reading image file");
                }
            }
        });

        // Add fields to form 
        formPanel.add(new JLabel("Name*:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Category:"));
        formPanel.add(categoryField);
        formPanel.add(new JLabel("Description:"));
        formPanel.add(descField);
        formPanel.add(new JLabel("Price/Unit*:"));
        formPanel.add(priceField);
        formPanel.add(new JLabel("Unit:"));
        formPanel.add(unitField);
        formPanel.add(new JLabel("Quantity*:"));
        formPanel.add(qtyField);
        formPanel.add(new JLabel("Location*:"));
        formPanel.add(locationField);
        formPanel.add(new JLabel("Image:"));
        formPanel.add(uploadBtn);
        formPanel.add(imageLabel);

        // Submit button
        JButton submitBtn = new JButton("Save Product");
        submitBtn.addActionListener(e -> {
            try {
                // Validate required fields
                if(nameField.getText().isEmpty() || 
                   priceField.getText().isEmpty() ||
                   qtyField.getText().isEmpty() ||
                   locationField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please fill all required fields (*)");
                    return;
                }

                Product newProduct = new Product();
                newProduct.setFarmerId(currentFarmerId);
                newProduct.setName(nameField.getText());
                newProduct.setCategory(categoryField.getText());
                newProduct.setDescription(descField.getText());
                newProduct.setPricePerUnit(Double.parseDouble(priceField.getText()));
                newProduct.setUnit(unitField.getText());
                newProduct.setQuantityAvailable(Double.parseDouble(qtyField.getText()));
                newProduct.setLocation(locationField.getText());
                newProduct.setImage(imageDataWrapper[0]);

                ProductsDAO dao = new ProductsDAO();
                if(dao.addProduct(newProduct)) {
                    JOptionPane.showMessageDialog(dialog, "Product added successfully!");
                    dialog.dispose();
                    loadProduce();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to save product");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid number format in price/quantity");
            }
        });

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(submitBtn, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private int getUserIdByEmail(String email) {
        String sql = "SELECT user_id FROM Users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("user_id") : -1;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
            return -1;
        }
    }

    private void loadProduce() {
        producePanel.removeAll();
        try {
            ProductsDAO pdao = new ProductsDAO();
            List<Product> products = pdao.getProductsByFarmer(currentFarmerId);

            for(Product p : products) {
                JPanel productCard = createProductCard(p);
                producePanel.add(productCard);
                producePanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading products: " + e.getMessage());
        }
        producePanel.revalidate();
        producePanel.repaint();
    }

    private JPanel createProductCard(Product p) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Color.LIGHT_GRAY, 1),
            new EmptyBorder(10, 10, 10, 10)
        ));

        // Image Display
        JLabel imageLabel = new JLabel();
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        if(p.getImage() != null && p.getImage().length > 0) {
            try {
                ImageIcon icon = new ImageIcon(p.getImage());
                Image scaledImage = getScaledImage(icon.getImage(), 250, 200);
                imageLabel.setIcon(new ImageIcon(scaledImage));
            } catch (Exception e) {
                imageLabel.setText("Invalid Image");
            }
        } else {
            imageLabel.setText("No Image Available");
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        }
        
        // Product Details
        JPanel detailsPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        detailsPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        addDetail(detailsPanel, "Product ID:", p.getProductId());
        addDetail(detailsPanel, "Name:", p.getName());
        addDetail(detailsPanel, "Category:", p.getCategory());
        addDetail(detailsPanel, "Price:", String.format("₹%.2f/%s", 
            p.getPricePerUnit(), p.getUnit()));
        addDetail(detailsPanel, "Quantity:", p.getQuantityAvailable());
        addDetail(detailsPanel, "Location:", p.getLocation());

        card.add(imageLabel);
        card.add(detailsPanel);
        
        return card;
    }

    private Image getScaledImage(Image src, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resized.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(src, 0, 0, width, height, null);
        g2.dispose();
        return resized;
    }

    private void addDetail(JPanel panel, String label, Object value) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        panel.add(lbl);
        panel.add(new JLabel(value.toString()));
    }
    private JButton createActionButton(String text, Color bg, ActionListener action) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        btn.addActionListener(action);
        return btn;
    }
    
    private void addOrderDetail(JPanel panel, String label, Object value) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        panel.add(lbl);
        panel.add(new JLabel(value.toString()));
    }
    private void handleOrderAction(Order order, String status) {
        try {
        	if ("Request Sent".equals(status)) {
        	    ProductsDAO pdao = new ProductsDAO();
        	    System.out.println(order.getFarmerId() +  order.getPname());
        	    
        	    if (!pdao.productExists(order.getFarmerId(), order.getPname())) {
        	        JOptionPane.showMessageDialog(this, "Product not found!");
        	        return;
        	    }
        	    
        	    if (!pdao.hasSufficientStock(order.getFarmerId(), order.getPname(), order.getQty())) {
        	        JOptionPane.showMessageDialog(this, "Insufficient stock!");
        	        return;
        	    }
        	    
        	    boolean success = pdao.updateProductQuantity(
        	        order.getFarmerId(), order.getPname(), -order.getQty()
        	    );
        	    
        	    if (!success) {
        	        JOptionPane.showMessageDialog(this, "Failed to update product quantity!");
        	        return;
        	    }
        	}

            // Update order status
            OrdersDAO odao = new OrdersDAO();
            if (odao.updateOrderStatus(order.getOrderId(), "Accepted")) {
                JOptionPane.showMessageDialog(this, "Order status updated to: " + "Accepted");
                loadOrders(); // Refresh the view
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update order status!");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    private void loadReviews(int farmerId) {
        DefaultTableModel model = (DefaultTableModel) reviewsTable.getModel();
        model.setRowCount(0); // Clear existing data
        
        try {
            ReviewsDAO rdao = new ReviewsDAO();
            List<Review> reviews = rdao.getReviewsByFarmer(farmerId);
            
            for (Review r : reviews) {
                model.addRow(new Object[]{
                    r.getOrderId(),
                    r.getRetailerId(),
                    createStarRating(r.getRating()), 
                    r.getReviewComment(),
                   
                });
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading reviews: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

 
   
   


    private void loadOrders() {
       
        try {
            OrdersDAO odao = new OrdersDAO();
            List<Order> orders = odao.getOrdersByFarmer(currentFarmerId);

            for(Order o : orders) {
                JPanel card = createOrderCard(o);
                ordersPanel.add(card);
                ordersPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading orders: " + e.getMessage());
        }
        ordersPanel.revalidate();
        ordersPanel.repaint();
    }
    private JPanel createOrderCard(Order order) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Color.LIGHT_GRAY, 1),
            new EmptyBorder(10, 10, 10, 10)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        // Left side - Order Info
        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        addOrderDetail(infoPanel, "Order ID:", order.getOrderId());
        addOrderDetail(infoPanel, "Retailer ID:", order.getRetailerId());
        addOrderDetail(infoPanel, "Total Amount:", String.format("₹%.2f", order.getTotalAmount()));
        addOrderDetail(infoPanel, "Status:", order.getStatus());

        // Right side - Actions
        JPanel actionPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        JButton acceptBtn = createActionButton("Accept", Color.GREEN, e ->handleOrderAction(order, "Request Sent"));
        JButton declineBtn = createActionButton("Decline", Color.RED, e ->handleOrderAction(order, "Declined"));
        
        // Check for null pname
        
        if (!"Request Sent".equals(order.getStatus())) {
            acceptBtn.setVisible(false);
            declineBtn.setVisible(false);
        }
        actionPanel.add(acceptBtn);
        actionPanel.add(declineBtn);

        card.add(infoPanel, BorderLayout.CENTER);
        card.add(actionPanel, BorderLayout.EAST);

        return card;
    }
    
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

  
    private String createStarRating(int rating) {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            stars.append(i < rating ? "★" : "☆");
        }
        return stars.toString();
    }
    private void loadPayments() {
    	 DefaultTableModel model = (DefaultTableModel) paymentsTable.getModel();
    	    model.setRowCount(0); // Clear existing data
    	    try {
    	        PaymentsDAO pdao = new PaymentsDAO();
    	        List<Payment> payments = pdao.getPaymentsForUser(currentFarmerId);
    	        
    	        for(Payment p : payments) {
    	            model.addRow(new Object[]{
    	                p.getPaymentId(),
    	                p.getOrderId(),
    	                String.format("₹%.2f", p.getAmount()),
    	                p.getPaymentMethod(),
    	                p.getStatus()
    	            });
    	        }
    	    } 
         catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading payments: " + e.getMessage());
        }
    }

    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new FarmerDashboard("test@farmer.com").setVisible(true);
        });
    }
}