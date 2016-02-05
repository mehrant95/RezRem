package RezRem;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.BrowserFunction;
import com.teamdev.jxbrowser.chromium.JSValue;
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
		
		primaryStage.initStyle(StageStyle.UNDECORATED);
		
		primaryStage.setScene(scene);
		
		primaryStage.show();
		
		initialize();
		
		// read user name & password file
		
		config = new File("src/RezRem/user.config");
		
		if(!config.exists()) {
			
		    try {
		    	
				config.createNewFile();
				
				showLogin();
				
				
			} catch (IOException e) {
				
				e.printStackTrace();
			
			}
		    
		}
		else if (!config.isDirectory()) {
			
			// login to dining
			
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
		
		browser.registerFunction("Login", new BrowserFunction() {
			
			@Override
			public JSValue invoke(JSValue... args) {
				
				for (JSValue arg : args) {
					
					System.out.println("arg = " + arg.getString());
					
					try {
						
						FileOutputStream oFile = new FileOutputStream(config, false);
						
						FileWriter writer = new FileWriter(config);
						
						writer.write("salam");
						
						writer.flush();
						
						writer.close();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
					
				}
				
				return JSValue.create("Hello!");
				
			}
			
		});
		
	}
	
}