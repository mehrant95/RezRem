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
	
    private Dining dining;
    
    private String firstName;
    
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
		
		Scene scene = new Scene(pane, 380, 500);
		
		makeDraggable(scene);
		
		primaryStage.initStyle(StageStyle.UNDECORATED);
		
		primaryStage.setScene(scene);
		
		primaryStage.show();
		
		initialize();
		
		dining = new Dining();
		
		// read user name & password file
		
		config = new File("src/RezRem/user.config");
		
		registerLogin();
		
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
	
	double xOffset, yOffset;
	boolean pressed;
	public void makeDraggable(Scene pane) {
		
		primaryStage.setX(600);
		
//		pane.setOnMousePressed(new EventHandler<MouseEvent>() {
//            public void handle(MouseEvent event) {
//            	pressed = true;
//            	System.out.println("salam");
//                xOffset = primaryStage.getX() - event.getScreenX();
//                yOffset = primaryStage.getY() - event.getScreenY();
//            }
//        });
		
//		primaryStage.addEventFilter(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
//		    @Override
//		    public void handle(MouseEvent mouseEvent) {
//		        System.out.println("mouse click detected! " + mouseEvent.getSource());
//		    }
//		});
		
//		pane.setOnMouseClicked(new EventHandler<MouseEvent>() {
//			@Override
//            public void handle(MouseEvent event) {
//                System.out.println("fuck");
//				primaryStage.setX(event.getScreenX() + 10);
//                primaryStage.setY(event.getScreenY() + 25);
//            }
//        });

		
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
	
	public void registerLogin() {
		
		browser.registerFunction("Login", new BrowserFunction() {
			
			@Override
			public JSValue invoke(JSValue... args) {				
				
				if (args.length >= 2) {
					
					JSValue[] arr = args.clone();
					
					if (arr[0].getString().trim().isEmpty() || arr[1].getString().trim().isEmpty())
						return null;
					
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
					
					loadMainTemplate();
					
					registerFunctions();
					
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
	
	public void loadMainTemplate() {
		
		if (firstName == null) {
			
			firstName = dining.getName();
			
		}
		
		browser.loadURL(Main.class.getResource("templates/main.html").toExternalForm());
		
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