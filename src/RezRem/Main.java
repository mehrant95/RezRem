package RezRem;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;

import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.JSValue;
import com.teamdev.jxbrowser.chromium.events.FinishLoadingEvent;
import com.teamdev.jxbrowser.chromium.events.LoadAdapter;
import com.teamdev.jxbrowser.chromium.events.ScriptContextAdapter;
import com.teamdev.jxbrowser.chromium.events.ScriptContextEvent;
import com.teamdev.jxbrowser.chromium.events.ScriptContextListener;
import com.teamdev.jxbrowser.chromium.javafx.BrowserView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.application.Platform;


public class Main extends Application {
	
	private Stage primaryStage;
	
	private Browser browser;
	
    private TrayIcon trayIcon;
    
    private File config;
    
    private File settings;
	
    private Dining dining;
    
    private Date lastReserve;
    
    private Events events;
    
	public static void main(String[] args) {
		
		launch(args);
		
	}
	
	@Override
	public void start(Stage primaryStage) {
		
		this.primaryStage = primaryStage;
		
		Platform.setImplicitExit(false);
		
		createTrayIcon();
		
		browser = new Browser();
		
		BrowserView browserView = new BrowserView(browser);
		
		StackPane pane = new StackPane();
		
		pane.getChildren().add(browserView);
		
		Scene scene = new Scene(pane, 330, 470);
		
		primaryStage.initStyle(StageStyle.UNDECORATED);
		
		primaryStage.setScene(scene);
		
		primaryStage.show();
		
		dining = new Dining();
		
		events = new Events(browser, trayIcon, dining, this, primaryStage);
		
		browser.addScriptContextListener(new ScriptContextAdapter() {
			
			@Override
			public void onScriptContextCreated(ScriptContextEvent event) {
				
					Browser browser = event.getBrowser();
		            
					JSValue value = browser.executeJavaScriptAndReturnValue("window");
		            
					value.asObject().setProperty("Events", events);
				
			}
			
		});
		
		settings = new File("src/RezRem/settings");
		
		if (settings.exists() && !settings.isDirectory()) {
			
			try {
				
				BufferedReader br = new BufferedReader(new FileReader(settings));
				
				SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
				
				lastReserve = format.parse(br.readLine());
				
				changeReserveBool(!(getDayDiff(new Date(), getExpDate()) > 0));
				
				br.close();
				
			} catch (Exception e) {
				
				// handler
				
				
			}
			
		}
		
		// read user name & password file
		
		config = new File("src/RezRem/user.config");
		
		if(!config.exists()) {
			
			browser.loadURL(Main.class.getResource("templates/login.html").toExternalForm());
		    
		}
		else if (!config.isDirectory()) {
			
			// login to dining
			
			try {
				
				BufferedReader br = new BufferedReader(new FileReader(config));
				
				String u_str = br.readLine();
				
				String p_str = br.readLine();
				
				br.close();
			
				dining.setUserName(EncryptUtils.decode(u_str).split("=")[1]);
				
				dining.setPassword(EncryptUtils.decode(p_str).split("=")[1]);
				
				events.setLoadingMessage("در حال ورود به سامانه ...");
				
				setLoadMessageOnPage();
				
				browser.loadURL(Main.class.getResource("templates/loading.html").toExternalForm());
				
				doLogin();
				
			} catch (Exception e) {
				
				config.delete();
				
				browser.loadURL(Main.class.getResource("templates/login.html").toExternalForm());
				
			}
			
		}
		
	}

	public void createTrayIcon() {
        
		if (SystemTray.isSupported()) {
			
            SystemTray tray = SystemTray.getSystemTray();
            
            final ActionListener closeListener = new ActionListener() {
                
            	@Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    
            		System.exit(0);
                
            	}
            
            };

            final ActionListener showListener = new ActionListener() {
                
            	@Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
            		
            		Platform.runLater(new Runnable() {
                        
            			@Override
                        public void run() {
                            
            				primaryStage.show();
            				
            			}
                    
            		});
                
            	}
            
            };

            PopupMenu popup = new PopupMenu();

            MenuItem showItem = new MenuItem("Show");
            showItem.addActionListener(showListener);
            popup.add(showItem);

            MenuItem closeItem = new MenuItem("Close");
            closeItem.addActionListener(closeListener);
            popup.add(closeItem);
            
            
            BufferedImage trayIconImage;
            
