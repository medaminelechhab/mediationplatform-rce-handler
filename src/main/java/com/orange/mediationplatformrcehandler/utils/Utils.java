package com.MyProject.mediationplatformrcehandler.utils;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    private Utils(){}
    
    public static <T> List<List<T>> splitList(List<T> originalList, int sublistSize) {
        List<List<T>> sublists = new ArrayList<>();

        for (int i = 0; i < originalList.size(); i += sublistSize) {
            int endIndex = Math.min(i + sublistSize, originalList.size());
            sublists.add(originalList.subList(i, endIndex));
        }

        return sublists;
    }
}
