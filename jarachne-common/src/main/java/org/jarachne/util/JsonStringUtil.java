package org.jarachne.util;

import net.sf.json.JSONObject;

public class JsonStringUtil {

	/**
	 * require keyValues.length % 2 == 0, keyValues[i] is key, keyValues[i+1] is value!
	 * @param keyValues
	 * @return
	 */
	public static String JSONFormatString(Object... keyValues){
		JSONObject json = new JSONObject();
		for(int i = 0 ; i < keyValues.length; i+=2){
			json.put(keyValues[i], keyValues[i+1]);
		}
		return json.toString();
	}
}
