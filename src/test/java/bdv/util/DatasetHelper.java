package bdv.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class DatasetHelper {

    // https://downloads.openmicroscopy.org/images/

    public static File cachedSampleDir = new File(System.getProperty("user.home"),"CachedSamples");

    final public static String TIF3D_TIMELAPSE = "https://github.com/NicoKiaru/TestImages/raw/master/CElegans/dub-0.5xy-TP1-22.tif";
    final public static String BDV_HisYFP_SPIM_XML = "https://downloads.openmicroscopy.org/images/BDV/samples/HisYFP-SPIM.xml";
    final public static String BDV_HisYFP_SPIM_H5 = "https://downloads.openmicroscopy.org/images/BDV/samples/HisYFP-SPIM.h5";
    final public static String BDV_Drosophila_XML = "https://downloads.openmicroscopy.org/images/BDV/samples/drosophila.xml";
    final public static String BDV_Drosophila_H5 = "https://downloads.openmicroscopy.org/images/BDV/samples/drosophila.h5";

    public static File getDataset(String urlString) {
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return urlToFile(url);
    }

    public static void main(String... args) {
        System.out.println("Downloading sample datasets.");
        ASyncDL(BDV_HisYFP_SPIM_XML);
        ASyncDL(BDV_HisYFP_SPIM_H5);
        ASyncDL(BDV_Drosophila_XML);
        ASyncDL(BDV_Drosophila_H5);
        ASyncDL(TIF3D_TIMELAPSE);
    }

    public static Thread ASyncDL(String str) {
        Thread thread = new Thread(() -> getDataset(str));
        thread.start();
        return thread;
    }

    public static File urlToFile(URL url) {
        try {
            File file_out = new File(cachedSampleDir,url.getFile());
            if (file_out.exists()) {
                return file_out;
            } else {
                System.out.println("Downloading and caching: "+url+" size = "+(getFileSize(url)/1024)+" kb");
                FileUtils.copyURLToFile(url, file_out, 10000, 10000);
                System.out.println("Downloading and caching of "+url+" completed successfully ");
                if (FilenameUtils.getExtension(file_out.getAbsolutePath()).equals(".vsi")) {
                    // We need to download all the subfolders
                }
                return file_out;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // https://stackoverflow.com/questions/12800588/how-to-calculate-a-file-size-from-url-in-java
    private static int getFileSize(URL url) {
        URLConnection conn = null;
        try {
            conn = url.openConnection();
            if(conn instanceof HttpURLConnection) {
                ((HttpURLConnection)conn).setRequestMethod("HEAD");
            }
            conn.getInputStream();
            return conn.getContentLength();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if(conn instanceof HttpURLConnection) {
                ((HttpURLConnection)conn).disconnect();
            }
        }
    }

}
