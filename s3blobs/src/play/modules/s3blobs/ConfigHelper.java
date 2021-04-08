package play.modules.s3blobs;

import play.Play;
import play.exceptions.ConfigurationException;

public class ConfigHelper {
	
    public static boolean getBoolean(String configKey, boolean defaultValue) {
        String asStr = Play.configuration.getProperty(configKey);
        if (asStr == null || asStr.length() == 0) {
            return defaultValue;
        }

        if (asStr.equals("true") || asStr.equals("false")) {
            return Boolean.parseBoolean(asStr);
        }

        throw new ConfigurationException(configKey + " must be either true or false");
    }	

}
