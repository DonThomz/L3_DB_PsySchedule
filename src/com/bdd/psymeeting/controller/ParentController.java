package com.bdd.psymeeting.controller;

import com.bdd.psymeeting.model.Consultation;
import com.bdd.psymeeting.model.Patient;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

public class ParentController {

    // FXML
    @FXML
    public StackPane stackPane;
    @FXML
    public VBox box_consultations;

    // Attributes
    protected Map<Calendar, JFXButton> consultations_map;
    protected int consultation_size;
    protected Calendar date_today;

    // --------------------
    //  Get methods
    // --------------------
    public StackPane getStackPane() {
        return stackPane;
    }


    // --------------------
    //  Set methods
    // --------------------
    public void setStackPane(StackPane stackPane) {
        this.stackPane = stackPane;
    }


    // --------------------
    //  Consultation methods
    // --------------------

    protected boolean setupBoxConsultations() {
        return true;
    }

    protected Consultation buildConsultationButton(int consultation_id) {

        // request SQL
        Consultation consultation = new Consultation(consultation_id);

        // init button
        JFXButton consultation_button = new JFXButton();
        // setting button
        consultation_button.setId("consultation-button-id-" + consultation_id);


        // Setup content
        VBox box = new VBox();

        assert consultation.getDate() != null;
        @SuppressWarnings("SpellCheckingInspection") String timeStamp = new SimpleDateFormat("EEEE dd MMMM, yyyy à HH:mm",
                Locale.FRANCE).format(consultation.getDate().getTime());
        Label title = new Label("Consultation : "
                + "\n\t" + timeStamp);

        box.getChildren().add(title);

        // create label and add patients
        Label patient_list = new Label();
        patient_list.getStyleClass().add("content_text");
        StringBuilder content = new StringBuilder();
        for (Patient p : consultation.getPatients()
             ) {
            content.append(" | ").append(p.getName()).append(" ").append(p.getLast_name()).append(" \n");
        }
        patient_list.setText(String.valueOf(content));
        box.getChildren().add(patient_list);


        // add action on button
        consultation_button.setOnAction(event -> loadConsultationInfo(consultation));

        // add to the button
        consultation_button.setGraphic(box);

        // add JFXButton to consultation
        consultation.setConsultation_button(consultation_button);

        return consultation;
    }

    protected void loadConsultationInfo(Consultation consultation) {
        // create dialog layout
        JFXDialogLayout content = new JFXDialogLayout();

        // add heading
        content.setHeading(createTitle(consultation.getDate()));

        // add body
        content.setBody(createBody(consultation));

        JFXDialog dialog = new JFXDialog(stackPane, content, JFXDialog.DialogTransition.CENTER);
        JFXButton done = new JFXButton("Fermer");
        JFXButton modify = new JFXButton("Modifier");

        done.setOnAction(event -> dialog.close());

        modify.setOnAction(event -> dialog.close());

        content.setActions(modify, done);

        dialog.show();
    }

    protected TextArea createBody(Consultation consultation) {

        // get Patients Info
        StringBuilder patientsInfo = new StringBuilder();
        for (Patient p: consultation.getPatients()
             ) {
            patientsInfo.append("| ").append(p.getName()).append(" ").append(p.getLast_name());
        }

        // get Feedback Info
        StringBuilder feedbackInfo = new StringBuilder();

        // info price and pay mode
        feedbackInfo.append("Prix : ").append(consultation.getPrice()).append(" €, payé avec : ").append(consultation.getPayMode());

        // info feedback commentary, key words, postures, indicator
        feedbackInfo.append("\n\nRetour de séance").append("\n\n\tCommentaire : \n").append(consultation.getFeedback().getCommentary());
        if (consultation.getFeedback().getKeyword() != null)
            feedbackInfo.append("\n\n\tMots clés :").append(consultation.getFeedback().getKeyword());
        if (consultation.getFeedback().getPosture() != null)
            feedbackInfo.append("\n\n\tPosture :").append(consultation.getFeedback().getPosture());
        if (consultation.getFeedback().getIndicator() != 0)
            feedbackInfo.append("\n\n\tIndicateur :").append(consultation.getFeedback().getIndicator());

        TextArea textArea = new TextArea("Patients :\n"
                + patientsInfo + "\n"
                + feedbackInfo + "\n"
        );
        textArea.setWrapText(true);
        return textArea;
    }

    protected Label createTitle(Calendar date) {
        @SuppressWarnings("SpellCheckingInspection") String format_date = new SimpleDateFormat("EEEE dd MMMM, yyyy à HH:mm",
                Locale.FRANCE).format(date.getTime());
        return new Label("Consultation du " + format_date);
    }


}