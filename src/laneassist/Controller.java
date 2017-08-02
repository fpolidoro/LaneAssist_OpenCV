package laneassist;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import laneassist.utils.Utils;

public class Controller {
	@FXML
	private CheckMenuItem menuItemShowDebug;
	@FXML
	private ImageView currentFrame;
	@FXML
	private Label lblFileName;
	@FXML
	private Label lblTime;
	@FXML
	private Label lblFrame;
	@FXML
	private Slider sliROIWidth;
	@FXML
	private Slider sliROIHeight;
	@FXML
	private Slider sliROIHorizontalPosition;
	@FXML
	private Slider sliROIVerticalPosition;
	@FXML
	private StackPane stackpROI;
	@FXML
	private ImageView imgMoveROIUpDown;
	@FXML
	private ImageView imgMoveROILeftRight;
	@FXML
	private ImageView imgResizeROIHorizontally;
	@FXML
	private ImageView imgResizeROIVertically;
	@FXML
	private Rectangle rectROI;
	@FXML
	private SplitPane splitPaneDebug;
	@FXML
	private AnchorPane paneControls;
	@FXML
	private Button btnPlayPause;
	@FXML
	private ImageView imgPlayPause;
	@FXML
	private SplitPane mainSplitPane;
	@FXML
	private Label lblSpeed;
	
	//Debug	
	@FXML
	private ScrollPane debugScrollPane;
	
	@FXML
	private Slider sliCannyThreshold;
	@FXML
	private TextField txtCannyThreshold;
	@FXML
	private Slider sliCannyRatio;
	@FXML
	private TextField txtCannyRatio;
	@FXML
	private Slider sliBlur;
	@FXML
	private TextField txtBlur;
	@FXML
	private ComboBox<Integer> cmbApertureSize;
	
	@FXML
	private Slider sliHoughRho;
	@FXML
	private TextField txtHoughRho;
	@FXML
	private Slider sliHoughTheta;
	@FXML
	private TextField txtHoughTheta;
	@FXML
	private Slider sliHoughThreshold;
	@FXML
	private TextField txtHoughThreshold;
	@FXML
	private Slider sliMinLenght;
	@FXML
	private TextField txtMinLenght;
	@FXML
	private Slider sliMaxGap;
	@FXML
	private TextField txtMaxGap;
	
	@FXML
	private CheckBox chkBorders;
	@FXML
	private CheckBox chkDetectedSegments;
	@FXML
	private CheckBox chkChosenSegments;
	@FXML
	private CheckBox chkStripes;
	@FXML
	private CheckBox chkLane;
	@FXML
	private CheckBox chkCenter;
	
	private Stage stage;
	private Node debugPane;
	
	private Image playImg;
	private Image pauseImg;

	private final Double ROI_WIDTH = 120.0;
	private final Double ROI_HEIGHT = 35.0;
	
	private final int DEFAULT_BLUR = 3;
	private final int DEFAULT_CANNY_THRESHOLD = 27;
	private final int DEFAULT_CANNY_RATIO = 3;
	private final int DEFAULT_APERTURE_SIZE = 3;
	
	private final int DEFAULT_HOUGH_RHO = 1;
	private final int DEFAULT_HOUGH_THETA = 180;
	private final int DEFAULT_HOUGH_THRESHOLD = 30;
	private final int DEFAULT_MIN_LENGHT = 30;
	private final int DEFAULT_MAX_GAP = 5;

	private Insets spROIPadding;
	private Double hPaddingMax;
	private Double vPaddingMax;
	private Double curROIPaneWidth;
	private Double curROIPaneHeight;
	private Double frameWidth;
	private Double frameHeight;
	private Double frameCount;

	private ScheduledExecutorService timer;
	private ScheduledFuture<?> future;
	private VideoCapture capture = new VideoCapture();
	private int speedMultiplier = 1;

	private Point leftTopPointROI;
	private Point rightBottomPointROI;

	private Runnable frameGrabber;

	private Rect roi;
	
	private Point[] lastLeftStripe;
	private Point[] lastRightStripe;
	private double lastAlpha;
	private int laneFrameCount;
	private boolean fillLane;
	
