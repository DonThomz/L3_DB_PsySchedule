package com.bdd.pj.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


/**
 * Database class
 */
public class OracleDB {

    private Connection connection;

    public OracleDB() {
    }

    /**
     * Connect to the DB.
     */
    public boolean connectionDatabase(String username, String password) throws Exception {
        System.out.println("Connecting to DB...");
        // load driver
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException ex) {
            System.out.println(" *** ERROR LOADING DRIVER *** ");
            System.out.println("Message: " + ex.getMessage());
            System.out.println("Error code: " + ex.getCause());
        }

        // connection to oracle database
        try {
            this.connection = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", username, password);
            this.connection.setAutoCommit(false);
            System.out.println(" *** Connection successfully established ***");
            System.out.println("Welcome " + username);
            return true;
        } catch (SQLException ex) {
            // if docker oracle database port : 51521
            try {
                this.connection = DriverManager.getConnection("jdbc:oracle:thin:@localhost:51521:xe", username, password);
                System.out.println(" *** Connection successfully established ***");
                System.out.println("Welcome " + username);
                return true;

            } catch (SQLException e) {
                System.out.println(" *** ERROR CONNECTION TO DATABASE ***");
                while (ex != null) {
                    System.out.println("SQL state: " + ex.getSQLState());
                    System.out.println("Message: " + ex.getMessage());
                    System.out.println("Error code: " + ex.getErrorCode());
                    ex = ex.getNextException();
                }
                while (e != null) {
                    System.out.println("SQL state: " + e.getSQLState());
                    System.out.println("Message: " + e.getMessage());
                    System.out.println("Error code: " + e.getErrorCode());
                    e = e.getNextException();
                }
                //return false;
                throw new Exception("Test");
            }
        }
    }

    /**
     * Close the connection to the DB properly.
     */
    public void closeDatabase() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println(" *** Connection close ***");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public Connection getConnection() {
        return connection;
    }
}
