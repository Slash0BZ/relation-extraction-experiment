package org.cogcomp.re;

import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;
import org.cogcomp.Datastore;

import java.io.File;
import java.util.Properties;

/**
 * Created by xuany on 10/22/2017.
 */
public class DatastorePublisher {
    public static void main(String[] args){
        try {
            Property ENDPOINT = new Property("ENDPOINT", "http://smaug.cs.illinois.edu:8080");
            Property ENDPOINT2 = new Property("datastoreEndpoint", "http://smaug.cs.illinois.edu:8080");
            Property SECRETKEY = new Property("SECRET-KEY", "");
            Property ACCESSKEY = new Property("ACCESS-KEY", "");
            Property[] properties = {ENDPOINT, ENDPOINT2, SECRETKEY, ACCESSKEY};
            Properties props = new Properties();
            for (Property property : properties)
                props.setProperty(property.key, property.value);
            Datastore ds = new Datastore(ENDPOINT.value, ACCESSKEY.value, SECRETKEY.value);
            File ff = new File("ACE_TEST_DOCS");
            if (!ff.exists()){
                System.exit(-1);
            }
            ds.publishDirectory("org.cogcomp.re", "ACE_TEST_DOCS", 1.0, ff.getAbsolutePath(), false, true);
            File f = ds.getDirectory("org.cogcomp.re", "ACE_TEST_DOCS", 1.0, false);
            System.out.println("f: " + f);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