	private int frameCounter;
	

	/**
	 * Inizializza i comandi della GUI
	 */
	protected void initializeGUI(Stage stage) {
		this.stage = stage;
		leftTopPointROI = new Point();
		rightBottomPointROI = new Point();
		roi = new Rect();
		setGUIDisabled(true);
		
		//Debug
		debugPane = mainSplitPane.getItems().get(1); 
		
		txtBlur.setText(String.valueOf(DEFAULT_BLUR));
		sliBlur.setValue(DEFAULT_BLUR);
		txtCannyThreshold.setText(String.valueOf(DEFAULT_CANNY_THRESHOLD));
		sliCannyThreshold.setValue(DEFAULT_CANNY_THRESHOLD);
		txtCannyRatio.setText(String.valueOf(DEFAULT_CANNY_RATIO));
		sliCannyRatio.setValue(DEFAULT_CANNY_RATIO);
		cmbApertureSize.getItems().addAll(3,5,7);	
		cmbApertureSize.setValue(DEFAULT_APERTURE_SIZE);
		
		txtHoughRho.setText(String.valueOf(DEFAULT_HOUGH_RHO));
		sliHoughRho.setValue(DEFAULT_HOUGH_RHO);
		txtHoughTheta.setText(String.valueOf(DEFAULT_HOUGH_THETA));
		sliHoughTheta.setValue(DEFAULT_HOUGH_THETA);	
		txtHoughThreshold.setText(String.valueOf(DEFAULT_HOUGH_THRESHOLD));
		sliHoughThreshold.setValue(DEFAULT_HOUGH_THRESHOLD);
		txtMinLenght.setText(String.valueOf(DEFAULT_MIN_LENGHT));
		sliMinLenght.setValue(DEFAULT_MIN_LENGHT);
		txtMaxGap.setText(String.valueOf(DEFAULT_MAX_GAP));
		sliMaxGap.setValue(DEFAULT_MAX_GAP);
		
		try {
			playImg = new Image("file:icons/play32x32.png");
			pauseImg = new Image("file:icons/pause32x32.png");
			imgPlayPause.setImage(playImg);
			
		} catch (IllegalArgumentException iae) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Illegal Argument Exception");
			alert.setHeaderText(iae.getMessage());
			alert.showAndWait();
		}
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

