package com.musigma.controllers;

import com.musigma.models.Festival;

public abstract class WorkspaceController {

    public static class WorkspaceRegister {
        public String name;
        public String iconPath;
        public String viewPath;

        public WorkspaceRegister(String name, String iconPath, String viewPath) {
            this.name = name;
            this.iconPath = iconPath;
            this.viewPath = viewPath;
        }
    }

    public static WorkspaceRegister REGISTER;

    protected Festival festival;

    public void initialize(Festival festival) {
        this.festival = festival;
    }
}
