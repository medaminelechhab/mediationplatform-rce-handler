package com.MyProject.mediationplatformrcehandler.service.utils;

public class FilesManagement {

    public enum TypeManagement {
        DAILY,SUNDAY;

        public static TypeManagement getTypeManagement(String typeManagement) {
            if ("DAILY".equals(typeManagement)) return DAILY;
            return SUNDAY;
        }
    }
}