		if (video != null) {
			if (capture.isOpened()) {
				setClosed();
			}

			capture.open(video.getAbsolutePath());

			if (capture.isOpened()) {
				lblFileName.setText(video.getName());
				frameWidth = capture.get(Videoio.CAP_PROP_FRAME_WIDTH);
				frameHeight = capture.get(Videoio.CAP_PROP_FRAME_HEIGHT);
				frameCount = capture.get(Videoio.CAP_PROP_FRAME_COUNT);
				System.out.println("frameCount = " + frameCount);
				setGUIDisabled(false);
				computeROIDimension();
				computePointsForROI();
				
				lastLeftStripe = new Point[2];
				lastRightStripe = new Point[2];
				lastLeftStripe[0] = new Point(0, roi.height);
				lastLeftStripe[1] = new Point(0, 0);
				lastRightStripe[0] = new Point(roi.width, 0);
				lastRightStripe[1] = new Point(roi.width, roi.height);
				lastAlpha = Math.toRadians(90);
				laneFrameCount = 0;
				fillLane = false;
				
				frameCounter = 0;
				
				frameGrabber = new Runnable() {

					@Override
					public void run() {
						Mat frame = grabFrame();
						if (frame != null) {
							
							showInfo();					
							Imgproc.rectangle(frame, leftTopPointROI, rightBottomPointROI, new Scalar(0, 0, 255), 3);
							Image imageToShow = Utils.mat2Image(frame);
							Utils.onFXThread(currentFrame.imageProperty(), imageToShow);
						} else {
							setClosed();
						}
					}
				};

				timer = Executors.newSingleThreadScheduledExecutor();
				future = timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
				imgPlayPause.setImage(pauseImg);

			} else {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");
				alert.setHeaderText("The video cannot be opened");
				alert.showAndWait();
			}
		}
	}

	@FXML
	private void actionMenuClose() {
		setClosed();
	}

	@FXML
	private void checkMenuItemDebug() {
		if (menuItemShowDebug.isSelected()) {
			mainSplitPane.getItems().add(1, debugPane); 
			stage.setWidth(stage.getWidth() + 243);
			
		} else {
			mainSplitPane.getItems().remove(debugPane); 
			stage.setWidth(stage.getWidth() - 243);
		}
	}

	/**
	 * Comando slider che aumenta/diminuisce la larghezza della Region of
	 * Interest (rettangolo azzurrino).
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
		imgResizeROIHorizontally.setVisible(false);
		rectROI.setVisible(true);
		rectROI.setWidth(width);

		hPaddingMax = stackpROI.getWidth() - rectROI.getWidth();

		spROIPadding = stackpROI.getPadding();
		Double padding = sliROIHorizontalPosition.getValue() * hPaddingMax / 100;
		stackpROI.setPadding(
				new Insets(spROIPadding.getTop(), spROIPadding.getRight(), spROIPadding.getBottom(), padding));
		computePointsForROI();
	}

	@FXML
	private void mouseEnteredResizeROIWidth() {
		imgResizeROIHorizontally.setVisible(true);
		rectROI.setVisible(false);
	}

	@FXML
	private void mouseExitedResizeROIWidth() {
		imgResizeROIHorizontally.setVisible(false);
		rectROI.setVisible(true);
	}

	/**
	 * Comando slider che aumenta/diminuisce l'altezza della Region of Interest
	 * (rettangolo azzurrino).
	 */
	@FXML
	private void dragROIHeight() {
		Double height = ROI_HEIGHT + (sliROIHeight.getValue() * (stackpROI.getHeight() - ROI_HEIGHT)) / 100;
		if (height >= curROIPaneHeight) {
			height = curROIPaneHeight;
			sliROIVerticalPosition.setDisable(true);
		} else
			sliROIVerticalPosition.setDisable(false);
		imgResizeROIVertically.setVisible(false);
		rectROI.setVisible(true);
		rectROI.setHeight(height);

		vPaddingMax = stackpROI.getHeight() - rectROI.getHeight();

		spROIPadding = stackpROI.getPadding();
		Double padding = sliROIVerticalPosition.getValue() * vPaddingMax / 100;
		stackpROI.setPadding(
				new Insets(spROIPadding.getTop(), spROIPadding.getRight(), padding, spROIPadding.getLeft()));
		computePointsForROI();
	}

	@FXML
	private void mouseEnteredResizeROIHeight() {
		imgResizeROIVertically.setVisible(true);
		rectROI.setVisible(false);
	}

	@FXML
	private void mouseExitedResizeROIHeight() {
		imgResizeROIVertically.setVisible(false);
		rectROI.setVisible(true);
	}

	/**
	 * Comando slider che sposta la Region of Interest (rettangolo azzurrino)
	 * orizzontalmente all'interno del frame (contorno nero).
	 */
	@FXML
	private void dragROIHorizontalPosition() {
		Double padding = sliROIHorizontalPosition.getValue() * hPaddingMax / 100;
		rectROI.setVisible(true);
		imgMoveROILeftRight.setVisible(false);
		spROIPadding = stackpROI.getPadding();
		stackpROI.setPadding(
				new Insets(spROIPadding.getTop(), spROIPadding.getRight(), spROIPadding.getBottom(), padding));
		computePointsForROI();
	}

	@FXML
	private void mouseEnteredMoveROILeftRight() {
		imgMoveROILeftRight.setVisible(true);
		rectROI.setVisible(false);
	}

	@FXML
	private void mouseExitedMoveROILeftRight() {
		imgMoveROILeftRight.setVisible(false);
		rectROI.setVisible(true);
	}

	/**
	 * Comando slider che sposta la Region of Interest (rettangolo azzurrino)
	 * verticalmente all'interno del frame (contorno nero).
	 */
	@FXML
	private void dragROIVerticalPosition() {
		Double padding = sliROIVerticalPosition.getValue() * vPaddingMax / 100;
		spROIPadding = stackpROI.getPadding();
		imgMoveROIUpDown.setVisible(false);
		rectROI.setVisible(true);
		stackpROI.setPadding(
				new Insets(spROIPadding.getTop(), spROIPadding.getRight(), padding, spROIPadding.getLeft()));
		computePointsForROI();
	}

	@FXML
	private void mouseEnteredMoveROIUpDown() {
		// System.out.println("siamo in mouseEnteredROIUpDown");
		imgMoveROIUpDown.setVisible(true);
		rectROI.setVisible(false);
	}

	@FXML
	private void mouseExitedMoveROIUpDown() {
		// System.err.println("siamo in mouseExitedROIUpDown");
		imgMoveROIUpDown.setVisible(false);
		rectROI.setVisible(true);
	}
	
	/**
	 * Comando bottone rewind
	 */
	@FXML
	private void setRewind() {

	}
	
	/**
	 * Comando bottone stop
	 */
	@FXML
	private void setStop() {

	}

	/**
	 * Comando bottone play/pause
	 */
	@FXML
	private void setPlayPause() {
		if (imgPlayPause.getImage().equals(playImg)) {
			imgPlayPause.setImage(pauseImg);
			future = timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
			
		} else {		
			speedMultiplier = 1;
			imgPlayPause.setImage(playImg);
			future.cancel(false);
			lblSpeed.setVisible(false);	
		}
	}

	/**
	 * Comando bottone next frame
	 */
	@FXML
	private void setNextFrame() {
		if (imgPlayPause.getImage().equals(pauseImg)) {
			speedMultiplier = 1;
			imgPlayPause.setImage(playImg);
			future.cancel(false);
			lblSpeed.setVisible(false);	
		}
		
		Mat frame = grabFrame();
		if (frame != null) {
			showInfo();
			Imgproc.rectangle(frame, leftTopPointROI, rightBottomPointROI, new Scalar(0, 0, 255), 3);
			Image imageToShow = Utils.mat2Image(frame);
			Utils.onFXThread(currentFrame.imageProperty(), imageToShow);
		} else {
			setClosed();
		}
	}
	
	/**
	 * Comando bottone per aumentare la velocità del video.
	 */
	@FXML
	private void setFastForward() {		
		if (imgPlayPause.getImage().equals(playImg)) {
			imgPlayPause.setImage(pauseImg);				
		} 		
		if (speedMultiplier < 32) {
			speedMultiplier *= 2;
			lblSpeed.setVisible(true);
			lblSpeed.setText(speedMultiplier + "x");
		}
		future.cancel(false);
		future = timer.scheduleAtFixedRate(frameGrabber, 0, (long) (33 / speedMultiplier) , TimeUnit.MILLISECONDS);
	}
	
	//Debug
	@FXML
	private void dragCannyThreshold() {
		int value = (int) sliCannyThreshold.getValue();
		txtCannyThreshold.setText(String.valueOf(value));
	}
	
	@FXML
	private void setCannyThreshold() {
		String text = txtCannyThreshold.getText();
		if (text.matches("\\d*")) {								
			sliCannyThreshold.setValue(Integer.valueOf(text));
		}
		txtCannyThreshold.setText(String.valueOf((int) sliCannyThreshold.getValue()));		
	}
	
	@FXML
	private void dragCannyRatio() {
		int value = (int) sliCannyRatio.getValue();
		txtCannyRatio.setText(String.valueOf(value));
	}
	
	@FXML
	private void setCannyRatio() {
		String text = txtCannyRatio.getText();
		if (text.matches("\\d*|\\d*\\.\\d")) {								
			sliCannyRatio.setValue(Integer.valueOf(text));
		}
		txtCannyRatio.setText(String.valueOf((int) sliCannyRatio.getValue()));		
	}
	
	@FXML
	private void dragBlur() {
		int value = (int) sliBlur.getValue();
		txtBlur.setText(String.valueOf(value));
	}
	
	@FXML
	private void setBlur() {
		String text = txtBlur.getText();
		if (text.matches("\\d*|\\d*\\.\\d")) {								
			sliBlur.setValue(Integer.valueOf(text));
		}
		txtBlur.setText(String.valueOf((int) sliBlur.getValue()));		
	}
	
	@FXML
	private void dragHoughRho() {
		int value = (int) sliHoughRho.getValue();
		txtHoughRho.setText(String.valueOf(value));
	}
	
	@FXML
	private void setHoughRho() {
		String text = txtHoughRho.getText();
		if (text.matches("\\d*|\\d*\\.\\d")) {								
			sliHoughRho.setValue(Integer.valueOf(text));
		}
		txtHoughRho.setText(String.valueOf((int) sliHoughRho.getValue()));		
	}
	
	@FXML
	private void dragHoughTheta() {
		int value = (int) sliHoughTheta.getValue();
		txtHoughTheta.setText(String.valueOf(value));
	}
	
	@FXML
	private void setHoughTheta() {
		String text = txtHoughTheta.getText();
		if (text.matches("\\d*|\\d*\\.\\d")) {								
			sliHoughTheta.setValue(Integer.valueOf(text));
		}
		txtHoughTheta.setText(String.valueOf((int) sliHoughTheta.getValue()));		
	}
	
	@FXML
	private void dragHoughThreshold() {
		int value = (int) sliHoughThreshold.getValue();
		txtHoughThreshold.setText(String.valueOf(value));
	}
	
	@FXML
	private void setHoughThreshold() {
		String text = txtHoughThreshold.getText();
		if (text.matches("\\d*|\\d*\\.\\d")) {								
			sliHoughThreshold.setValue(Integer.valueOf(text));
		}
		txtHoughThreshold.setText(String.valueOf((int) sliHoughThreshold.getValue()));		
	}
	
	@FXML
	private void dragMinLenght() {
		int value = (int) sliMinLenght.getValue();
		txtMinLenght.setText(String.valueOf(value));
	}
	
	@FXML
	private void setMinLenght() {
		String text = txtMinLenght.getText();
		if (text.matches("\\d*|\\d*\\.\\d")) {								
			sliMinLenght.setValue(Integer.valueOf(text));
		}
		txtMinLenght.setText(String.valueOf((int) sliMinLenght.getValue()));		
	}
	
	@FXML
	private void dragMaxGap() {
		int value = (int) sliMaxGap.getValue();
		txtMaxGap.setText(String.valueOf(value));
	}
	
	@FXML
	private void setMaxGap() {
		String text = txtMaxGap.getText();
		if (text.matches("\\d*|\\d*\\.\\d")) {								
			sliMaxGap.setValue(Integer.valueOf(text));
		}
		txtMaxGap.setText(String.valueOf((int) sliMaxGap.getValue()));		
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
			Utils.onFXThread(currentFrame.imageProperty(), null);
		}

	}

	/**
	 * Per disegnare il rettangolo contenente la rappresentazione della ROI, in
	 * modo che mantenga lo stesso aspect ratio del video
	 * 
	 * @param mat
	 */
	private void computeROIDimension() {
		int dim = (int) Math.round(stackpROI.getWidth() * frameHeight / frameWidth);
		stackpROI.setPrefHeight(dim);
		stackpROI.setMaxHeight(dim);
		System.out.println("dim: " + stackpROI.getWidth() + " * " + frameHeight + " / " + frameWidth + " = " + dim);
	}

	// per abilitare/disabilitare i controlli della GUI relativi al video
	protected void setGUIDisabled(boolean value) {
		paneControls.setDisable(value);
		splitPaneDebug.setDisable(value);
		if (value) { // schiarisco i colori del rettangolo ROI
			rectROI.setVisible(false);
			stackpROI.setStyle("-fx-border-color: lightgrey ;");
		} else { // attivo il rettangolo azzurro e lo posiziono in base ai
					// cursori
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
	private void computePointsForROI() {
		int x = (int) Math.round(stackpROI.getPadding().getLeft() * frameWidth / stackpROI.getWidth());
		int y = (int) Math
				.round(frameHeight - (stackpROI.getPadding().getBottom() * frameHeight / stackpROI.getHeight()));
		leftTopPointROI.x = x;
		leftTopPointROI.y = y - Math.round(frameHeight * rectROI.getHeight() / stackpROI.getHeight());
		rightBottomPointROI.x = x + Math.round(frameWidth * rectROI.getWidth() / stackpROI.getWidth());
		rightBottomPointROI.y = y;
		// il rettangolo viene disegnato a partire da top-left del frame, che è
		// 0,0
		// Imgproc.rectangle(frame, leftTopPointROI, rightBottomPointROI, new
		// Scalar(0,0,255), 3);
		roi = new Rect(leftTopPointROI, rightBottomPointROI);
	}

	private void showInfo() {
		int millis = ++frameCounter * 33;
		int seconds = millis / 1000;
		int minutes = (seconds / 60) % 60;
		seconds %= 60;
		millis %= 1000;
		String time = String.format("%02d", minutes) + ":" + String.format("%02d", seconds) + ":" + String.format("%04d", millis);
		
		Platform.runLater(() -> {
			lblFrame.setText(Integer.toString(frameCounter));
			lblTime.setText(time);
		});
	}
	
	/**
	 * Ottiene il frame corrente
	 * 
	 * @return Mat contenente il frame corrente
	 */
	private Mat grabFrame() {

		Mat frame = new Mat();

		if (this.capture.isOpened()) {
			try {
				this.capture.read(frame);

				if (!frame.empty()) {

					Mat imageROI = frame.submat(roi);
					Mat workingROI = imageROI.clone();

					Imgproc.cvtColor(imageROI, workingROI, Imgproc.COLOR_BGR2GRAY);
					// Imgproc.equalizeHist(workingROI, workingROI);
					Imgproc.blur(workingROI, workingROI, new Size(sliBlur.getValue(), sliBlur.getValue()));

					// Canny
					Imgproc.Canny(workingROI, workingROI, sliCannyThreshold.getValue(),
							sliCannyRatio.getValue() * sliCannyThreshold.getValue(), (int) cmbApertureSize.getValue(), false);
					
					// AdaptiveThreshold (meglio con equalizzazione)
					// Imgproc.adaptiveThreshold(workingROI, workingROI, 240,
					// Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
					// Imgproc.THRESH_BINARY_INV, 11, 10);

					// Hough
					Mat lines = new Mat();
					Imgproc.HoughLinesP(workingROI, lines, sliHoughRho.getValue(), Math.PI / sliHoughTheta.getValue(), (int) sliHoughThreshold.getValue(), 
							sliMinLenght.getValue(), sliMaxGap.getValue());

					// Solo due strise da disegnare, sinistra e destra
					Point[] leftStripe = new Point[2], rightStripe = new Point[2];
					leftStripe[0] = new Point(0, workingROI.rows());
					leftStripe[1] = new Point(0, 0);

					rightStripe[0] = new Point(workingROI.cols(), 0);
					rightStripe[1] = new Point(workingROI.cols(), workingROI.rows());

					double leftSlope = 0, rightSlope = 0;
					boolean leftFound = false, rightFound = false;

					Point center = new Point(workingROI.cols() / 2, workingROI.rows());
					
					for (int i = 0; i < lines.rows(); i++) {
						double[] val = lines.get(i, 0);
						
						// P2 come punto più basso del segmento
						Point p1, p2;
						if (val[1] < val[3]) {
							p1 = new Point(val[0], val[1]);
							p2 = new Point(val[2], val[3]);
						} else {
							p1 = new Point(val[2], val[3]);
							p2 = new Point(val[0], val[1]);
						}
						
						if (chkDetectedSegments.isSelected())
							Imgproc.line(imageROI, p1, p2, new Scalar(255, 0, 255), 4);
												
						double slope = Math.atan2(p2.y - p1.y, p2.x - p1.x);

						if (p2.x <= center.x && slope > Math.toRadians(60) && slope < Math.toRadians(170)
								&& Utils.EuclideanDistance(p2, center) < Utils.EuclideanDistance(leftStripe[0], center)) { 
							// P1 è a destra del centro, ha una pendenza sensata, ed è a minor distanza dal centro rispetto a quello in memoria
							leftStripe[0] = p1;
							leftStripe[1] = p2;
							leftSlope = slope;
							leftFound = true;
							
						} else if (p2.x > center.x && slope > Math.toRadians(10) && slope < Math.toRadians(120) 
								&& Utils.EuclideanDistance(p2, center) < Utils.EuclideanDistance(rightStripe[0], center)) {
							// P1 è a destra del centro, ha una pendenza sensata, ed è a minor distanza dal centro rispetto a quello in memoria
							rightStripe[0] = p1;
							rightStripe[1] = p2;
							rightSlope = slope;
							rightFound = true;
						}						
					}
									
					double alpha = leftSlope - rightSlope;
					
					if (Math.abs(lastAlpha - alpha) < Math.toRadians(3)) {				
						laneFrameCount++;
					} else {
						laneFrameCount = 0;
					}
					
					lastAlpha = alpha;
					
					// Se ha trovato entrambe e insieme non formano troppo grande o troppo piccolo, e sono regolari da n frame
					if (leftFound && rightFound && alpha > Math.toRadians(70) && alpha < Math.toRadians(120) && laneFrameCount >= 3) {					
						
						fillLane = true;
						lastLeftStripe[0] = leftStripe[0].clone();
						lastLeftStripe[1] = leftStripe[1].clone();
						lastRightStripe[0] = rightStripe[0].clone();
						lastRightStripe[1] = rightStripe[1].clone();
						
						// Estende i segmenti
						double dx = Math.cos(leftSlope) * 10000;				
						double dy = Math.sin(leftSlope) * 10000;
						
						lastLeftStripe[0].x -= dx;
						lastLeftStripe[0].y -= dy;
						lastLeftStripe[1].x += dx;
						lastLeftStripe[1].y += dy;
						
						dx = Math.cos(rightSlope) * 10000;
						dy = Math.sin(rightSlope) * 10000;
						lastRightStripe[0].x -= dx;
						lastRightStripe[0].y -= dy;
						lastRightStripe[1].x += dx;
						lastRightStripe[1].y += dy;

						Rect clipping = new Rect(Integer.MIN_VALUE / 2, 0, Integer.MAX_VALUE, workingROI.rows());
						Imgproc.clipLine(clipping, lastLeftStripe[0], lastLeftStripe[1]);
						Imgproc.clipLine(clipping, lastRightStripe[0], lastRightStripe[1]);
					}

					if (chkStripes.isSelected()) {
						Imgproc.line(imageROI, lastLeftStripe[0], lastLeftStripe[1], new Scalar(0, 0, 255), 4);
						Imgproc.line(imageROI, lastRightStripe[0], lastRightStripe[1], new Scalar(0, 0, 255), 4);
					}

					if (fillLane && chkLane.isSelected()){
						MatOfPoint lane = new MatOfPoint(lastLeftStripe[0], lastLeftStripe[1], lastRightStripe[1], lastRightStripe[0]);
						Imgproc.fillConvexPoly(imageROI, lane, new Scalar(255, 0, 0));
					}
					
					if (leftFound && chkChosenSegments.isSelected()) Imgproc.line(imageROI, leftStripe[0], leftStripe[1], new Scalar(0, 255, 0), 4);
					if (rightFound && chkChosenSegments.isSelected()) Imgproc.line(imageROI, rightStripe[0], rightStripe[1], new Scalar(0, 255, 0), 4);
										
					if (chkCenter.isSelected())
						Imgproc.line(imageROI, center, new Point(center.x, 0), new Scalar(0, 255, 255), 4);
					
					if (chkBorders.isSelected()) {		
						Imgproc.cvtColor(workingROI, workingROI, Imgproc.COLOR_GRAY2BGR);
						Core.addWeighted(imageROI, 1.0, workingROI, 0.7, 0.0, imageROI);
					}
					
				} else {
					System.err.println("Video concluso");
					return null;
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
}