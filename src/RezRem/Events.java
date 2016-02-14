package RezRem;

import java.awt.SystemTray;
import java.awt.TrayIcon;

import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.events.FinishLoadingEvent;
import com.teamdev.jxbrowser.chromium.events.LoadAdapter;

import javafx.application.Platform;
import javafx.stage.Stage;

public class Events {
	
	private boolean firstMinimize;
	
	private String firstName;
	
	private final Browser browser;
	
	private final TrayIcon trayIcon;
	
	private final Dining dining;
	
	private final Main main;
	
	private final Stage primaryStage;
	
	private String loadingMessage;
	
	private boolean reservedNextWeek;
	
	public Events(Browser browser, TrayIcon trayIcon, Dining dining, Main main, Stage primaryStage) {
		
		this.browser = browser;
		
		this.trayIcon = trayIcon;
		
		this.dining = dining;
		
		this.main = main;
		
		this.primaryStage = primaryStage;
		
		firstMinimize = true;
		
	}
	
	public void setLoadingMessage(String loadingMessage) {
		
		this.loadingMessage = loadingMessage;
		
	}
	
	public void setReservedNextBool(boolean reservedNextWeek) {
		
		this.reservedNextWeek = reservedNextWeek;
		
	}
	
	public boolean getReservedNextWeek() {
		
		return reservedNextWeek;
		
	}
	
	public void setFirstName(String firstName) {
		
		this.firstName = firstName;
		
	}
	
	public void close() {
		
		Platform.runLater(new Runnable() {
			
			@Override
			public void run() {
				
				closeToTray();
				
			}
			
		});
				
	}
	
	public void minimize() {
		
		Platform.runLater(new Runnable() {
			
			@Override
			public void run() {
				
				primaryStage.setIconified(true);
				
			}
			
		});
		
	}
	
	private void closeToTray() {
    	
        Platform.runLater(new Runnable() {
            
        	@Override
            
        	public void run() {
                
        		if (SystemTray.isSupported()) {
                    
        			primaryStage.hide();
        			
                    showMinimizeMessage();
                    
                } 
        		else {
        			
                    System.exit(0);
                
        		}
        		
            }
        	
        });
        
    }
	
	public String getLoadMessage() {
		
		return loadingMessage;
		
	}
	
	private void showMinimizeMessage() {
        
    	if (firstMinimize) {
            
    		trayIcon.displayMessage("RezRem",
                    "RezRem is running in background.",
                    TrayIcon.MessageType.INFO);
            
    		firstMinimize = false;
        
    	}
    	
    }
	
	public void login(String userName, String password) {
		
		if (userName.trim().isEmpty() || password.trim().isEmpty())
			return;
		
		loadingMessage = "œ— Õ«· Ê—Êœ »Â ”«„«‰Â ...";
		
		main.setLoadMessageOnPage();
		
		browser.loadURL(Main.class.getResource("templates/loading.html").toExternalForm());
		
		dining.setUserName(userName);
		
		dining.setPassword(password);
		
		main.doLogin();
		
	}
	
	public void goToMain() {
		
		if (firstName == null) {
			
			firstName = dining.getName();
			
		}
		
		main.setNameOnPage();
		
		browser.loadURL(Main.class.getResource("templates/main.html").toExternalForm());
		
	}
	
	public void nowReserve() {
		
		if (!reservedNextWeek)
			main.reserve();
		
	}
	
	public void reserveDone() {
		
		if (!reservedNextWeek)
			main.changeReserveBool(true);
		
	}
	
	public void goToFirst() {
		
		main.setNameOnPage();
		
		main.setReserveBoolOnPage();
		
		browser.loadURL(Main.class.getResource("templates/simple.html").toExternalForm());
		
	}
	
	public void exit() {
		
		firstName = null;
		
		browser.loadURL(Main.class.getResource("templates/login.html").toExternalForm());
		
		final LoadAdapter loadAdapter = new LoadAdapter() {
			
			@Override
		    public void onFinishLoadingFrame(FinishLoadingEvent event) {
				
		        if (event.isMainFrame()) {
		        	
		        	browser.removeLoadListener(this);
		        	
		        	loginLoaded();
		        
		        }
		        
		    }
			
		};
		
		browser.addLoadListener(loadAdapter);
		
	}
	
	public void loginLoaded() {
		
		if (!dining.getUserName().trim().isEmpty()) {
			
			browser.executeJavaScript("$(\"label[for='student_number']\").addClass('active');");
			
			browser.executeJavaScript("$('#student_number').attr('value', '" + dining.getUserName() + "');");
			
		}
		
		if (!dining.getPassword().trim().isEmpty()) {
		
			browser.executeJavaScript("$(\"label[for='password']\").addClass('active');");
		
			browser.executeJavaScript("$('#password').attr('value', '" + dining.getPassword() + "');");
	
		}
		
	}
	
	public String getFirstName() {
		
		return firstName == null ? "" : firstName;
		
	}
		
}