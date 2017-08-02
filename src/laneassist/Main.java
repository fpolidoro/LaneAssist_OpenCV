package laneassist;
	
import java.lang.reflect.Field;

//import java.lang.reflect.Field;

import org.opencv.core.Core;

import laneassist.Controller;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
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
			Scene scene = new Scene(rootElement);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			
			// create the stage with the given title and the previously created
			// scene
			primaryStage.setTitle("Lane Assist");
			primaryStage.setScene(scene);
			primaryStage.setResizable(false);
			
			// show the GUI
			primaryStage.show();
			
			Controller controller = loader.getController();
			controller.initializeGUI(primaryStage);
			
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
		String arch = System.getProperty("os.arch");
		try {
		    Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
		    fieldSysPath.setAccessible(true);
		    fieldSysPath.set(null, null);
			
			//controllo se il sistema è a 32 o 64 bit, per caricare le giuste librerie
			if(arch.contains("x86")){
				System.setProperty("java.library.path", "lib/x86");
				System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
				System.loadLibrary("opencv_ffmpeg320");
			}else{
				System.setProperty("java.library.path", "lib/x64");
				System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
				System.loadLibrary("opencv_ffmpeg320_64");
			}

		} catch (Exception e) {
		    //ex.printStackTrace();
		    Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Exception");
			alert.setHeaderText(e.getMessage());
			alert.showAndWait();
		}
		
		launch(args);
	}
}
