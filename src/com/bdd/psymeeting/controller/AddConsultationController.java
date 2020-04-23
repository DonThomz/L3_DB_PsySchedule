/*
 * Copyright (c) 2020. Thomas GUILLAUME & Gabriel DUGNY
 */

package com.bdd.psymeeting.controller;

import com.bdd.psymeeting.Main;
import com.bdd.psymeeting.TransitionEffect;
import com.bdd.psymeeting.model.Consultation;
import com.bdd.psymeeting.model.Patient;
import com.bdd.psymeeting.model.User;
import com.jfoenix.controls.*;
import com.jfoenix.validation.RequiredFieldValidator;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;


public class AddConsultationController implements Initializable {

    // --------------------
    //   Attributes
    // --------------------

    // Add patients
    public Label label_patients;
    public JFXButton add_patient_button;

    // Fields
    public JFXDatePicker date_field;
    public JFXComboBox<String> hour_field;
    public JFXTextField name_field;
    public JFXTextField last_name_field;
    public JFXTextField email_field;
    public JFXCheckBox anxiety_checkbox;
    public JFXButton submit_button;
    private RequiredFieldValidator validator_field;

    // Box
    public StackPane stackPane;
    public VBox form_box;
    public VBox patients_save;

    // Warring
    public Label warring;
    private boolean warringCheck;

    private ArrayList<Patient> patients;
    private ArrayList<User> users;
    private boolean confirmation;


    // --------------------
    //   Services
    // --------------------
    Service<Map<Timestamp, Boolean>> loadTimeSlots = new Service<Map<Timestamp, Boolean>>() {
        @Override
        protected Task<Map<Timestamp, Boolean>> createTask() {
            return new Task<Map<Timestamp, Boolean>>() {
                @Override
                protected Map<Timestamp, Boolean> call() {
                    return Consultation.getTimeSlots(date_field.getValue());
                }
            };
        }
    };

    Service<Boolean> updateNewConsultation = new Service<Boolean>() {
        @Override
        protected Task<Boolean> createTask() {
            return new Task<Boolean>() {
                @Override
                protected Boolean call() {
                    return updateNewConsultation();
                }
            };
        }
    };

    Service<Boolean> addPatient = new Service<Boolean>() {
        @Override
        protected Task<Boolean> createTask() {
            return new Task<Boolean>() {
                @Override
                protected Boolean call() {
                    return addPatientTask();
                }
            };
        }
    };

    // --------------------
    //   Initialize method
    // --------------------
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // add transition effects to the form
        TransitionEffect.TranslateTransitionY(form_box, 600, 75);
        TransitionEffect.FadeTransition(form_box, 600, 0.2f, 5);

        // init ArrayList
        patients = new ArrayList<>();
        users = new ArrayList<>();

        // init patient label
        label_patients.setText("Patient - " + (patients.size() + 1));

        // validation settings
        validator_field = new RequiredFieldValidator();
        validator_field.setMessage("Le champs est obligatoire");

        // add validation for all fields
        addListenerValidationField(name_field);
        addListenerValidationField(last_name_field);
        addListenerValidationField(email_field);
        addListenerValidationField(date_field);
        addListenerValidationField(hour_field);

        // Init service
        loadTimeSlots.setOnSucceeded(event -> {
            System.out.println("Task loading time slots succeeded !");
            setupTimeSlotsField(loadTimeSlots.getValue());
            loadTimeSlots.reset();
        });
        loadTimeSlots.setOnFailed(event -> {
            System.out.println("Task failed ! ");
            loadTimeSlots.reset();
        });

        updateNewConsultation.setOnSucceeded(event -> {
            System.out.println("Task update new consultation succeeded !");
            // reload scene
            Main.sceneMapping("add_consultation_scene", "add_consultation_scene");
            updateNewConsultation.reset();
        });
        updateNewConsultation.setOnFailed(event -> {
            System.out.println("Task new consultation failed !");
            updateNewConsultation.reset();
        });

        addPatient.setOnSucceeded(event -> {
            System.out.println("Task adding patient succeeded ! ");
            addPatientSucceeded();
            addPatient.reset();
        });

        addPatient.setOnFailed(event -> {
            System.out.println("Task adding patient failed !");
            addPatient.reset();
        });


