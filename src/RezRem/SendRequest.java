package RezRem;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import java.io.IOException;
import org.apache.http.message.BasicNameValuePair;


public class SendRequest {

	private final String USER_AGENT = "Mozilla/5.0";
	
	public void sendGet(String url, String cookie) {
		
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {

		    // no need to close httpClient explicitly
			
			HttpGet request = new HttpGet(url);
			
			// add request header
			request.addHeader("User-Agent", USER_AGENT);
			request.addHeader("Cookie", cookie);
			
			HttpResponse response = httpClient.execute(request);
		
			System.out.println("\nSending 'GET' request to URL : " + url);
			System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
		
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		
			StringBuffer result = new StringBuffer();
			
			String line = "";
			
			while ((line = rd.readLine()) != null) {
				
				result.append(line);
			
			}
		
			System.out.println(result.toString());

		} catch (IOException e) {

		    // handle

		}
	
	}
	
	// HTTP POST request
	public void sendPost(String url, String cookie) {
	
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			
			HttpPost post = new HttpPost(url);

			// add header
			post.setHeader("User-Agent", USER_AGENT);
			post.addHeader("Cookie", cookie);
			
			List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
			
			urlParameters.add(new BasicNameValuePair("saturday", "1451757997"));
			urlParameters.add(new BasicNameValuePair("delivery_id", "2"));
			urlParameters.add(new BasicNameValuePair("t", "1451757997"));
			urlParameters.add(new BasicNameValuePair("_[35139]", "on"));
			
			post.setEntity(new UrlEncodedFormEntity(urlParameters));

			HttpResponse response = httpClient.execute(post);
			
			System.out.println("\nSending 'POST' request to URL : " + url);
			System.out.println("Post parameters : " + post.getEntity());
			System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
		
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

			StringBuffer result = new StringBuffer();
			
			String line = "";
			
			while ((line = rd.readLine()) != null) {
				
				result.append(line);
			
			}

			System.out.println(result.toString());
			
		} catch (Exception e) {
			
			
			
		}

	}
	
}