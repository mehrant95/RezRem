package RezRem;

import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.javafx.BrowserView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {
	
   @Override
   public void start(Stage primaryStage) {
	   
       Browser browser = new Browser();
       
       BrowserView browserView = new BrowserView(browser);
       
       StackPane pane = new StackPane();
       
       pane.getChildren().add(browserView);
       
       Scene scene = new Scene(pane, 380, 500);
       
       primaryStage.initStyle(StageStyle.UNDECORATED);
       
       primaryStage.setScene(scene);
       
       primaryStage.show();
       
       browser.loadURL(Main.class.getResource("templates/login.html").toExternalForm());
       
   }
   
   public static void main(String[] args) {
       
	   launch(args);
   
   }
   
}