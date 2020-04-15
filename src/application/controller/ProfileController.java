package application.controller;

import application.App;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import data.Consultation;
import data.Job;
import data.Patient;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;
import oracle.jdbc.proxy.annotation.Pre;

import java.net.URL;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ProfileController extends ParentController implements Initializable {

    // Fields
    public JFXTextField name_field;
    public JFXTextField last_name_field;
    public JFXDatePicker birthday_field;
    public JFXComboBox<String> gender_field;
    public JFXComboBox<String> relation_field;
    public JFXTextField discovery_field;
    public JFXComboBox<Job> jobs_list_field;
    public VBox box_consultations;

    // Attributes
    Patient tmp_p;
    ArrayList<Integer> consultation_id;

    // --------------------
    //   Initialize method
    // --------------------
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        super.date_today = Calendar.getInstance();

        tmp_p = PatientsController.list_patients.get(PatientsController.current_patient_id-1);

        name_field.setText(tmp_p.getName());

        last_name_field.setText(tmp_p.getLast_name());

        if(tmp_p.getBirthday() == null) {
            System.out.println("t");
        }
        else{
            birthday_field.setDisable(true);
            birthday_field.setValue(tmp_p.getBirthday().toLocalDate());
        }


        gender_field.getItems().addAll( "homme", "femme", "non binaire");
        gender_field.setValue(tmp_p.getGender());

        relation_field.getItems().addAll("célibataire", "couple", "autre");
        relation_field.setValue(tmp_p.getRelationship());

        discovery_field.setText(tmp_p.getDiscovery_way());

        setupConsultationHistory();

    }

    public void setupConsultationHistory(){

        consultation_id = new ArrayList<>();
        try{
            PreparedStatement preparedStatement = App.database.getConnection().prepareStatement("select c.CONSULTATION_ID\n" +
                    "from CONSULTATION c\n" +
                    "join CONSULTATION_CARRYOUT CC on c.CONSULTATION_ID = CC.CONSULTATION_ID\n" +
                    "where PATIENT_ID = ?");

            preparedStatement.setInt(1, tmp_p.getPatient_id());
            ResultSet result = preparedStatement.executeQuery();
            while(result.next()){
                consultation_id.add(result.getInt(1)); // add id
            }
            consultation_id.forEach(System.out::println);
            // Build consultation button
            setupBoxConsultations();

        }catch (SQLException ex){
            ex.printStackTrace();
        }

    }

    @Override
    protected void setupBoxConsultations() {
        consultations_map = new HashMap<>();
        box_consultations.setSpacing(20);
        for (Integer i: consultation_id
             ) {
            consultations_map.put(Consultation.getDateById(i), buildConsultationButton(i));
        }
        // sort in descending order
        consultations_map = new TreeMap<>(consultations_map).descendingMap();
        // add all button
        consultations_map.forEach((k,v)->{
            if(k.compareTo(date_today) < 0){
                v.setStyle("-fx-background-color: #eceff1;");
            }
            v.getStyleClass().add("patient_consultation_cell");
            box_consultations.getChildren().add(v);
        });
    }


}
