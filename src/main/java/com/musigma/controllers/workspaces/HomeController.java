package com.musigma.controllers.workspaces;

import com.musigma.controllers.WorkspaceController;
import com.musigma.models.Festival;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class HomeController extends WorkspaceController {
    public static WorkspaceRegister REGISTER = new WorkspaceRegister(
        "Home",
        "/com/musigma/images/icons/home.png",
        "/com/musigma/views/home-view.fxml"
    );
    public DatePicker festivalStart;

    @FXML
    TextField festivalName;

    @FXML
    DatePicker festivalDate;

    @FXML
    TextField festivalLocation;

    @FXML
    TextField festivalArea;

    @FXML
    TextField festivalLocationPrice;

    @Override
    public void initialize(Festival festival){
        super.initialize(festival);
        setInput();
    }

    private void setInput() {
        festivalName.setText(festival.getName());
        festivalLocation.setText(festival.getLocation());
        festivalArea.setText(Double.toString(festival.getArea()));
        festivalLocationPrice.setText(Float.toString(festival.getLocationPrice()));
    }
}
