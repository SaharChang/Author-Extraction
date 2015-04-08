package authorQueryWeb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONArray; // JSON library from http://www.json.org/java/
import org.json.JSONObject;

import authorExtract.DicoDomParse;

/**
 * Query Google avec JSON
 * 
 * Librairie pour poser des requêtes sur Google.
 * 
 * Le nombre de résultat est limité.
 * 
 * @author changuel
 * 
 */
public class GoogleQuery {

	// Put your website here
	private final String HTTP_REFERER = "http://www.example.com/";
	int count = 5;
	ArrayList<String> listadress = new ArrayList<String>();
	ArrayList<String> listTitles = new ArrayList<String>();
	ArrayList<String> listauthors = new ArrayList<String>();

	void query() {
		makeQuery("Probability introduction");
		makeQuery("Wave Mechanics introduction");
		makeQuery("Number Theory introduction");
	}

	private void makeQuery(String query) {

		System.out.println(" Querying for " + query);
		for (int j = 0; j < count; j++) {

			try {
				// Convert spaces to +, etc. to make a valid URL
				query = URLEncoder.encode(query, "UTF-8");

				URL url = new URL(
						"http://ajax.googleapis.com/ajax/services/search/web?start="
								+ j * 8 + "&rsz=large&v=1.0&q=" + query);
				URLConnection connection = url.openConnection();
				connection.addRequestProperty("Referer", HTTP_REFERER);

				// Get the JSON response
				String line;
				StringBuilder builder = new StringBuilder();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(connection.getInputStream()));
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}

				String response = builder.toString();
				JSONObject json = new JSONObject(response);

				System.out.println("Total results = "
						+ json.getJSONObject("responseData").getJSONObject(
								"cursor").getString("estimatedResultCount"));

				JSONArray ja = json.getJSONObject("responseData").getJSONArray(
						"results");

				System.out.println(" Results:");
				for (int i = 0; i < ja.length(); i++) {
					JSONObject object = ja.getJSONObject(i);
					String adress = object.getString("url");
					if (!adress.toLowerCase().contains("wikipedia")
							&& !adress.toLowerCase().contains("amazon")
							&& !adress.toLowerCase().contains("wiki")
							&& !adress.toLowerCase().contains("book")
							&& !adress.endsWith("pdf")) {
						System.out.println(object.getString("url"));
						listadress.add(object.getString("url"));
						listTitles.add(object.getString("titleNoFormatting"));
						listauthors.add("aaa");
					}
				}
			} catch (Exception e) {
				System.err.println("Something went wrong...");
				e.printStackTrace();
			}
		}
	}

	public static void main(String args[]) {
		GoogleQuery GQ = new GoogleQuery();
		GQ.query();
	}
}