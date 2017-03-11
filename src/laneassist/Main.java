package laneassist;
	
import org.opencv.core.Core;

import laneassist.Controller;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage)
	{
		try
		{
			// load the FXML resource
			FXMLLoader loader = new FXMLLoader(getClass().getResource("GUI.fxml"));
			
			// store the root element so that the controllers can use it
			BorderPane rootElement = (BorderPane) loader.load();
			
			// create and style a scene
			Scene scene = new Scene(rootElement, 800, 600);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			
			// create the stage with the given title and the previously created
			// scene
			primaryStage.setTitle("Lane Assist");
			primaryStage.setScene(scene);
			
			// show the GUI
			primaryStage.show();
			
			Controller controller = loader.getController();
			controller.InitializeGUI();
			
			// set the proper behavior on closing the application
			primaryStage.setOnCloseRequest((new EventHandler<WindowEvent>() {
				public void handle(WindowEvent we)
				{
					controller.setClosed();
				}
			}));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		// load the native OpenCV library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		launch(args);
	}
}
