package laneassist;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import laneassist.utils.Utils;

public class Controller {
	@FXML
	private ImageView currentFrame;
	@FXML
	private Label lblVideoTitle;
	@FXML
	private Slider sliROIWidth;
	@FXML
	private Slider sliROIHeight;
	@FXML
	private Slider sliROIHorizontalPosition;
	@FXML
	private Slider sliROIVerticalPosition;
	@FXML
	private Slider sliVideoSpeed;
	@FXML
	private StackPane stackpROI;
	@FXML
	private Rectangle rectROI;
	@FXML
	private BorderPane borderPaneROI;
	@FXML
	private BorderPane borderPaneSpeed;

	private final Double ROI_WIDTH = 80.0;
	private final Double ROI_HEIGHT = 35.0;

	private Insets spROIPadding;
	private Double hPaddingMax;
	private Double vPaddingMax;
	private Double curROIPaneWidth;
	private Double curROIPaneHeight;
	private Double frameWidth;
	private Double frameHeight;

	private ScheduledExecutorService timer;
	private ScheduledFuture<?> future;
	private VideoCapture capture = new VideoCapture();
	
	//punti che delineano la ROI che verrà impressa sul frame
	private Point leftTopPointROI;
	private Point rightBottomPointROI;
	
	Runnable frameGrabber;
	
	private Rect roi;
	
	/**
	 * Inizializza i comandi della GUI
	 */
	protected void initializeGUI() {
		leftTopPointROI = new Point();
		rightBottomPointROI = new Point();
		roi = new Rect();
		setGUIDisabled(true);	
	}

	/**
	 * Azione per il menu "open"
	 */
	@FXML
	private void actionMenuOpen() {

		String userDir = System.getProperty("user.home");
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File(userDir + "/Videos"));
		fileChooser.setTitle("Open File");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Video Files", "*.avi", "*.mp4", ".mkv", ".mov"));
		File video = fileChooser.showOpenDialog(null);

		if (capture.isOpened()) {
			setClosed();
		}
		
		//carico il video
		capture.open(video.getAbsolutePath());

