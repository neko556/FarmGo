package FarmGo;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.sql.*;
import java.util.List;
import javax.swing.table.DefaultTableModel;




public class RetailerDashboard extends JFrame {
    private int currentRetailerId;
    private JPanel producePanel;
    private JTable ordersTable;
   
    private JTabbedPane tabbedPane;
    private JPanel deliveriesPanel;
   
    
    private JPanel paymentsPanel;
   
    private JTable reviewsTable;
    
    public RetailerDashboard(String email) {
        setTitle("Retailer Dashboard");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        currentRetailerId = getUserIdByEmail(email);
        
        if(currentRetailerId == -1) {
            JOptionPane.showMessageDialog(this, "User  not found!", "Error", JOptionPane.ERROR_MESSAGE);
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
        producePanel = new JPanel(new GridLayout(0, 3, 15, 15));
        producePanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        JScrollPane produceScroll = new JScrollPane(producePanel);
        tabbedPane.addTab("Marketplace", produceScroll);
       
        ordersTable = new JTable();
        ordersTable.setModel(new DefaultTableModel(
            new Object[]{"Order ID", "Farmer ID", "Total", "Status"}, 0
        ));
        JScrollPane ordersScroll = new JScrollPane(ordersTable);
        tabbedPane.addTab("My Orders", ordersScroll);

        // Add change listener
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            String tabTitle = tabbedPane.getTitleAt(selectedIndex);
            
            if ("My Orders".equals(tabTitle)) {
                loadOrders(); // Refresh orders table
            } else if ("Produce".equals(tabTitle)) {
                loadProduce();
            } else if ("Deliveries".equals(tabTitle)) {
                loadDeliveries();
            }
        });
        
       
        
        // Payments Tab
        paymentsPanel = new JPanel();
        paymentsPanel.setLayout(new BoxLayout(paymentsPanel, BoxLayout.Y_AXIS));
        tabbedPane.addTab("Pending Payments", new JScrollPane(paymentsPanel));
        
        
     // Deliveries Tab
        deliveriesPanel = new JPanel();
        deliveriesPanel.setLayout(new BoxLayout(deliveriesPanel, BoxLayout.Y_AXIS));
        JScrollPane deliveriesScroll = new JScrollPane(deliveriesPanel);
        deliveriesScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        tabbedPane.addTab("Deliveries", deliveriesScroll);

        // Change listener for tab selection
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            String tabTitle = tabbedPane.getTitleAt(selectedIndex);
            
