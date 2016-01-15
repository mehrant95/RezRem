package RezRem;

import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.BrowserFunction;
import com.teamdev.jxbrowser.chromium.JSValue;
import com.teamdev.jxbrowser.chromium.javafx.BrowserView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.application.Platform;

public class Main extends Application {
	
	private Stage primaryStage;
	
	private Browser browser;
	
	public static void main(String[] args) {
		
		launch(args);
		
	}
	
	@Override
	public void start(Stage primaryStage) {
		
		this.primaryStage = primaryStage;
		
		browser = new Browser();
		
		BrowserView browserView = new BrowserView(browser);
		
		StackPane pane = new StackPane();
		
		pane.getChildren().add(browserView);
		
		Scene scene = new Scene(pane, 380, 500);
		
		primaryStage.initStyle(StageStyle.UNDECORATED);
		
		primaryStage.setScene(scene);
		
		primaryStage.show();
		
		initialize();
		
		// if not logged in
		
		showLogin();
		
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
						
						primaryStage.fireEvent(
							    new WindowEvent(
							        primaryStage,
							        WindowEvent.WINDOW_CLOSE_REQUEST
							    )
							);	
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
	
	public void showLogin() {
		
		browser.loadURL(Main.class.getResource("templates/login.html").toExternalForm());
		
		browser.registerFunction("Login", new BrowserFunction() {
			
			@Override
			public JSValue invoke(JSValue... args) {
				
				
				
				for (JSValue arg : args) {
					
					System.out.println("arg = " + arg.getString());
					
				}
				
				return JSValue.create("Hello!");
				
			}
			
		});
		
	}
	
}