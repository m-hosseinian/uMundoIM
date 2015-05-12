package de.tudarmstadt.tk.umundoim.constant;

import java.util.HashMap;

/**
 * Created by Mohammad on 5/9/2015.
 */
public final class Constants {
    public static String USER_NAME = "";
    public HashMap<String,Boolean> Trends = new HashMap();

    public Constants() {
        Trends.put("TK3",false);
        Trends.put("PILOTY",false);
        Trends.put("MENSA",false);
        Trends.put("CRYPTO",false);
        Trends.put("TK1",false);
        Trends.put("KN2",false);
        Trends.put("SEDC",false);

    }
}
