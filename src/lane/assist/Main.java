package lane.assist;
	
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try
		{
			// load the FXML resource
			FXMLLoader loader = new FXMLLoader(getClass().getResource("GUI.fxml"));
			Controller controller = new Controller();
			loader.setController(controller);
			// store the root element so that the controllers can use it
			BorderPane rootPane = (BorderPane) loader.load();
			// create and style a scene
			Scene scene = new Scene(rootPane, 800, 600);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			// create the stage with the given title and the previously created
			// scene
			primaryStage.setTitle("Lane Assist");
			primaryStage.setScene(scene);
			
			// show the GUI
			primaryStage.show();
			controller.InitializeGUI();
		}
		catch (Exception e)
		{
			e.printStackTrace();
}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
