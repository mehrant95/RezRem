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
import com.teamdev.jxbrowser.chromium.BrowserFunction;
import com.teamdev.jxbrowser.chromium.JSValue;
import com.teamdev.jxbrowser.chromium.events.FinishLoadingEvent;
import com.teamdev.jxbrowser.chromium.events.LoadAdapter;
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
	
	private boolean firstMinimize;
	
    private TrayIcon trayIcon;
    
    private File config;
    
    private File settings;
	
    private Dining dining;
    
    private String firstName;
    
    private Date lastReserve;
    
    private boolean reservedNextWeek;
    
    private String loadingMessage;
    
	public static void main(String[] args) {
		
		launch(args);
		
	}
	
	@Override
	public void start(Stage primaryStage) {
		
		this.primaryStage = primaryStage;
		
		firstMinimize = true;
		
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
		
		initialize();
		
		dining = new Dining();
		
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
		
		registerLogin();
		
		registerLoadMessage();
		
		if(!config.exists()) {
			
			showLogin();
		    
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
				
				loadingMessage = "در حال ورود به سامانه ...";
				
				browser.loadURL(Main.class.getResource("templates/loading.html").toExternalForm());
				
				doLogin();
				
			} catch (Exception e) {
				
				config.delete();
				
				showLogin();
				
			}
			
		}
		
	}
	
	public void initialize() {
		
		initCloseButton(primaryStage);
		
		initMinimizeButton(primaryStage);
		
	}
	
	public void initCloseButton(Stage primaryStage) {
		
		browser.registerFunction("Close", new BrowserFunction() {
			
			@Override
			public JSValue invoke(JSValue... args) {
				
				Platform.runLater(new Runnable() {
					
					@Override
					public void run() {
						
						closeToTray(primaryStage);
						
					}
				});
				
				return null;
				
			}
			
		});
		
	}
	
	public void initMinimizeButton(Stage primaryStage) {
		
		browser.registerFunction("Minimize", new BrowserFunction() {
			
			@Override
			public JSValue invoke(JSValue... args) {
				
				Platform.runLater(new Runnable() {
					
					@Override
					public void run() {
						
						primaryStage.setIconified(true);
						
					}
				});
				
				return null;
				
			}
			
		});
		
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

    public void showMinimizeMessage() {
        
    	if (firstMinimize) {
            
    		trayIcon.displayMessage("RezRem",
                    "RezRem is running in background.",
                    TrayIcon.MessageType.INFO);
            
    		firstMinimize = false;
        
    	}
    	
    }

    private void closeToTray(Stage primaryStage) {
    	
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
	
	public void showLogin() {
		
		browser.loadURL(Main.class.getResource("templates/login.html").toExternalForm());
		
	}
	
	public void registerLoadMessage() {
		
		browser.registerFunction("GetLoadMessage", new BrowserFunction() {
			
			@Override
			public JSValue invoke(JSValue... args) {				
				
				return JSValue.create(loadingMessage);
				
			}
			
		});
		
	}
	
	public void registerLogin() {
		
		browser.registerFunction("Login", new BrowserFunction() {
			
			@Override
			public JSValue invoke(JSValue... args) {				
				
				if (args.length >= 2) {
					
					JSValue[] arr = args.clone();
					
					if (arr[0].getString().trim().isEmpty() || arr[1].getString().trim().isEmpty())
						return null;
					
					loadingMessage = "در حال ورود به سامانه ...";
					
					browser.loadURL(Main.class.getResource("templates/loading.html").toExternalForm());
					
					dining.setUserName(arr[0].getString());
					
					dining.setPassword(arr[1].getString());
					
					doLogin();
					
				}
				
				return null;
				
			}
			
		});
		
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
					
					registerFunctions();
					
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
	
	public void registerFunctions() {
		
		registerExit();
		
		registerGetFirstName();
		
		registerReturn();
		
		registerGoToMain();
		
		registerGetReserveBool();
		
		registerNowReserve();
		
		registerReserveDone();
		
	}
	
	public void registerReserveDone() {
		
		browser.registerFunction("ReserveDone", new BrowserFunction() {
			
			@Override
			public JSValue invoke(JSValue... args) {
				
				changeReserveBool(true);
				
				return null;
				
			}
			
		});
		
	}
	
	public void registerNowReserve() {
		
		browser.registerFunction("NowReserve", new BrowserFunction() {
			
			@Override
			public JSValue invoke(JSValue... args) {				
				
				reserve();
				
				return null;
				
			}
			
		});
		
	}
	
	public void registerReturn() {
		
		browser.registerFunction("Return", new BrowserFunction() {
			
			@Override
			public JSValue invoke(JSValue... args) {				
				
				browser.loadURL(Main.class.getResource("templates/simple.html").toExternalForm());
				
				return null;
				
			}
			
		});
		
	}
	
	public void registerExit() {
		
		browser.registerFunction("Exit", new BrowserFunction() {
			
			@Override
			public JSValue invoke(JSValue... args) {				
				
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
				
				return null;
				
			}
			
		});
		
	}
	
	public void registerGoToMain() {
		
		browser.registerFunction("GoToMain", new BrowserFunction() {
			
			@Override
			public JSValue invoke(JSValue... args) {				
				
				if (firstName == null) {
					
					firstName = dining.getName();
					
				}
				
				browser.loadURL(Main.class.getResource("templates/main.html").toExternalForm());
				
				return null;
				
			}
			
		});
		
	}
	
	public void loadSimpleTemplate() {
		
		if (firstName == null) {
			
			firstName = dining.getName();
			
		}
		
		browser.loadURL(Main.class.getResource("templates/simple.html").toExternalForm());
		
	}
	
	public void registerGetFirstName() {
		
		browser.registerFunction("GetFirstName", new BrowserFunction() {
			
			@Override
			public JSValue invoke(JSValue... args) {				
				
				if (firstName == null)
					return JSValue.create("");
				else
					return JSValue.create(firstName);
				
			}
			
		});
		
	}
	
	public Date getExpDate() {
		
		Calendar c = Calendar.getInstance();
		
		c.setTime(lastReserve);
		
		int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
		
		dayOfWeek %= 7;
		
		c.add(Calendar.DATE, 6 - dayOfWeek);
		
		return c.getTime();
		
	}
	
	public void registerGetReserveBool() {
		
		browser.registerFunction("reservedNextWeek", new BrowserFunction() {
			
			@Override
			public JSValue invoke(JSValue... args) {				
				
				return JSValue.create(reservedNextWeek);
				
			}
			
		});
		
	}
	
	public long getDayDiff(Date dt1, Date dt2) {
		
		long diffInMillies = dt1.getTime() - dt2.getTime();
		
		return TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
		
	}
	
	public void changeReserveBool(boolean newValue) {
		
		if (newValue != reservedNextWeek) {
		
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
		
			reservedNextWeek = newValue;
		
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
    	
    	if (reservedNextWeek) {
    		
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
		
		if (reservedNextWeek) {
			
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