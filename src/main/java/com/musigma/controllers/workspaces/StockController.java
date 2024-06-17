package com.musigma.controllers.workspaces;

import com.musigma.controllers.WorkspaceController;

public class StockController extends WorkspaceController {
    public static WorkspaceRegister REGISTER = new WorkspaceRegister(
            "Stock",
            "/com/musigma/images/icons/stock.png",
            "/com/musigma/views/stock-view.fxml"
    );
}
