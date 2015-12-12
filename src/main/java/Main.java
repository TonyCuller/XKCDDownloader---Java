import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Simple XKCD Downloader
 */
public class Main {


    public static void main(String[] args) throws IOException {
        final String downloadDestination = "C:\\Users\\atony_000\\Documents\\XKCD\\";


        int number;

        Gson gson = new GsonBuilder().create();

        XKCDData data = gson.fromJson(getJson("http://xkcd.com/info.0.json"), XKCDData.class);

        number = data.getNum();


        if (!new File(downloadDestination + data.getYear()).exists()) {
            new File(downloadDestination + data.getYear()).mkdirs();
        }

        //Download latest XKCD to Folder already set up
        try (InputStream in = new URL(data.getImg()).openStream()) {
            if (!new File(downloadDestination + data.getYear() + "\\" + data.getSafe_title() + ".png").exists()) {
                Files.copy(in, Paths.get(downloadDestination + data.getYear() + "\\" + data.getSafe_title() + ".png"));
            }

        }

        //Download the rest using a loop
        for (int i = number - 1; i > 0; i--) {

            //Skip 1608 because the img points to nowhere
            if (i == 1608) {
                i = 1607;
            }

            //Very funny...
            else if (i == 404) {
                i = 403;
            }

            data = gson.fromJson(getJson("http://xkcd.com/" + i + "/info.0.json"), XKCDData.class);
            String title = data.getSafe_title();

            title = title.replaceAll("[^A-Za-z0-9()'\\- ]", " "); //Use Regex to format title

            if (!new File(downloadDestination + data.getYear()).exists()) {
                new File(downloadDestination + data.getYear()).mkdirs();
            }


            if (new File(downloadDestination + data.getYear() + "\\" + title + ".png").exists()) { //Make sure it doesn't download the same thing twice
                continue;
            }

            try (InputStream in = new URL(data.getImg()).openStream()) {
                Files.copy(in, Paths.get(downloadDestination + data.getYear() + "\\" + title + ".png"));
            }


        }

    }

    public static String getJson(String data) throws IOException {
        CloseableHttpClient XKCDClient = HttpClients.createDefault();
        HttpGet get = new HttpGet(data);
        CloseableHttpResponse response = XKCDClient.execute(get);
        StringBuilder result = new StringBuilder();

        try {

            BufferedReader reader = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));

            String line;


            while ((line = reader.readLine()) != null) {
                result.append(line);
            }


        } catch (IOException e) {
            System.err.println(e.getCause().getLocalizedMessage());
        } finally {
            XKCDClient.close();
            response.close();

        }
        return result.toString();
    }
}


