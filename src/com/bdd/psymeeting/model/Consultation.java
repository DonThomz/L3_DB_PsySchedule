/*
 * Copyright (c) 2020. Thomas GUILLAUME & Gabriel DUGNY
 */

package com.bdd.psymeeting.model;

import com.bdd.psymeeting.Main;
import com.jfoenix.controls.JFXButton;
import javafx.scene.control.TextArea;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class Consultation {

    // --------------------
    //   Attributes
    // --------------------

    // Consultation attributes from table CONSULTATION
    private final int consultationID; //primary key
    private Calendar date;
    private float price;
    private String payMode;
    private HashMap<Integer, String[]> patients;

    // Feedback
    private Feedback feedback;


    // Graphic attributes
    private TextArea full_infos;
    private JFXButton consultation_button;

    // --------------------
    //   Constructors
    // --------------------
    public Consultation(int consultationID) {

        this.consultationID = consultationID;

        // get feedback
        this.feedback = new Feedback(this.consultationID);

        try (Connection connection = Main.database.getConnection()) {
            String query;
            PreparedStatement preparedStatement;
            ResultSet resultSet;

            // get consultation info (date, price, pay mode)
            query = "select CONSULTATION_DATE, PRICE, PAY_MODE from CONSULTATION where CONSULTATION_ID = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, this.consultationID);
            resultSet = preparedStatement.executeQuery();
            resultSet.next();

            // assign attributes
            this.date = Main.Timestamp2Calendar(resultSet.getTimestamp(1));
            this.price = resultSet.getFloat(2);
            this.payMode = resultSet.getString(3);

            // get patients list from consultations
            query = "select P.PATIENT_ID, P.NAME, P.LAST_NAME " +
                    "from PATIENT P " +
                    "join CONSULTATION_CARRYOUT CC on P.PATIENT_ID = CC.PATIENT_ID " +
                    "where CC.CONSULTATION_ID = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, this.consultationID);
            resultSet = preparedStatement.executeQuery();

            patients = new HashMap<>();
            while (resultSet.next()) {
                String[] fullName = {resultSet.getString(2), resultSet.getString(3)};
                patients.put(resultSet.getInt(1), fullName);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }


    // --------------------
    //   Get methods
    // --------------------
    public int getConsultationID() {
        return consultationID;
    }

    public Calendar getDate() {
        return date;
    }

    public JFXButton getConsultation_button() {
        return consultation_button;
    }

    public float getPrice() {
        return price;
    }

    public String getPayMode() {
        return payMode;
    }

    public TextArea getFull_infos() {
        return full_infos;
    }

    public Feedback getFeedback() {
        return feedback;
    }

    public HashMap<Integer, String[]> getPatients() {
        return patients;
    }

    // --------------------
    //   Set methods
    // --------------------

    public void setConsultation_button(JFXButton consultation_button) {
        this.consultation_button = consultation_button;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public void setPayMode(String payMode) {
        this.payMode = payMode;
    }

    public void setFull_infos(TextArea full_infos) {
        this.full_infos = full_infos;
    }

    public void setFeedback(Feedback feedback) {
        this.feedback = feedback;
    }

    public void setPatients(HashMap<Integer, String[]> patients) {
        this.patients = patients;
    }

    // --------------------
    //   Statement methods
    // --------------------

    public static int getLastPrimaryKeyId() {
        try (Connection connection = Main.database.getConnection()) {
            Statement stmt = connection.createStatement();
            ResultSet result = stmt.executeQuery("select max(CONSULTATION_ID) from CONSULTATION");
            result.next();
            return result.getInt(1);
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return -1;
    }

    public static int getConsultationSize() {
        try (Connection connection = Main.database.getConnection()) {
            Statement stmt = connection.createStatement();
            ResultSet result = stmt.executeQuery("select count(CONSULTATION_ID) from CONSULTATION");
            result.next();
            return result.getInt(1);
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return -1;
    }

    public static Calendar getDateById(int consultation_id) {
        // TODO Fix this, as the connection can't be checked out
        try (Connection connection = Main.database.getConnection()) {
            String query = "select\n" +
                    "     c.CONSULTATION_DATE\n" +
                    "from CONSULTATION c\n" +
                    "where c.CONSULTATION_ID = ?";
            PreparedStatement preparedStmt = connection.prepareStatement(query);
            preparedStmt.setInt(1, consultation_id);
            ResultSet result = preparedStmt.executeQuery();
            if (result.next()) {
                return Main.Timestamp2Calendar(result.getTimestamp(1));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * PreparedStatement to get all time slots free
     *
     * @param dateField date selected
     * @return HashMap with all time slots free ( key = TimeStamp, value = true / free or false / block )
     */
    public static Map<Timestamp, Boolean> getTimeSlots(LocalDate dateField) {

        try (Connection connection = Main.database.getConnection()) {

            Map<Timestamp, Boolean> timeSlots = initTimeSlotsByDateSelected(dateField);
            ArrayList<Timestamp> timeSlotsBlocked = new ArrayList<>();

            String[] dates = Main.getDatesOfDay(dateField);
            System.out.println(dates[0] + " " + dates[1]);

            String query = "select CONSULTATION_DATE\n" +
                    "from CONSULTATION\n" +
                    "where CONSULTATION_DATE between TO_DATE('" + dates[0] + "', 'yyyy-mm-dd HH24:mi:ss') "
                    + "and TO_DATE('" + dates[1] + "', 'yyyy-mm-dd HH24:mi:ss')";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet result = preparedStatement.executeQuery();

            // Check all time slots
            while (result.next()) {
                timeSlotsBlocked.add(result.getTimestamp(1));
            }

            // 20 appointment by day
            for (Timestamp tmp : timeSlotsBlocked
            ) {
                timeSlots.put(tmp, true);
            }

            return timeSlots;
            /*timeSlots.forEach((k, v) -> {
                Timestamp tmp = new Timestamp(System.currentTimeMillis());
                if (!v && k.compareTo(tmp) >= 0) {
                    String hours_date = new SimpleDateFormat("HH:mm").format(k.getTime());
                    //System.out.println(hours_date);
                    hour_field.getItems().add(hours_date);
                }
            });*/
        } catch (SQLException ex) {
            System.out.println("Error loading date");
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * get all time slots during a day
     */
    private static Map<Timestamp, Boolean> initTimeSlotsByDateSelected(LocalDate date) {
        Map<Timestamp, Boolean> timeSlots = new HashMap<>();
        String selectDate = new SimpleDateFormat("yyyy-MM-dd").format(Timestamp.valueOf(date.atTime(LocalTime.MIDNIGHT)));
        timeSlots.put(java.sql.Timestamp.valueOf(selectDate + " 08:00:00"), false);
        timeSlots.put(java.sql.Timestamp.valueOf(selectDate + " 08:30:00"), false);
        timeSlots.put(java.sql.Timestamp.valueOf(selectDate + " 09:00:00"), false);
        timeSlots.put(java.sql.Timestamp.valueOf(selectDate + " 09:30:00"), false);
        timeSlots.put(java.sql.Timestamp.valueOf(selectDate + " 10:00:00"), false);
        timeSlots.put(java.sql.Timestamp.valueOf(selectDate + " 10:30:00"), false);
        timeSlots.put(java.sql.Timestamp.valueOf(selectDate + " 11:00:00"), false);
        timeSlots.put(java.sql.Timestamp.valueOf(selectDate + " 11:30:00"), false);
        timeSlots.put(java.sql.Timestamp.valueOf(selectDate + " 14:00:00"), false);
        timeSlots.put(java.sql.Timestamp.valueOf(selectDate + " 14:30:00"), false);
        timeSlots.put(java.sql.Timestamp.valueOf(selectDate + " 15:00:00"), false);
        timeSlots.put(java.sql.Timestamp.valueOf(selectDate + " 15:30:00"), false);
        timeSlots.put(java.sql.Timestamp.valueOf(selectDate + " 16:00:00"), false);
        timeSlots.put(java.sql.Timestamp.valueOf(selectDate + " 16:30:00"), false);
        timeSlots.put(java.sql.Timestamp.valueOf(selectDate + " 17:00:00"), false);
        timeSlots.put(java.sql.Timestamp.valueOf(selectDate + " 17:30:00"), false);
        timeSlots.put(java.sql.Timestamp.valueOf(selectDate + " 18:00:00"), false);
        timeSlots.put(java.sql.Timestamp.valueOf(selectDate + " 18:30:00"), false);
        timeSlots.put(java.sql.Timestamp.valueOf(selectDate + " 19:00:00"), false);
        timeSlots.put(java.sql.Timestamp.valueOf(selectDate + " 19:30:00"), false);

        // order HashMap
        timeSlots = new TreeMap<>(timeSlots);
        return timeSlots;
    }

    @Override
    public String toString() {
        return "Consultation{" +
                "consultationID=" + consultationID +
                ", date=" + date +
                ", price=" + price +
                ", payMode='" + payMode + '\'' +
                ", patients=" + patients +
                ", feedback=" + feedback +
                ", full_infos=" + full_infos +
                ", consultation_button=" + consultation_button +
                '}';
    }
}