			try {
				
				trayIconImage = ImageIO.read(new FileInputStream("src/RezRem/img/R2_Logo.png"));
				
				int trayIconHeight = new TrayIcon(trayIconImage).getSize().height;
				
				int trayIconWidth = new TrayIcon(trayIconImage).getSize().width;
				
	            trayIcon = new TrayIcon(trayIconImage.getScaledInstance(trayIconWidth, trayIconHeight, Image.SCALE_SMOOTH), "RezRem", popup);
	            
	            trayIcon.addActionListener(showListener);
	            
	            try {
	                
	            	tray.add(trayIcon);
	            
	            } catch (AWTException e) {
	                
	            	System.err.println(e);
	            
	            }
	            
				
			} catch (IOException e1) {
				
				e1.printStackTrace();
			
			}
            
        }
		
    }

	public void doLogin() {
		
		final Task task = new Task();
		
		task.setTask(new Runnable() {
			
			@Override
			public void run() {
				
				task.setResult(dining.logIn());
				
			}
			
		});
		
		task.setCallBack(new CallBack() {
			
			@Override
			public void OnComplete(boolean success) {
				
				if (success) {
				
					if (config.exists())
						config.delete();
					
					try {
						
						config.createNewFile();
						
						FileWriter writer = new FileWriter(config);
						
						writer.write(EncryptUtils.encode("username=" + dining.getUserName()) + "\n");
						
						writer.write(EncryptUtils.encode("password=" + dining.getPassword()) + "\n");
						
						writer.flush();
					
						writer.close();
						
					} catch (FileNotFoundException e) {
						
						e.printStackTrace();
				
					} catch (IOException e) {
					
						e.printStackTrace();
				
					}
					
					goForReserve();
					
					loadSimpleTemplate();
					
				} else {
					
					browser.loadURL(Main.class.getResource("templates/login.html").toExternalForm());
					
					final LoadAdapter loadAdapter = new LoadAdapter() {
						
						@Override
					    public void onFinishLoadingFrame(FinishLoadingEvent event) {
							
					        if (event.isMainFrame()) {
					        	
					        	if (config.exists())
					        		config.delete();
					        	
					        	browser.removeLoadListener(this);
					        	
					        	if (dining.getUserName() != null && dining.getPassword() != null)
					        		loginLoaded();
					        
					        }
					        
					    }
						
					};
					
					browser.addLoadListener(loadAdapter);
					
				}
				
			}
			
		});
		
		new Thread(task).start();
		
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
	
	public void setNameOnPage() {
		
		browser.addLoadListener(new LoadAdapter() {
			
			@Override
		    public void onFinishLoadingFrame(FinishLoadingEvent event) {
				
				browser.executeJavaScript("$('#name').text('" + events.getFirstName() + "');");
				
				browser.removeLoadListener(this);
				
			}
			
		});
		
	}
	
	public void setReserveBoolOnPage() {
		
		browser.addLoadListener(new LoadAdapter() {
			
			@Override
		    public void onFinishLoadingFrame(FinishLoadingEvent event) {
				
				if (events.getReservedNextWeek()) {
					
					browser.executeJavaScript("$('#now-reserve a').addClass('disabled').removeClass('waves-effect')");
					
					browser.executeJavaScript("$('#reserve-done a').addClass('disabled').removeClass('waves-effect')");
					
				}
				else {
					
					browser.executeJavaScript("$('#now-reserve a').removeClass('disabled').addClass('waves-effect')");
					
					browser.executeJavaScript("$('#reserve-done a').removeClass('disabled').addClass('waves-effect')");
					
				}
				
				browser.removeLoadListener(this);
				
			}
			
		});
		
	}
	
	public void setLoadMessageOnPage() {
		
		browser.addLoadListener(new LoadAdapter() {
			
			@Override
		    public void onFinishLoadingFrame(FinishLoadingEvent event) {
				
				browser.executeJavaScript("$('#load-message').text('" + events.getLoadMessage() + "')");
				
				browser.removeLoadListener(this);
				
			}
			
		});
		
	}
	
	public void loadSimpleTemplate() {
		
		if (events.getFirstName().isEmpty()) {
			
			events.setFirstName(dining.getName());
			
		}
		
		setNameOnPage();
		
		setReserveBoolOnPage();
		
		browser.loadURL(Main.class.getResource("templates/simple.html").toExternalForm());
		
	}
	
	public Date getExpDate() {
		
		Calendar c = Calendar.getInstance();
		
		c.setTime(lastReserve);
		
		int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
		
		dayOfWeek %= 7;
		
		c.add(Calendar.DATE, 6 - dayOfWeek);
		
		return c.getTime();
		
	}
	
	public long getDayDiff(Date dt1, Date dt2) {
		
		long diffInMillies = dt1.getTime() - dt2.getTime();
		
		return TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
		
	}
	
	public void changeReserveBool(boolean newValue) {
		
		if (newValue != events.getReservedNextWeek()) {
		
			if (newValue) {
				
				browser.executeJavaScript("$('#now-reserve a').addClass('disabled').removeClass('waves-effect')");
				
				browser.executeJavaScript("$('#reserve-done a').addClass('disabled').removeClass('waves-effect')");
				
				if (lastReserve == null) {
					
					lastReserve = new Date();
					
					writeLastDate();
					
				}
					
				if (SystemTray.isSupported()) {
					
					trayIcon.displayMessage("RezRem",
		                    "Reservation has been done for the next week!",
		                    TrayIcon.MessageType.WARNING);
					
				}
				
			}
			else {
				
				browser.executeJavaScript("$('#now-reserve a').removeClass('disabled').addClass('waves-effect')");
				
				browser.executeJavaScript("$('#reserve-done a').removeClass('disabled').addClass('waves-effect')");
				
			}
		
			events.setReservedNextBool(newValue);
		
		}
		
	}
	
	public void goForReserve() {
		
		// 4 for Wednesday
		
		Timer timer = new Timer ();
		
		TimerTask hourlyTask = new TimerTask() {
			
		    @Override
		    public void run () {
		        
		    	check();
		    	
		    }
		    
		};

		// schedule the task to run starting now and then every 5 hour...
		
		timer.schedule(hourlyTask, 0l, 1000*60*60*5);
		
	}
	
	public void writeLastDate() {
		
		if (settings.exists())
			settings.delete();
		
		try {
			
			settings.createNewFile();
			
			FileWriter writer = new FileWriter(settings);
			
			writer.write(lastReserve.toString());
			
			writer.flush();
		
			writer.close();
			
		} catch (Exception e) {
			
			// handler
			
		}
		
	}
	
	public void check() {
		
		changeReserveBool(dining.nextWeekIsReserved(5));
    	
    	if (events.getReservedNextWeek()) {
    		
    		changeReserveBool(!(getDayDiff(new Date(), getExpDate()) > 0));
    		
    	}
    	else {
    		
    		Calendar c = Calendar.getInstance();
			
			int todayDayOfWeek = c.get(Calendar.DAY_OF_WEEK);
			
			if (todayDayOfWeek == 3 || todayDayOfWeek == 4 || todayDayOfWeek == 3) {
				
				reserve();
				
			}
			else if (todayDayOfWeek == 2 || todayDayOfWeek == 1 || todayDayOfWeek == 7){
				
				if (SystemTray.isSupported()) {
					
					trayIcon.displayMessage("RezRem",
		                    "Don't forget to reserve food for next week!",
		                    TrayIcon.MessageType.WARNING);
					
				}
				
			}
    		
    	}
		
	}
	
	public void reserve() {
		
		if (SystemTray.isSupported()) {
			
			trayIcon.displayMessage("RezRem",
                    "RezRem is going to reserve food for next week!",
                    TrayIcon.MessageType.INFO);
			
		}
		
		String result = dining.reserveNextWeek().trim();
		
		changeReserveBool(result.isEmpty());
		
		if (!result.isEmpty()) {
			
			if (SystemTray.isSupported()) {
				
				trayIcon.displayMessage("RezRem",
	                    result,
	                    TrayIcon.MessageType.ERROR);
				
			}
			
		}
		
		if (events.getReservedNextWeek()) {
			
			lastReserve = new Date();
			
			if (SystemTray.isSupported()) {
				
				trayIcon.displayMessage("RezRem",
	                    "Reservation is Done!",
	                    TrayIcon.MessageType.INFO);
				
			}
			
			writeLastDate();
			
		}
		
	}
	
}

interface CallBack {
	
	public void OnComplete(boolean success);
	
}

class Task implements Runnable {
	
	private CallBack callBack;
	
	private Runnable task;
	
	private final AtomicBoolean result;
	
	public Task() {
		
		result = new AtomicBoolean();
		
	}
	
	public void run() {
		
		task.run();
		
		callBack.OnComplete(result.get());
		
	}
	
	public void setTask(Runnable task) {
		
		this.task = task;
		
	}
	
	public void setCallBack(CallBack callBack) {
		
		this.callBack = callBack;
		
	}
	
	public void setResult(boolean result) {
		
		this.result.set(result);
		
	}
	
}