		if (capture.isOpened()) {
			lblVideoTitle.setText(video.getName());
			//ottengo le dimensioni del frame
			frameWidth = capture.get(Videoio.CAP_PROP_FRAME_WIDTH);
			frameHeight = capture.get(Videoio.CAP_PROP_FRAME_HEIGHT);
			setGUIDisabled(false);
			computeROIDimension();
			computePointsForROI();
			frameGrabber = new Runnable() {

				@Override
				public void run() {
					Mat frame = grabFrame();
					Imgproc.rectangle(frame, leftTopPointROI, rightBottomPointROI, new Scalar(0,0,255), 3);
					Image imageToShow = Utils.mat2Image(frame);
					updateImageView(currentFrame, imageToShow);
				}
			};

			timer = Executors.newSingleThreadScheduledExecutor();
			future = timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

		} else {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");
			alert.setHeaderText("The video cannot be opened");
			alert.showAndWait();
		}
	}
	
	@FXML
	private void actionMenuClose() {
		setClosed();
	}

	/**
	 * Comando slider che aumenta/diminuisce la larghezza della Region of Interest (rettangolo azzurrino).
	 */
	@FXML
	private void dragROIWidth() {
		Double width = ROI_WIDTH + (sliROIWidth.getValue() * (stackpROI.getWidth() - ROI_WIDTH)) / 100;
		if (width >= curROIPaneWidth) {
			width = curROIPaneWidth;
			sliROIHorizontalPosition.setDisable(true);
		} else {
			sliROIHorizontalPosition.setDisable(false);
		}
		rectROI.setWidth(width);

		hPaddingMax = stackpROI.getWidth() - rectROI.getWidth();

		spROIPadding = stackpROI.getPadding();
		Double padding = sliROIHorizontalPosition.getValue() * hPaddingMax / 100;
		stackpROI.setPadding(
				new Insets(spROIPadding.getTop(), spROIPadding.getRight(), spROIPadding.getBottom(), padding));
		computePointsForROI();
	}

	/**
	 * Comando slider che aumenta/diminuisce l'altezza della Region of Interest (rettangolo azzurrino).
	 */
	@FXML
	private void dragROIHeight() {
		Double height = ROI_HEIGHT + (sliROIHeight.getValue() * (stackpROI.getHeight() - ROI_HEIGHT)) / 100;
		if (height >= curROIPaneHeight) {
			height = curROIPaneHeight;
			sliROIVerticalPosition.setDisable(true);
		} else
			sliROIVerticalPosition.setDisable(false);
		rectROI.setHeight(height);

		vPaddingMax = stackpROI.getHeight() - rectROI.getHeight();

		spROIPadding = stackpROI.getPadding();
		Double padding = sliROIVerticalPosition.getValue() * vPaddingMax / 100;
		stackpROI.setPadding(
				new Insets(spROIPadding.getTop(), spROIPadding.getRight(), padding, spROIPadding.getLeft()));
		computePointsForROI();
	}

	/**
	 * Comando slider che sposta la Region of Interest (rettangolo azzurrino) orizzontalmente all'interno del frame (contorno nero).
	 */
	@FXML
	private void dragROIHorizontalPosition() {
		Double padding = sliROIHorizontalPosition.getValue() * hPaddingMax / 100;
		spROIPadding = stackpROI.getPadding();
		stackpROI.setPadding(
				new Insets(spROIPadding.getTop(), spROIPadding.getRight(), spROIPadding.getBottom(), padding));
		computePointsForROI();
	}

	/**
	 * Comando slider che sposta la Region of Interest (rettangolo azzurrino) verticalmente all'interno del frame (contorno nero).
	 */
	@FXML
	private void dragROIVerticalPosition() {
		Double padding = sliROIVerticalPosition.getValue() * vPaddingMax / 100;
		spROIPadding = stackpROI.getPadding();
		stackpROI.setPadding(
				new Insets(spROIPadding.getTop(), spROIPadding.getRight(), padding, spROIPadding.getLeft()));
		computePointsForROI();
	}

	/**
	 * Comando slider per aumentare/diminuire la velocità del video.
	 */
	@FXML
	private void dragVideoSpeed() {
		if(!future.isCancelled()) {
			future.cancel(false);
			future = timer.scheduleAtFixedRate(frameGrabber, 0, (long) (33 / sliVideoSpeed.getValue()), TimeUnit.MILLISECONDS);
		}
	}
	

	protected void setClosed() {
		if (this.timer != null && !this.timer.isShutdown()) {
			try {
				this.timer.shutdown();
				this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error while trying to close the video");
				alert.setHeaderText(e.getMessage());
				alert.showAndWait();
			}
		}
		if (capture != null && capture.isOpened()) {
			capture.release();
			updateImageView(currentFrame, null);
		}
		
	}
	
	/**
	 * Per disegnare il rettangolo contenente la rappresentazione della ROI, in modo che mantenga lo stesso aspect ratio del video
	 * @param mat
	 */
	private void computeROIDimension(){
		int dim = (int)Math.round(stackpROI.getWidth()*frameWidth/frameHeight);
		stackpROI.setPrefHeight(dim);
		System.out.println("dim: " + dim);
	}
		
	//per abilitare/disabilitare i controlli della GUI relativi al video
	protected void setGUIDisabled(boolean value){
		borderPaneSpeed.setDisable(value);
		borderPaneROI.setDisable(value);
		if(value){ //schiarisco i colori del rettangolo ROI
			rectROI.setVisible(false);
			stackpROI.setStyle("-fx-border-color: lightgrey ;");
		}else{	//attivo il rettangolo azzurro e lo posiziono in base ai cursori
			hPaddingMax = stackpROI.getWidth() - rectROI.getWidth();
			vPaddingMax = stackpROI.getHeight() - rectROI.getHeight();
			Double padding = hPaddingMax / 2;
			spROIPadding = stackpROI.getPadding();
			stackpROI.setPadding(new Insets(spROIPadding.getTop(), spROIPadding.getRight(), 0.0, padding));
			curROIPaneWidth = stackpROI.getWidth();
			curROIPaneHeight = stackpROI.getHeight();
			System.out.println("padding: " + padding);
			rectROI.setVisible(true);
			stackpROI.setStyle("-fx-border-color: grey ;");
		}
	}
	
	/**
	 * Disegna la ROI sul frame corrente
	 */
	private void computePointsForROI(){
		int x = (int)Math.round(stackpROI.getPadding().getLeft()*frameWidth/stackpROI.getWidth());
		int y = (int)Math.round(frameHeight - (stackpROI.getPadding().getBottom()*frameHeight/stackpROI.getHeight()));
		leftTopPointROI.x = x;
		leftTopPointROI.y = y - Math.round(frameHeight*rectROI.getHeight()/stackpROI.getHeight());
		rightBottomPointROI.x = x + Math.round(frameWidth*rectROI.getWidth()/stackpROI.getWidth());
		rightBottomPointROI.y = y;
		//il rettangolo viene disegnato a partire da top-left del frame, che è 0,0
		//Imgproc.rectangle(frame, leftTopPointROI, rightBottomPointROI, new Scalar(0,0,255), 3);
		roi = new Rect(leftTopPointROI, rightBottomPointROI);
	}
	
	/**
	 * Ottiene il frame corrente
	 * @return Mat contenente il frame corrente
	 */
	private Mat grabFrame() {

		Mat frame = new Mat();

		if (this.capture.isOpened()) {
			try {
				this.capture.read(frame);

				if (!frame.empty()) {

					// ottiene la ROI a partire dal rettangolo Rect roi
					Mat imageROI = frame.submat(roi);
					//da qua in poi penso ci serva il canny e la hough transform
				}

			} catch (Exception e) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error while processing the image");
				alert.setHeaderText(e.getMessage());
				alert.showAndWait();
			}
		}

		return frame;
	}	

	/**
	 * Aggiorna l'immagine mostrata a video
	 * @param view elemento FXML in cui l'immagine deve essere mostrata
	 * @param image frame da mostrare
	 */
	private void updateImageView(ImageView view, Image image) {
		Utils.onFXThread(view.imageProperty(), image);
	}	

}