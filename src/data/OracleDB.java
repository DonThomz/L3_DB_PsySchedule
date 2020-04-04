package data;

import java.sql.*;


/**
 *  Database class
 */
public class OracleDB {

    private Connection connection;

    // Constructor

    public OracleDB(){}

    // Connexion methods

    public void connectionDatabase(String username, String password){

        // load driver
        try{
            Class.forName("oracle.jdbc.driver.OracleDriver");
        }catch (ClassNotFoundException ex){
            System.out.println(" *** ERROR LOADING DRIVER *** ");
            System.out.println("Message: " + ex.getMessage());
            System.out.println("Error code: " + ex.getCause());
        }

        // connection to oracle database
        try {
            this.connection = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", username, password);
            System.out.println(" *** Connection successfully established ***");
            System.out.println("Welcome " + username);

        }catch(SQLException ex){
            // if docker oracle database port : 51521
            try{
                this.connection = DriverManager.getConnection("jdbc:oracle:thin:@localhost:51521:xe", username, password);
                System.out.println(" *** Connection successfully established ***");
                System.out.println("Welcome " + username);
            } catch (SQLException e) {
                System.out.println(" *** ERROR CONNECTION TO DATABASE ***");
                while (ex != null) {
                    System.out.println ("SQL Etat: " + ex.getSQLState ());
                    System.out.println ("Message: " + ex.getMessage ());
                    System.out.println ("Error code: " +
                            ex.getErrorCode ());
                    ex = ex.getNextException ();
                }
                while (e != null) {
                    System.out.println ("SQL Etat: " + e.getSQLState ());
                    System.out.println ("Message: " + e.getMessage ());
                    System.out.println ("Error code: " +
                            e.getErrorCode ());
                    e = e.getNextException ();
                }
            }

        }
    }

    public void closeDatabase(){
        if(connection != null) {
            try {
                connection.close();
                System.out.println(" *** Connection close ***");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}