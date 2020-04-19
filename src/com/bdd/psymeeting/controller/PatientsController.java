/*
 * Copyright (c) 2020. Thomas GUILLAUME & Gabriel DUGNY
 */

package com.bdd.psymeeting.controller;

import com.bdd.psymeeting.model.Patient;
import com.jfoenix.controls.JFXButton;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

public class PatientsController implements Initializable {

    // JavaFX
    public VBox patient_list_box;
    public HashMap<JFXButton, Boolean> profilesButtonsHashMap;
    public AnchorPane profilePane;

    // Attributes
    public static ArrayList<Patient> list_patients;
    public static int current_patient_id;


    // --------------------
    //  Services
    // --------------------
    final Service<ArrayList<Patient>> loadPatients = new Service<ArrayList<Patient>>() {
        @Override
        protected Task<ArrayList<Patient>> createTask() {
            return new Task<ArrayList<Patient>>() {
                @Override
                protected ArrayList<Patient> call() throws Exception {
                    return Patient.getAllPatientsProfiles(); // init patients
                }
            };
        }
    };

    // --------------------
    //   Initialize method
    // --------------------
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        profilesButtonsHashMap = new HashMap<>();
        patient_list_box.setSpacing(10);

        // execute sql request in another thread
        if (loadPatients.getState() == Task.State.READY)
            loadPatients.start();

        loadPatients.setOnSucceeded(event -> {
            System.out.println("Task succeeded -> loading patients informations\nCreation patients box...");
            setupListPatients(loadPatients.getValue());
        });

        loadPatients.setOnFailed(event -> System.out.println("Task failed -> error loading patients informations"));

        loadPatients.setOnRunning(event -> System.out.println("Loading..."));
    }

    public void setupListPatients(ArrayList<Patient> patientArrayList) {
        int ID = 0;
        list_patients = patientArrayList;
        for (Patient p : list_patients
        ) {
            if (p != null) {
                JFXButton patient_button = new JFXButton();
                patient_button.getStyleClass().add("patient_cell");
                patient_button.getStyleClass().add("patient_cell_list");

                patient_button.setText(p.getName() + " " + p.getLast_name());

                profilesButtonsHashMap.put(patient_button, false);

                int finalID = ID;
                patient_button.setOnAction(event -> {
                    loadPatientInfo(finalID);
                    updateButtonStyle(patient_button);
                });
                ID++;
                patient_list_box.getChildren().add(patient_button);
            }
        }

    }

    private void loadPatientInfo(int ID) {
        try {
            System.out.println("load patient " + ID);
            profilePane.getChildren().clear();
            current_patient_id = ID;
            profilePane.getChildren().add(FXMLLoader.load(getClass().getResource("../fxml/profile_patient.fxml")));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void updateButtonStyle(JFXButton b) {
        profilesButtonsHashMap.forEach((k, v) -> {
            if (k.getText().equals(b.getText())) {
                profilesButtonsHashMap.put(k, true);
                k.setStyle("-fx-background-color: #546e7a");
            } else {
                profilesButtonsHashMap.put(k, false);
                k.setStyle("-fx-background-color: #fafafa");
            }
        });
    }


}
