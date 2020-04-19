package com.bdd.psymeeting;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * Database class
 */
public class OracleDB {
//    private Connection connection;

    //private static final OracleDB instance = new OracleDB();
    private ComboPooledDataSource comboPooledDataSource;

    public OracleDB() {

    }

//    public static final OracleDB getInstance() {
//        return instance;
//    }

    public Connection getConnection() throws SQLException {
        return this.comboPooledDataSource.getConnection();
    }

    public void close() {
        this.comboPooledDataSource.close();
    }

    /**
     * Connect to the DB.
     */
    public boolean connectionDatabase(String username, String password) throws Exception {
        System.out.println("Connecting to DB...");
        try {
            this.comboPooledDataSource = new ComboPooledDataSource();
            this.comboPooledDataSource.setDriverClass("oracle.jdbc.driver.OracleDriver");
            this.comboPooledDataSource.setJdbcUrl("jdbc:oracle:thin:@localhost:1521:xe");
            this.comboPooledDataSource.setUser(username);
            this.comboPooledDataSource.setPassword(password);

            this.comboPooledDataSource.setMinPoolSize(5);
            this.comboPooledDataSource.setAcquireIncrement(5);
            this.comboPooledDataSource.setMaxPoolSize(20);

            this.comboPooledDataSource.setAcquireRetryAttempts(0);

            this.comboPooledDataSource.setUnreturnedConnectionTimeout(10);
            this.comboPooledDataSource.setDebugUnreturnedConnectionStackTraces(true);

            this.comboPooledDataSource.setTestConnectionOnCheckout(true);

            System.out.println("Pooled data source set up: done!");

            try(Connection connection = this.getConnection()) {
                System.out.println("Checking connection....");
                Statement stmt = connection.createStatement();
                stmt.executeQuery("select NAME, LAST_NAME from ADMINISTRATOR");

                System.out.println("Connection succeeded!");
                return true;
            }
            catch (Exception e) {
                System.out.println("Connection failed! Maybe wrong password?");
                this.comboPooledDataSource.close();
                e.printStackTrace();
                return false;
            }

        } catch (PropertyVetoException e) {
            System.out.println("Unable to set up connection pool!");
            e.printStackTrace();
            // System.exit(1);
            return false;
        }
        //return true;
        // load driver
//        try {
//            Class.forName("oracle.jdbc.driver.OracleDriver");
//        } catch (ClassNotFoundException ex) {
//            System.out.println(" *** ERROR LOADING DRIVER *** ");
//            System.out.println("Message: " + ex.getMessage());
//            System.out.println("Error code: " + ex.getCause());
//        }
//
//        // connection to oracle database
//        try {
//            this.connection = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", username, password);
//            this.connection.setAutoCommit(false);
//            System.out.println(" *** Connection successfully established ***");
//            System.out.println("Welcome " + username);
//            return true;
//        } catch (SQLException ex) {
//            // if docker oracle database port : 51521
//            try {
//                this.connection = DriverManager.getConnection("jdbc:oracle:thin:@localhost:51521:xe", username, password);
//                System.out.println(" *** Connection successfully established ***");
//                System.out.println("Welcome " + username);
//                return true;
//
//            } catch (SQLException e) {
//                System.out.println(" *** ERROR CONNECTION TO DATABASE ***");
//                while (ex != null) {
//                    System.out.println("SQL state: " + ex.getSQLState());
//                    System.out.println("Message: " + ex.getMessage());
//                    System.out.println("Error code: " + ex.getErrorCode());
//                    ex = ex.getNextException();
//                }
//                while (e != null) {
//                    System.out.println("SQL state: " + e.getSQLState());
//                    System.out.println("Message: " + e.getMessage());
//                    System.out.println("Error code: " + e.getErrorCode());
//                    e = e.getNextException();
//                }
//                //return false;
//                throw new Exception("Test");
//            }
//        }
    }

    /**
     * Close the connection to the DB properly.
     */
    public void closeDatabase() {
//        if (connection != null) {
//            try {
//                connection.close();
//                System.out.println(" *** Connection close ***");
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }
        this.comboPooledDataSource.close();
    }


//    public Connection getConnection() {
//        return connection;
//    }
}