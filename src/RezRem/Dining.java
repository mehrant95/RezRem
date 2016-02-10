package RezRem;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Dining {
	
	private String userName;
	
	private String password;
	
	private String login_url = "http://reserve.dining.sharif.ir/login";
	
	private String foods_url = "http://reserve.dining.sharif.ir";
	
	private String symfony_cookie;
	
	private String ref;
	
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
		
		setInitialCookie();
		
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		
		urlParameters.add(new BasicNameValuePair("signin[username]", userName));
		urlParameters.add(new BasicNameValuePair("signin[password]", password));
		
		HttpRequestResult res = SendRequest.sendPost(login_url, symfony_cookie, urlParameters);
		
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
	
	public void setInitialCookie() {
		
		try {
		
			HttpRequestResult res = SendRequest.sendGet(foods_url, "");
			
			HttpResponse response = res.getResponse();
			
			symfony_cookie = response.getHeaders("Set-Cookie")[0]
					.toString().split(" ")[1].split(";")[0];
			
			
		} catch (Exception e) {
			
			//handler
			
		}
		
	}
	
	public String getName() {
		
		String result = null;
		
		HttpRequestResult res = SendRequest.sendGet(foods_url, symfony_cookie);
		
		if (res.getResponse() != null) {
			
			if (res.getResponse().getStatusLine().getStatusCode() == 200) {
				
				Document doc = Jsoup.parse(res.getResult());
				
				Elements elements = doc.select("tbody tr:nth-child(2) td:nth-child(3) blockquote h1:nth-of-type(2)");
				
				if (!elements.isEmpty()) {
					
					Element element = elements.get(0);
					
					String[] wel_mes = element.ownText().split(" ");
					
					if (wel_mes.length > 1)
						result = wel_mes[1];
					
				}
				
			}
			
		}
		
		return result;
		
	}
	
	public String reserveWeek(String url, int delivery_id) {
		
		HttpRequestResult res = SendRequest.sendGet(url + delivery_id, symfony_cookie);
		
		ArrayList<String> food_ids = new ArrayList<String>();
		
		if (res.getResponse() != null) {
			
			if (res.getResponse().getStatusLine().getStatusCode() == 200) {
				
				Document doc = Jsoup.parse(res.getResult());
				
				Elements temp1, temp2;
				
				Elements elements = doc.select("form #datagrid tbody > tr");
				
				for (Element element : elements) {
					
					temp1 = element.select("td");
					
					if (!temp1.isEmpty()) {
						
						temp2 = temp1.select("p:first-of-type input");
						
						if (!temp2.isEmpty()) {
							
							food_ids.add(temp2.get(0).attr("name"));
							
						}
						
					}
					
					temp1 = element.select("td:nth-of-type(3)");
					
					if (!temp1.isEmpty()) {
						
						temp2 = temp1.select("p:first-of-type input");
						
						if (!temp2.isEmpty()) {
							
							food_ids.add(temp2.get(0).attr("name"));
							
						}
						
					}
					
				}
				
			}
			
		}
		
		String saturday = ref.split("&")[0].split("=")[1];
		
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		
		urlParameters.add(new BasicNameValuePair("saturday", saturday));
		urlParameters.add(new BasicNameValuePair("delivery_id", Integer.toString(delivery_id)));
		urlParameters.add(new BasicNameValuePair("t", saturday));
		
		for (int i = 0 ; i < food_ids.size() ; i++)
			urlParameters.add(new BasicNameValuePair(food_ids.get(i), "on"));
		
		res = SendRequest.sendPost(foods_url, symfony_cookie, urlParameters);
		
		if (res.getResponse() != null) {
			
			if (res.getResponse().getStatusLine().getStatusCode() == 200) {
				
				Document doc = Jsoup.parse(res.getResult());
				
				if (doc.select(".error-box:not(.success)").isEmpty())
					return "";
				else
					return "Consider charging your account!\n";
				
			}
			
		}
		
		return "error connecting!\n";
		
	}
	
	public String reserveNextWeek() {
		
		String result, temp;
		
		result = reserveWeek(foods_url + ref, 2);
		
		temp = reserveWeek(foods_url + ref, 5);
		
		if (result.trim().isEmpty())
			result += temp;
		
		return result;
		
	}
	
	public boolean nextWeekIsReserved(int delivery_id) {
		
		HttpRequestResult res = SendRequest.sendGet(foods_url, symfony_cookie);
		
		if (res.getResponse() != null) {
			
			if (res.getResponse().getStatusLine().getStatusCode() == 200) {
				
				Document doc = Jsoup.parse(res.getResult());
				
				Elements elements = doc.select("#datagrid tfoot > tr:first-of-type a:nth-of-type(3)");
				
				if (!elements.isEmpty()) {
					
					Element element = elements.get(0);
					
					ref = element.attr("href");
					
					ref = ref.substring(0, ref.length()-1);
					
					res = SendRequest.sendGet(foods_url + ref + delivery_id, symfony_cookie);
					
					doc = Jsoup.parse(res.getResult());
					
					elements = doc.select("form #datagrid input[checked]");
					
					return !elements.isEmpty();
					
				}
				
			}
			
		}
		
		return false;
		
	}

}