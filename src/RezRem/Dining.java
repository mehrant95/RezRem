package RezRem;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class Dining {
	
	private String userName;
	
	private String password;
	
	private String login_url = "http://reserve.dining.sharif.ir/login";
	
	private String symfony_cookie;
	
	public String getUserName() {
		
		return userName;
		
	}

	public void setUserName(String userName) {
		
		this.userName = userName;
	
	}

	public String getPassword() {
	
		return password;
	
	}

	public void setPassword(String password) {
	
		this.password = password;
	
	}
	
	public boolean logIn() {
		
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		
		urlParameters.add(new BasicNameValuePair("signin[username]", userName));
		urlParameters.add(new BasicNameValuePair("signin[password]", password));
		
		HttpRequestResult res = SendRequest.sendPost(login_url, null, urlParameters);
		
		HttpResponse response = res.getResponse();
		
		if (response != null) {
			
			int statusCode = response.getStatusLine().getStatusCode();
			
			if (statusCode == 200 || statusCode == 302) {
				
				Document doc = Jsoup.parse(res.getResult());
				
				Elements elements = doc.select("form[action=/login] .error-box");
				
				if (elements.isEmpty()) {
					
					try {
						
						symfony_cookie = response.getHeaders("Set-Cookie")[0]
								.toString().split(" ")[1].split(";")[0];
					
					} catch (Exception e) {
						
						// handle
						
					}
					
				}
				
				return elements.isEmpty();
				
			}
			
		}
		
		return false;
		
	}

}