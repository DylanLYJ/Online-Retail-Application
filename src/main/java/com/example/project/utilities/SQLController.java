package com.example.project.utilities;

import com.example.project.items.Order;
import com.example.project.items.Product;
import com.example.project.items.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import java.sql.*;
import java.sql.Date;
import java.util.*;

public class SQLController {
    private static SQLController sqlController;
    private static Connection conn;

    private static final String url = "jdbc:sqlite:gamestore.db";

    private SQLController() {
        conn = connectDatabase();
    }

    public static SQLController getController() {
        if (sqlController == null) {
            sqlController = new SQLController();
        }
        return sqlController;
    }

    public Connection connectDatabase() {
        Connection conn = null;

        //attempt connection
        try {
            conn = DriverManager.getConnection(url);
            if (isDatabaseEmpty(conn)) {
                createDatabase();
            }

            Statement foreignKeyEnable = conn.createStatement();
            foreignKeyEnable.execute("PRAGMA foreign_keys = ON");
            foreignKeyEnable.close();

        } catch (SQLException e) {
             e.printStackTrace();
             System.out.println("Error connecting to database: " + e.getMessage());
        }
        return conn;
    }

    private boolean isDatabaseEmpty(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // Query to check if there are any tables in the database
            ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table';");

            // If no tables are found, the database is empty
            return !rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error checking if database is empty: " + e.getMessage());
            return false;
        }
    }

    private void createDatabase() {
        String userTableCreation = "CREATE TABLE IF NOT EXISTS users (" +
                "username TEXT PRIMARY KEY, " +
                "password TEXT NOT NULL, " +
                "admin INTEGER NOT NULL" +
                ");";

        String productTableCreation = "CREATE TABLE IF NOT EXISTS product (" +
                "productname TEXT PRIMARY KEY, " +
                "price REAL NOT NULL, " +
                "type TEXT NOT NULL, " +
                "image TEXT, " +
                "stock INTEGER NOT NULL, " +
                "description TEXT" +
                ");";

        String ordersTableCreation = "CREATE TABLE IF NOT EXISTS orders (" +
                "orderID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT NOT NULL, " +
                "orderdate TEXT NOT NULL, " +
                "total REAL NOT NULL, " +
                "FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE ON UPDATE CASCADE" +
                ");";

        String orderItemsTableCreation = "CREATE TABLE IF NOT EXISTS Order_Items (" +
                "orderitemid INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "orderid INTEGER NOT NULL, " +
                "productname TEXT NOT NULL, " +
                "quantity INTEGER NOT NULL, " +
                "totalperproduct REAL NOT NULL, " +
                "category TEXT NOT NULL, " +
                "FOREIGN KEY (orderid) REFERENCES orders(orderID) ON DELETE CASCADE" +
                ");";

        String cartItemsTableCreation = "CREATE TABLE IF NOT EXISTS Cart_Items (" +
                "productname TEXT NOT NULL, " +
                "username TEXT NOT NULL, " +
                "quantity INTEGER NOT NULL, " +
                "PRIMARY KEY (username, productname), " +
                "FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE, " +
                "FOREIGN KEY (productname) REFERENCES product(productname) ON DELETE CASCADE ON UPDATE CASCADE" +
                ");";

        String tagTableCreation = "CREATE TABLE IF NOT EXISTS tag (" +
                "tagname TEXT, " +
                "category TEXT NOT NULL, " +
                "PRIMARY KEY (tagname, category)" +
                ");";

        String productTagTableCreation = "CREATE TABLE IF NOT EXISTS product_tag (" +
                "productname TEXT NOT NULL, " +
                "tagname TEXT NOT NULL, " +
                "category TEXT NOT NULL, "+
                "PRIMARY KEY (productname, tagname)," +
                "FOREIGN KEY (productname) REFERENCES product(productname) ON DELETE CASCADE ON UPDATE CASCADE, " +
                "FOREIGN KEY (tagname, category) REFERENCES tag(tagname, category) ON DELETE CASCADE" +
                ");";

        String insertAdmin = "INSERT OR IGNORE INTO users VALUES('admin', 'admin', 1);";

        //connection to localhost to create database
        try {
            Connection conn = DriverManager.getConnection(url);
            //connection to the created database & create tables
            try (Statement statement = conn.createStatement();){
                statement.execute("PRAGMA foreign_keys = ON;");
                statement.executeUpdate(userTableCreation);
                statement.executeUpdate(productTableCreation);
                statement.executeUpdate(ordersTableCreation);
                statement.executeUpdate(orderItemsTableCreation);
                statement.executeUpdate(cartItemsTableCreation);
                statement.executeUpdate(tagTableCreation);
                statement.executeUpdate(productTagTableCreation);
                statement.executeUpdate(insertAdmin);

            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Error connecting to database: " + e.getMessage());
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error connecting to database: " + e.getMessage());
        }
    }

    public ObservableList<Product> getAllProducts() {
        ObservableList<Product> products = FXCollections.observableArrayList();
        try(PreparedStatement getAllProducts = conn.prepareStatement("SELECT * FROM product");
            ResultSet rs = getAllProducts.executeQuery();) {

            while (rs.next()) {

                products.add(new Product(
                        rs.getString(1),
                        rs.getDouble(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getInt(5),
                        rs.getString(6),
                        getProductTags(rs.getString(1))));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return products;
    }

    public ObservableList<Order> getAllOrders(String user) {
        ObservableList<Order> orders = FXCollections.observableArrayList();
        try (PreparedStatement getAllOrders = conn.prepareStatement("SELECT * FROM orders WHERE username = '" + user + "'");
             ResultSet rs = getAllOrders.executeQuery();) {

            while (rs.next()) {
                orders.add(new Order(rs.getString(1), rs.getString(3), rs.getDouble(4), this.getOrderItems(rs.getString(1))));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public ObservableList<User> getAllUsers() {
        ObservableList<User> users = FXCollections.observableArrayList();
        try (PreparedStatement getAllUsers = conn.prepareStatement("SELECT * FROM users");
             ResultSet rs = getAllUsers.executeQuery();) {

            while (rs.next()) {
                User user = new User(rs.getString(1), rs.getBoolean(3));
                users.add(user);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public Map<String, List<String>> getOrderItems(String orderID) {
        Map<String, List<String>> orderItems = new HashMap<>();
        try (Connection conn = connectDatabase();
        PreparedStatement getOrderItems = conn.prepareStatement("SELECT * FROM order_items WHERE orderid = " + orderID);
        ResultSet rs = getOrderItems.executeQuery();) {

            while (rs.next()) {
                List<String> itemPriceAndQuantity = new ArrayList<>();
                itemPriceAndQuantity.add(rs.getString(4)); //total quantity
                itemPriceAndQuantity.add(rs.getString(5)); //total price
                orderItems.putIfAbsent(rs.getString(3), itemPriceAndQuantity);
            }



        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orderItems;
    }

    public ObservableList<ObservableList<Object>> getSalesPerProductData(String startDate, String endDate) {
        ObservableList<ObservableList<Object>> salesPerProductData = FXCollections.observableArrayList();
        try {
            PreparedStatement getSalesPerProducts = conn.prepareStatement("SELECT " +
                    "p.productName, " +
                    "SUM(oi.quantity) AS order_amount, " +
                    "SUM(oi.totalperproduct) AS total_sales " +
                    "FROM Product p " +
                    "LEFT JOIN Order_Items oi ON p.productName = oi.productName " +
                    "LEFT JOIN Orders o ON oi.orderid = o.orderID " +
                    "WHERE o.orderdate BETWEEN ? and ? " +
                    "GROUP BY p.productName " );

            getSalesPerProducts.setString(1, startDate);
            getSalesPerProducts.setString(2, endDate);

            ResultSet rs = getSalesPerProducts.executeQuery();

            while (rs.next()) {
                ObservableList<Object> salesPerProduct = FXCollections.observableArrayList();
                salesPerProduct.add(rs.getString(1));
                salesPerProduct.add(rs.getString(2));
                salesPerProduct.add(rs.getString(3));
                salesPerProductData.add(salesPerProduct);

            }

            rs.close();
            getSalesPerProducts.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return salesPerProductData;
    }

    public ObservableList<ObservableList<Object>> getSalesPerTimeData(String timeFormat, String startDate, String endDate) {
        ObservableList<ObservableList<Object>> salesPerTimeData = FXCollections.observableArrayList();
        try {
            String groupBy = "";
            if ("Day".equals(timeFormat)) {
                groupBy = "%Y-%m-%d"; // Group by day
            } else if ("Month".equals(timeFormat)) {
                groupBy = "%Y-%m"; // Group by month
            } else if ("Year".equals(timeFormat)) {
                groupBy = "%Y"; // Group by year
            }

            PreparedStatement getSalesPerTimeData = conn.prepareStatement("SELECT " +
                    "strftime(?, o.orderdate) AS period, " +
                    "COUNT(DISTINCT o.orderID) AS total_orders, " +
                    "SUM(CASE WHEN oi.category = 'Merchandise' THEN oi.totalperproduct ELSE 0 END) AS merch_sales, " +
                    "SUM(CASE WHEN oi.category = 'Game' THEN oi.totalperproduct ELSE 0 END) AS game_sales, " +
                    "SUM(CASE WHEN oi.category = 'Peripheral' THEN oi.totalperproduct ELSE 0 END) AS peripheral_sales, " +
                    "SUM(oi.totalperproduct) AS total_earnings " +
                    "FROM orders o " +
                    "JOIN Order_Items oi ON o.orderID = oi.orderid " +
                    "WHERE o.orderdate BETWEEN ? and ? " +
                    "GROUP BY period ");

            getSalesPerTimeData.setString(1, groupBy);
            getSalesPerTimeData.setString(2, startDate);
            getSalesPerTimeData.setString(3, endDate);

            ResultSet rs = getSalesPerTimeData.executeQuery();
            while (rs.next()) {
                ObservableList<Object> row = FXCollections.observableArrayList();
                row.add(rs.getString(1));
                row.add(rs.getInt(2));
                row.add(rs.getDouble(3));
                row.add(rs.getDouble(4));
                row.add(rs.getDouble(5));
                row.add(rs.getDouble(6));
                salesPerTimeData.add(row);
            }

            rs.close();
            getSalesPerTimeData.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return salesPerTimeData;
    }

    public Map<String, Integer> getCartList(String username) {
        Map<String, Integer> cartList = new HashMap<>();
        try {
            PreparedStatement getCartList = conn.prepareStatement("SELECT * FROM Cart_Items WHERE username = ?");
            getCartList.setString(1, username);
            ResultSet rs = getCartList.executeQuery();

            while (rs.next()) {
                cartList.put(rs.getString(1), rs.getInt(3));
            }

            rs.close();
            getCartList.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cartList;
    }

    public ObservableList<String> getTags(String category) {
        ObservableList<String> categoryTags = FXCollections.observableArrayList();
        String query = (category == null) ? "SELECT tagname FROM tag" : "SELECT tagname FROM tag WHERE category = ?";
        try (PreparedStatement getTags = conn.prepareStatement(query);) {
            if (category != null) {
                getTags.setString(1, category);
            }
            ResultSet rs = getTags.executeQuery();
            while (rs.next()) {
                categoryTags.add(rs.getString(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categoryTags;
    }

    public ObservableList<String> getProductTags(String productName) {
        ObservableList<String> productTags = FXCollections.observableArrayList();
        try (PreparedStatement getProductTags = conn.prepareStatement("SELECT tagname FROM product_tag WHERE productname = ?");) {
            getProductTags.setString(1, productName);
            ResultSet rs = getProductTags.executeQuery();
            while (rs.next()) {
                productTags.add(rs.getString(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return  productTags;
    }

    //insert a user into the database
    public void insertUser(String username, String password, boolean admin) {
        try (PreparedStatement insertUser = conn.prepareStatement("INSERT INTO users (username, password, admin) VALUES (?, ?, ?)");){

            insertUser.setString(1, username);
            insertUser.setString(2, password);
            insertUser.setBoolean(3, admin);
            insertUser.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertOrder(String user, String orderDateTime, double total, ObservableMap<Product, Integer> cartItems) {
        try(PreparedStatement addOrder = conn.prepareStatement("INSERT INTO orders (username, orderdate, total) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            PreparedStatement addOrderItem = conn.prepareStatement("INSERT INTO order_items (orderid, productname, quantity, totalperproduct, category) VALUES (?, ?, ?, ?, ?)");) {
            addOrder.setString(1, user);
            addOrder.setString(2, orderDateTime);
            addOrder.setDouble(3, total);
            addOrder.executeUpdate();

            //get orderID
            ResultSet generatedKeys = addOrder.getGeneratedKeys();
            generatedKeys.next();
            int orderId = generatedKeys.getInt(1);

            for (Product product : cartItems.keySet()) {
                addOrderItem.setInt(1, orderId);
                addOrderItem.setString(2, product.getProductName());
                addOrderItem.setInt(3, cartItems.get(product));
                addOrderItem.setDouble(4, product.getProductPrice() * cartItems.get(product));
                addOrderItem.setString(5, product.getCategory());
                addOrderItem.addBatch();
            }
            addOrderItem.executeBatch();


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertProduct(String productName, double productPrice, String productCategory, String productImage, int productStock, String productDescription) {
        try(PreparedStatement insertProduct = conn.prepareStatement("INSERT INTO product (productname, price, type, image, stock, description) VALUES (?, ?, ?, ?, ?, ?)");) {

            insertProduct.setString(1, productName);
            insertProduct.setDouble(2, productPrice);
            insertProduct.setString(3, productCategory);
            insertProduct.setString(4, productImage);
            insertProduct.setInt(5, productStock);
            insertProduct.setString(6, productDescription);
            insertProduct.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertAndUpdateCartProduct(String username, String productName, int quantity) {
        try(PreparedStatement insertCartProduct = conn.prepareStatement(
                "INSERT INTO Cart_Items (productname, username, quantity)VALUES (?, ?, ?) ON CONFLICT(username, productname) DO UPDATE SET quantity = ?");) {

            insertCartProduct.setString (1, productName);
            insertCartProduct.setString(2, username);
            insertCartProduct.setInt(3, quantity);
            insertCartProduct.setInt(4, quantity);
            insertCartProduct.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void insertTag(String tagName, String category) {
        try (PreparedStatement addTag = conn.prepareStatement("INSERT INTO tag VALUES (?, ?)")) {
            addTag.setString(1, tagName);
            addTag.setString(2, category);
            addTag.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertAndUpdateProductTag(String productName, String category, ObservableList<String> appliedTags) {
        try (PreparedStatement clearProductTag = conn.prepareStatement("DELETE FROM product_tag WHERE productname = ?");
                PreparedStatement insertProductTag = conn.prepareStatement("INSERT OR REPLACE INTO product_tag VALUES (?, ?, ?)")) {

            clearProductTag.setString(1, productName);
            clearProductTag.executeUpdate();

            for (String tag : appliedTags) {
                insertProductTag.setString(1, productName);
                insertProductTag.setString(2, tag);
                insertProductTag.setString(3, category);
                insertProductTag.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateProduct(String originalProductName, String newProductName, double newProductPrice, String newProductCategory, String newProductImage, int productStock, String newProductDescription) {
        try(PreparedStatement updateProduct = conn.prepareStatement("UPDATE product SET productname = ?, price = ?, type = ?, image = ?, stock = ?, description = ? WHERE productname = ?");) {

            updateProduct.setString(1, newProductName);
            updateProduct.setDouble(2, newProductPrice);
            updateProduct.setString(3, newProductCategory);
            updateProduct.setString(4, newProductImage);
            updateProduct.setInt(5, productStock);
            updateProduct.setString(6, newProductDescription);
            updateProduct.setString(7, originalProductName);
            updateProduct.executeUpdate();

        } catch (SQLException e) {}
    }

    public void updateProductStock(String productName, int productStock) {
        try (PreparedStatement updateProductStock = conn.prepareStatement("UPDATE product SET stock = ? WHERE productname = ?");) {

            updateProductStock.setInt(1, productStock);
            updateProductStock.setString(2, productName);
            updateProductStock.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateUserRole(String username, boolean admin) {
        try(PreparedStatement updateUserRole = conn.prepareStatement("UPDATE users SET admin = ? WHERE username = ?");) {
            updateUserRole.setBoolean(1, admin);
            updateUserRole.setString(2, username);
            updateUserRole.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteProduct(String productName) {
        try(PreparedStatement deleteProduct = conn.prepareStatement("DELETE FROM product WHERE productname = ?");) {
            deleteProduct.setString(1, productName);
            deleteProduct.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteCartProduct(String username, String productName) {
        try(PreparedStatement deleteProduct = conn.prepareStatement("DELETE FROM Cart_Items WHERE productname = ? AND username = ?");) {
            deleteProduct.setString(1, productName);
            deleteProduct.setString(2, username);
            deleteProduct.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteAllUserCart (String username) {
        try(PreparedStatement deleteProduct = conn.prepareStatement("DELETE FROM Cart_Items WHERE username = ?");) {
            deleteProduct.setString(1, username);
            deleteProduct.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteTag(String tagName, String category) {
        try(PreparedStatement deleteTag = conn.prepareStatement("DELETE FROM tag WHERE tagName = ? AND category = ?");) {
            deleteTag.setString(1, tagName);
            deleteTag.setString(2, category);
            deleteTag.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //return true of the checkElement is repeated in a table
    public boolean checkRepeated(String checkElement, String table, String column) {
        try {
            boolean repeated = false;
            PreparedStatement checkExist = conn.prepareStatement("SELECT * FROM " + table + " WHERE " + column + " = ?");
            checkExist.setString(1, checkElement);
            ResultSet rs = checkExist.executeQuery();
            repeated = rs.isBeforeFirst();

            rs.close();
            checkExist.close();

            return repeated;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //compare data of a user record with a desired value
    public boolean compareUserTable(String comparingElement, String comparedElement, String column) {
        try {
            PreparedStatement comparePass = conn.prepareStatement("SELECT " + column + " FROM users WHERE username = ?");
            comparePass.setString(1, comparingElement);
            ResultSet rs = comparePass.executeQuery();
            boolean isEqual = false;

            if (rs.next()) {
                if (rs.getString(column).equals(comparedElement)) {
                    isEqual = true;
                }
            }

            rs.close();
            comparePass.close();
            return isEqual;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
