package RezRem;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class SendRequest {

	private static final String USER_AGENT = "Mozilla/5.0";
	
	public static HttpRequestResult sendGet(String url, String cookie) {
		
		StringBuffer result = new StringBuffer();
		
		HttpResponse response = null;
		
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			
			HttpGet request = new HttpGet(url);

			request.setHeader("User-Agent", USER_AGENT);
			request.setHeader("Cookie", cookie);
			
			response = httpClient.execute(request);

			System.out.println("\nSending 'GET' request to URL : " + url);
			System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
			
			BufferedReader rd;
			rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			
			String line = new String();
			
			while (rd.ready() && (line = rd.readLine()) != null) {
				
				result.append(line);
				
			}
			
		} catch (Exception e) {
			
			// handler
			
		}
		
		return new HttpRequestResult(result.toString(), response);
		
	}
	
	public static HttpRequestResult sendPost(String url, String cookie, List<NameValuePair> urlParameters) {
	
		StringBuffer result = new StringBuffer();
		
		HttpResponse response = null;
		
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			
			HttpPost post = new HttpPost(url);

			post.setHeader("User-Agent", USER_AGENT);
			post.setHeader("Cookie", cookie);
			
			post.setEntity(new UrlEncodedFormEntity(urlParameters));

			response = httpClient.execute(post);
			
			System.out.println("\nSending 'POST' request to URL : " + url);
			System.out.println("Post parameters : " + post.getEntity());
			System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
			
			BufferedReader rd;
			
			rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			
			String line = new String();
			
			while (rd.ready() && (line = rd.readLine()) != null) {

				result.append(line);
				
			}
			
		} catch (Exception e) {
			
			// handler
			
		}
		
		return new HttpRequestResult(result.toString(), response);

	}
	
}

class HttpRequestResult {
	
	private final String result;
	
	private final HttpResponse response;
	
	public HttpRequestResult(String result, HttpResponse response) {
		
		this.result = result;
		
		this.response = response;
		
	}
	
	public String getResult() {
		
		return result;
		
	}
	
	public HttpResponse getResponse() {
		
		return response;
		
	}
	
}