            if ("Produce".equals(tabTitle)) {
                loadProduce();
            } else if ("Deliveries".equals(tabTitle)) {
                loadDeliveries();
            }
        });
        
        
        reviewsTable = new JTable();
        reviewsTable.setModel(new DefaultTableModel(
            new Object[]{"Order ID", "Retailer ID", "Rating", "Review"}, 0
        ));
        tabbedPane.addTab("Reviews", new JScrollPane(reviewsTable));
        
       
        
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (tabbedPane.getTitleAt(selectedIndex).equals("Marketplace")) {
                loadProduce(); // Refresh marketplace data
            }
        });
       
    }

   

    private void loadAllData() {
        loadProduce();
        loadOrders();
        loadPayments(); 
        loadReviews(currentRetailerId);
    }

    private void loadProduce() {
        producePanel.removeAll();
        try {
            ProductsDAO pdao = new ProductsDAO();
            List<Product> products = pdao.getAllAvailableProducts();
            
            for(Product p : products) {
                JPanel productCard = createProductCard(p);
                producePanel.add(productCard);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading products: " + e.getMessage());
        }
        producePanel.revalidate();
        producePanel.repaint();
    }

    private void loadOrders() {
        DefaultTableModel model = (DefaultTableModel) ordersTable.getModel();
        model.setRowCount(0);
        try {
            OrdersDAO odao = new OrdersDAO();
            List<Order> orders = odao.getOrdersByRetailer(currentRetailerId);
            for(Order o : orders) {
                model.addRow(new Object[]{
                    o.getOrderId(),
                    o.getFarmerId(),
                    String.format("₹%.2f", o.getTotalAmount()),
                    o.getStatus()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading orders: " + e.getMessage());
        }
    }

    private void loadPayments() {
        paymentsPanel.removeAll();
        try {
            OrdersDAO odao = new OrdersDAO();
            List<Order> acceptedOrders = odao.getAcceptedOrdersByRetailer(currentRetailerId);
            

            for (Order order : acceptedOrders) {
                JPanel paymentCard = createPaymentCard(order);
                paymentsPanel.add(paymentCard);
                paymentsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading payments: " + e.getMessage());
        }
        paymentsPanel.revalidate();
        paymentsPanel.repaint();
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
    private void showCancelPaymentDialog(Order order) {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to cancel this payment?",
            "Confirm Cancellation",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            cancelPayment(order);
        }
    }
    private String createStarRating(int rating) {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            stars.append(i < rating ? "★" : "☆");
        }
        return stars.toString();
    }
    private void cancelPayment(Order order) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Update Payments table
            String paymentSql = "UPDATE Payments SET status = 'Cancelled' WHERE order_id = ?";
            try (PreparedStatement paymentPs = conn.prepareStatement(paymentSql)) {
            	ProductsDAO pdo=new ProductsDAO();
            	pdo.updateProductQuantity(order.getFarmerId(),order.getPname(),order.getQty());
                paymentPs.setInt(1, order.getOrderId());
                int paymentRows = paymentPs.executeUpdate();
                
                if (paymentRows == 0) {
                    JOptionPane.showMessageDialog(this, "No payment found for this order!");
                    conn.rollback();
                    return;
                }
            }

            // Update Orders table
            String orderSql = "UPDATE Orders SET status = 'Cancelled' WHERE order_id = ?";
            try (PreparedStatement orderPs = conn.prepareStatement(orderSql)) {
                orderPs.setInt(1, order.getOrderId());
                int orderRows = orderPs.executeUpdate();
                
                if (orderRows == 0) {
                    JOptionPane.showMessageDialog(this, "No order found!");
                    conn.rollback();
                    return;
                }
            }

            conn.commit(); // Commit transaction
            JOptionPane.showMessageDialog(this, "Payment and Order cancelled successfully!");
            
            // Refresh both views
            loadPayments();
            loadOrders();

        } catch (SQLException| ClassNotFoundException  e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException  ex) {
                ex.printStackTrace();
            }
            JOptionPane.showMessageDialog(this, "Error cancelling payment: " + e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true); // Reset auto-commit
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    private JPanel createPaymentCard(Order order) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.LIGHT_GRAY, 1),
                new EmptyBorder(10, 10, 10, 10)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        // Left side - Order Info
        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        addDetail(infoPanel, "Order ID:", order.getOrderId());
        addDetail(infoPanel, "Farmer ID:", order.getFarmerId());
        addDetail(infoPanel, "Total Amount:", String.format("₹%.2f", order.getTotalAmount()));
        addDetail(infoPanel, "Status:", order.getStatus());

        // Right side - Actions
        JPanel actionPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        JButton payBtn = createActionButton("Pay", Color.GREEN, e -> showPaymentDialog(order));
        JButton cancelBtn = createActionButton("Cancel",Color.RED,e->showCancelPaymentDialog(order));
        actionPanel.add(payBtn);
        actionPanel.add(cancelBtn);

        card.add(infoPanel, BorderLayout.CENTER);
        card.add(actionPanel, BorderLayout.EAST);

        return card;
    }

    private void showPaymentDialog(Order order) {
        String[] paymentMethods = {"UPI", "Credit Card", "Cash"};
        String selectedMethod = (String) JOptionPane.showInputDialog(
                this,
                "Select Payment Method:",
                "Payment",
                JOptionPane.QUESTION_MESSAGE,
                null,
                paymentMethods,
                paymentMethods[0]);

        if (selectedMethod != null) {
        	try {
            makePayment(order, selectedMethod);}
        	catch(SQLException | ClassNotFoundException e) {
        		JOptionPane.showMessageDialog(this,"Invalid Payment");
        	}
        }
    }

    private void makePayment(Order order, String paymentMethod) throws SQLException,ClassNotFoundException{
        try {
            PaymentsDAO paymentsDAO = new PaymentsDAO();
            boolean paymentCreated = paymentsDAO.createPayment(
                order.getOrderId(),
                currentRetailerId,
                order.getTotalAmount(),
                paymentMethod,
                "Paid"
            );

            if (paymentCreated) {
                JOptionPane.showMessageDialog(this, "Payment successful!");
                loadPayments(); // Refresh payments list
            } else {
                JOptionPane.showMessageDialog(this, "Payment already exists for this order.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error processing payment: " + e.getMessage());
        }
        loadPayments();
    }
    private void addDetail(JPanel panel, String label, Object value) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        panel.add(lbl);
        panel.add(new JLabel(value.toString()));
    }

    private JPanel createProductCard(Product p) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(10, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setBackground(Color.WHITE);

        // Image Display
        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        if (p.getImage() != null && p.getImage().length > 0) {
            try {
                ImageIcon icon = new ImageIcon(p.getImage());
                Image scaledImage = getScaledImage(icon.getImage(), 200, 150);
                imageLabel.setIcon(new ImageIcon(scaledImage));
            } catch (Exception e) {
                imageLabel.setText("Image Not Available");
            }
        } else {
            imageLabel.setText("No Image");
        }

        // Product Details
        JPanel detailsPanel = new JPanel(new GridLayout(0, 2, 6, 6));
        detailsPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        addDetail(detailsPanel, "Name", p.getName());
        addDetail(detailsPanel, "Description", p.getDescription());
        addDetail(detailsPanel, "Price", String.format("₹%.2f/%s", p.getPricePerUnit(), p.getUnit()));
        addDetail(detailsPanel, "Available", String.format("%.2f %s", p.getQuantityAvailable(), p.getUnit()));
        addDetail(detailsPanel, "Location", p.getLocation());

        // Buy Button
        JButton buyButton = new JButton("Purchase");
        buyButton.setBackground(new Color(76, 175, 80));
        buyButton.setForeground(Color.WHITE);
        buyButton.setFocusPainted(false);
        System.out.println("showPurchaseDialog"+ p.getName());
        buyButton.addActionListener(e -> showPurchaseDialog(p, p.getName()));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        buttonPanel.add(buyButton);

        card.add(imageLabel, BorderLayout.NORTH);
        card.add(detailsPanel, BorderLayout.CENTER);
        card.add(buttonPanel, BorderLayout.SOUTH);

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

    private void showPurchaseDialog(Product product, String pname) {
        JDialog dialog = new JDialog(this, "Purchase Product", true);
        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel quantityLabel = new JLabel("Quantity:");
        JTextField quantityField = new JTextField();
        JLabel totalLabel = new JLabel("Total:");
        JLabel totalValue = new JLabel();

        quantityField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateTotal(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateTotal(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateTotal(); }

            private void updateTotal() {
                try {
                    double qty = Double.parseDouble(quantityField.getText());
                    double total = qty * product.getPricePerUnit();
                    totalValue.setText(String.format("₹%.2f", total));
                } catch (NumberFormatException ex) {
                    totalValue.setText("-");
                }
            }
        });

        JButton confirmButton = new JButton("Confirm Order");
        confirmButton.addActionListener(e -> {
            try {
                double quantity = Double.parseDouble(quantityField.getText());
                if (quantity > 0 && quantity <= product.getQuantityAvailable()) {
                    Order newOrder = new Order();
                    newOrder.setPname(product.getName());
                    System.out.println("Setting product name: " + product.getName());
                    newOrder.setRetailerId(currentRetailerId);
                    newOrder.setFarmerId(product.getFarmerId());
                    newOrder.setTotalAmount(quantity * product.getPricePerUnit());
                    newOrder.setStatus("Request Sent");
                   
                    newOrder.setQty(quantity);

                    OrdersDAO ordersDAO = new OrdersDAO();
                    if (ordersDAO.createOrder(newOrder)) {
                        JOptionPane.showMessageDialog(dialog, "Order request sent successfully!");
                        dialog.dispose();
                        loadOrders(); // Refresh orders list
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Failed to create order!");
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid quantity!");
            }
        });

        panel.add(quantityLabel);
        panel.add(quantityField);
        panel.add(totalLabel);
        panel.add(totalValue);
        panel.add(new JLabel());
        panel.add(confirmButton);

        dialog.add(panel);
        dialog.setVisible(true);
    }
  
   
   
    


    
    private void loadReviews(int farmerId) {
        DefaultTableModel model = (DefaultTableModel) reviewsTable.getModel();
        model.setRowCount(0); // Clear existing data
        
        try {
            ReviewsDAO rdao = new ReviewsDAO();
            List<Review> reviews = rdao.getReviewsForUser(farmerId);
            
            for (Review r : reviews) {
                model.addRow(new Object[]{
                    r.getOrderId(),
                    r.getRetailerId(),
                    createStarRating(r.getRating()), // Returns a string like "★★★☆☆"
                    r.getReviewComment(),
                   
                });
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading reviews: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private JPanel createDeliveryCard(Delivery delivery) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        card.setBackground(Color.WHITE);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        // Delivery Info
        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        addDetail(infoPanel, "Delivery ID:", delivery.getDeliveryId());
        addDetail(infoPanel, "Order ID:", delivery.getOrderId());
        addDetail(infoPanel, "Address:", delivery.getAddress());

        // Action Button
        JButton receivedBtn = new JButton("Mark as Delivered");
        receivedBtn.setBackground(new Color(76, 175, 80)); // Green
        receivedBtn.setForeground(Color.WHITE);
        receivedBtn.setFocusPainted(false);

        try {
            DeliveryDAO deliveryDAO = new DeliveryDAO();
            String status = deliveryDAO.checkDelivery(delivery.getOrderId());
            receivedBtn.setVisible("Sent for Delivery".equalsIgnoreCase(status));
        } catch (SQLException | ClassNotFoundException e) {
            receivedBtn.setVisible(false); // Hide button on error
            e.printStackTrace();
        }

        receivedBtn.addActionListener(e -> handleDeliveryReceived(delivery.getOrderId()));

        // Assemble
        card.add(infoPanel, BorderLayout.CENTER);
        card.add(receivedBtn, BorderLayout.EAST);

        return card;
    }
    private void handleDeliveryReceived(int orderId) {
        try {
            DeliveryDAO deliveryDAO = new DeliveryDAO();
            String currentStatus = deliveryDAO.checkDelivery(orderId);
            
            if (!"Sent for Delivery".equalsIgnoreCase(currentStatus)) {
                JOptionPane.showMessageDialog(this, 
                    "Order is not in 'Sent for Delivery' status!", 
                    "Invalid Action", 
                    JOptionPane.WARNING_MESSAGE
                );
                return;	
            }

            int confirm = JOptionPane.showConfirmDialog(
                this, 
                "Mark Order #" + orderId + " as delivered?", 
                "Confirm Delivery", 
                JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = deliveryDAO.markAsDelivered(orderId);
                if (success) {
                    JOptionPane.showMessageDialog(this, 
                        "Order marked as delivered!", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    loadDeliveries(); // Refresh the deliveries tab
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Failed to update delivery status.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, 
                "Database error: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    private void loadDeliveries() {
        deliveriesPanel.removeAll();
        
        try {
            DeliveryDAO deliveryDAO = new DeliveryDAO();
            List<Delivery> deliveries = deliveryDAO.getAllDeliveries();
            
            for (Delivery delivery : deliveries) {
                JPanel card = createDeliveryCard(delivery);
                deliveriesPanel.add(card);
                deliveriesPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        } catch (SQLException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, 
     "Error loading deliveries: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE
            );
        }
        
        deliveriesPanel.revalidate();
        deliveriesPanel.repaint();
    }
  
    private int getUserIdByEmail(String email) {
        String sql = "SELECT user_id FROM Users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("user_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new RetailerDashboard("retailer@example.com").setVisible(true);
        });
    }
}