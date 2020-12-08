package com.netflix.eureka.zuul.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringHelper {
    public static String getObjectValue(Object obj) {
        return obj==null ? "" : obj.toString();
    }
    
    public static List<String> getObjectList(Object obj) {
    	if(obj == null) {
    		return new ArrayList<String>();
    	}
    	
        return Arrays.asList(String.valueOf(obj).split("&&"));
    }
}
