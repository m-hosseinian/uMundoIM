package de.tudarmstadt.tk.umundoim.constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.tudarmstadt.tk.umundoim.datasrtucture.IMSubscription;

/**
 * Created by Mohammad on 5/9/2015.
 */
public final class Constants {
    public static String userName = "";
    public static Map<String, IMSubscription> subscriptionMap = new HashMap();//the key is a trend
    public static ArrayList<String> trendsDropDownList = new ArrayList<>();
    public static Map<String,Boolean> subscriptionStatus = new HashMap<>();
    public static ArrayList<String> newTrends = new ArrayList<>();

}