        // Init listener
        date_field.valueProperty().addListener(event -> {
            // reset timeSlotsField
            hour_field.getItems().clear();
            System.out.println(Timestamp.valueOf(date_field.getValue().toString() + " 00:00:00.0"));
        });


    }


    // --------------------
    //  Submit consultation
    // --------------------
    public void submit() { // button "Enregistrer"
        // disable during the thread
        add_patient_button.setDisable(true);

        // check if minimum 1 patient exist in tmp_patients or if fields are not empty
        if (date_field.getValue() != null && hour_field.getValue() != null && (!patients.isEmpty() || validField())) {
            if (validField()) {
                if (addPatient.getState() == Task.State.READY) // adding patient
                    addPatient.start();
            }
            // open confirmation dialog
            confirmationDialog();

        } else { // focus on empty field
            validateTextFieldAction();
            validateDateFieldAction();
        }

    }

    public void confirmationDialog() {

        JFXDialogLayout content = new JFXDialogLayout();
        confirmation = false;
        content.setHeading(new Text("La consultation a été enregistrée !"));
        // show information
        StringBuilder info = new StringBuilder();
        if (patients.size() != 0) {
            for (Patient p : patients
            ) {
                info.append("\n- ").append(p.getName()).append(" ").append(p.getLast_name());
            }
            if (validField())
                info.append("\n- ").append(name_field.getText()).append(" ").append(last_name_field.getText());
        } else
            info.append("\n- ").append(name_field.getText()).append(" ").append(last_name_field.getText());

        content.setBody(new Text("Date de consultation : " + date_field.getValue()
                + " à " + hour_field.getValue() + "\n"
                + "Les patients sont : \n"
                + info));

        JFXDialog dialog = new JFXDialog(stackPane, content, JFXDialog.DialogTransition.CENTER);
        JFXButton submit = new JFXButton("Confirmer & Envoyer");
        JFXButton cancel = new JFXButton("Annuler");

        submit.setOnAction(event -> {
            confirmation = true;
            if (updateNewConsultation.getState() == Task.State.READY) // loading update table in Service
                updateNewConsultation.start();
            dialog.close();
        });

        cancel.setOnAction(event -> {
            confirmation = false;
            dialog.close();
        });
        content.setActions(cancel, submit);

        dialog.show();
    }

    private boolean updateNewConsultation() {
        try (Connection connection = Main.database.getConnection()) {
            if (confirmation) {

                Timestamp date = Timestamp.valueOf(date_field.getValue().toString() + " " + hour_field.getValue() + ":00.0");
                // enable commit command
                connection.setAutoCommit(false);
                int consultationID = Consultation.getLastPrimaryKeyId();
                int lastPatientID = Patient.getLastPrimaryKeyId();
                if (consultationID != -1) {
                    if (Patient.insertIntoPatientTable(patients, lastPatientID)
                            && User.insertIntoUserTable(users)
                            && Consultation.insertIntoConsultationTable(date, consultationID)
                            && Consultation.insertIntoConsultationCarryOutTable(patients, consultationID, lastPatientID)) {
                        connection.commit();
                        return true;
                    } else return false;
                } else return false;

            } else return false;
        } catch (SQLException ex) {
            System.out.println("Error creation consultation");
            ex.printStackTrace();
            return false;
        }
    }

    //-----------------
    // Adding Patient
    //-----------------
    public void add() { // button "Ajouter patient"
        if (addPatient.getState() == Task.State.READY) {
            addPatient.start();
        }
    }

    public boolean addPatientTask() {
        // field not empty
        if (validField()) {
            return addPatient2ArrayList(); // create and add patient to ArrayList patients
        } else validateTextFieldAction();
        return false;
    }

    public void addPatientSucceeded() {
        // reset field
        resetField();
        // add patient to the side right bar
        attachPatients();
        // update patient label
        updatePatientLabel();
    }


    // --------------------
    //  Private methods
    // --------------------
    private boolean addPatient2ArrayList() {
        if (userExist()) { // check if user exist ==> email exit in database
            // check if name and last name is correct
            Patient tmp_patient = Patient.getPatientByEmail(email_field.getText());
            assert tmp_patient != null;
            if (tmp_patient.getName().equals(name_field.getText()) && tmp_patient.getLast_name().equals(last_name_field.getText())) {
                patients.add(tmp_patient);
                return true;
            } else {
                // error user email already exist in database
                // ex : ubabst1@zdnet.com
                warring = new Label("L'email renseigné est déjà pris");
                warring.getStyleClass().add("warring_label");
                form_box.getChildren().add(2, warring);
                warringCheck = true;
                return false;
            }
        } else { // create a patient and a user
            int lastPatientId = Patient.getLastPrimaryKeyId();
            if (lastPatientId != -1) {
                // @TODO Fix users update
                Patient tmp_p = new Patient(lastPatientId + 1, name_field.getText(), last_name_field.getText(), true);
                patients.add(tmp_p); // add new patient

                User tmp_user = new User(User.getLastUserId() + 1, email_field.getText(), tmp_p.getPatient_id(), true);
                users.add(tmp_user); // add new user
                return true;
            } else return false;
        }
    }

    private boolean userExist() { // check if user exist in database with email_field
        return User.userExist(email_field.getText());
    }

    private void attachPatients() { // attach patient to the right side
        JFXButton patient_save = new JFXButton();
        patient_save.getStyleClass().add("patient_cell");


        String text = "Patient"
                + "\n " + patients.get(patients.size() - 1).getName()
                + " " + patients.get(patients.size() - 1).getLast_name();

        patient_save.setText(text);
        Patient selectPatient = patients.get(patients.size() - 1);
        patient_save.setOnAction(event -> confirmationDialogPatient(patient_save, selectPatient));

        patients_save.getChildren().add(patient_save);
        patients_save.setSpacing(20);


    }

    private void confirmationDialogPatient(JFXButton patientSlot, Patient patient) {
        // add dialog layout to remove patient
        JFXDialogLayout confirmation_remove = new JFXDialogLayout();
        confirmation_remove.setHeading(new Text("Voulez-vous supprimer le patient ?"));

        JFXDialog dialog_confirmation = new JFXDialog(stackPane, confirmation_remove, JFXDialog.DialogTransition.CENTER);
        JFXButton remove = new JFXButton("Supprimer");
        JFXButton cancel = new JFXButton("Annuler");

        remove.setOnAction(event -> {
            // remove patient
            patients_save.getChildren().remove(patientSlot);
            patients.remove(patient);
            updatePatientLabel();
            dialog_confirmation.close();
        });
        cancel.setOnAction(event -> {
            confirmation = false;
            dialog_confirmation.close();
        });

        confirmation_remove.setActions(remove, cancel);
        dialog_confirmation.show();


    }


    // --------------------
    //  Field methods
    // --------------------
    private boolean validField() { // check if fields are not empty
        return !name_field.getText().isEmpty()
                && !last_name_field.getText().isEmpty()
                && !email_field.getText().isEmpty();
    }

    private void resetField() {
        // reset field
        name_field.setText("");
        last_name_field.setText("");
        email_field.setText("");
        anxiety_checkbox.setSelected(false);
    }

    private void validateTextFieldAction() { // add require validation if fields are empty
        if (name_field.getText().isEmpty())
            name_field.validate();
        if (last_name_field.getText().isEmpty())
            last_name_field.validate();
        if (email_field.getText().isEmpty())
            email_field.validate();
    }

    private void validateDateFieldAction() { // add require validation if date and hour fields are empty
        if (date_field.getValue() == null)
            date_field.validate();
        if (hour_field.getValue() == null)
            hour_field.validate();
    }

    private void addListenerValidationField(JFXTextField field) {
        field.getValidators().add(validator_field);
        field.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                if (warringCheck) { // remove warring if warring check
                    form_box.getChildren().remove(2);
                    warringCheck = false;
                }
                field.resetValidation();
            }
        });
    }

    private void addListenerValidationField(JFXDatePicker field) {
        field.getValidators().add(validator_field);
        field.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                field.resetValidation();
            }
        });

    }

    private void addListenerValidationField(JFXComboBox<String> field) {
        field.getValidators().add(validator_field);
        field.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (date_field.getValue() != null) {
                if (date_field.getValue().compareTo(LocalDate.now()) >= 0) {
                    if (loadTimeSlots.getState() == Task.State.READY)
                        loadTimeSlots.start();
                }
                if (newValue) {
                    field.resetValidation();
                }
            }
        });
    }

    private void setupTimeSlotsField(Map<Timestamp, Boolean> timeSlots) {
        timeSlots.forEach((k, v) -> {
            Timestamp tmp = new Timestamp(System.currentTimeMillis());
            if (!v && k.compareTo(tmp) >= 0) {
                String hours_date = new SimpleDateFormat("HH:mm").format(k.getTime());
                hour_field.getItems().add(hours_date);
            }
        });
    }


    // --------------------
    //  Update methods
    // --------------------
    private void updatePatientLabel() {
        if (patients.size() == 3) {
            add_patient_button.setDisable(true);
        } else {
            label_patients.setText("Patient - " + (patients.size() + 1));
            add_patient_button.setDisable(false);
        }
    }


}
