package fr.inria.corese.command.utils.coreseCoreWrapper;

import fr.inria.corese.core.util.CoreseInfo;
import fr.inria.corese.core.util.Property;
import fr.inria.corese.core.util.Property.Value;

/**
 * Wrapper class for corese core utils package
 * Wraps interactions with corese.core.util.Property and corese.core.util.CoreseInfo
 */
public class CoreseUtils {
    
    public static String getCoreseVersion() {
        return CoreseInfo.getVersion();
    }

	public static void loadProperty(String path) {
        try {
            Property.load(path.toString());
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to open config file: " + path.toString(), e);
        }
	}

    public static void setProperty(Value owlProperty, boolean value) {
        Property.set(owlProperty, value);
    }
}
