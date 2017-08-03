package laneassist;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
	
	private final int DEFAULT_MIN_SLOPE = 10;
	private final int DEFAULT_MAX_SLOPE = 120;
	private final int DEFAULT_MIN_ALPHA = 70;
	private final int DEFAULT_MAX_ALPHA = 120;
	private final int DEFAULT_ALPHA_VARIANCE = 3;
	private final int DEFAULT_FRAME_WINDOW = 3;
	private final int DEFAULT_NO_FRAME_WINDOW = 50;
	
	@FXML
	private CheckMenuItem menuItemShowDebug;
	@FXML
	private ImageView currentImage;
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

	// Debug
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
	private Slider sliMinSlope;
	@FXML
	private TextField txtMinSlope;
	@FXML
	private Slider sliMaxSlope;
	@FXML
	private TextField txtMaxSlope;
	@FXML
	private Slider sliMinAlpha;
	@FXML
	private TextField txtMinAlpha;
	@FXML
	private Slider sliMaxAlpha;
	@FXML
	private TextField txtMaxAlpha;
	@FXML
	private Slider sliAlphaVariance;
	@FXML
	private TextField txtAlphaVariance;
	@FXML
	private Slider sliFrameWindow;
	@FXML
	private TextField txtFrameWindow;
	@FXML
	private Slider sliNoFrameWindow;
	@FXML
	private TextField txtNoFrameWindow;
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
	private CheckBox chkROI;

	private Stage stage;
	private Node debugPane;
	private Image playImg;
	private Image pauseImg;

	private Insets spROIPadding;
	private double hPaddingMax;
	private double vPaddingMax;
	private double curROIPaneWidth;
	private double curROIPaneHeight;
	private double frameWidth;
	private double frameHeight;
	private Point leftTopPointROI;
	private Point rightBottomPointROI;
	private Rect roi;

	private ScheduledExecutorService timer;
	private ScheduledFuture<?> future;
	private VideoCapture capture = new VideoCapture();	
	private Runnable frameGrabber;
	private Mat currentFrame;

	private Point[] lastLeftStripe;
	private Point[] lastRightStripe;
	private double lastAlpha;
	private int laneFrameCount;
	private int noLaneFrameCount;
	private boolean fillLane;
	
	private Scalar laneColor;
	private double[] red;
	private double[] green;
	private double roiCenter;

	private int frameCounter;
	private float speedMultiplier;

	/**
	 * Inizializza i comandi della GUI
	 */
	protected void initializeGUI(Stage stage) {
		this.stage = stage;
		leftTopPointROI = new Point();
		rightBottomPointROI = new Point();
		roi = new Rect();
		setGUIDisabled(true);

		// Debug
		debugPane = mainSplitPane.getItems().get(1);

		txtBlur.setText(String.valueOf(DEFAULT_BLUR));
		sliBlur.setValue(DEFAULT_BLUR);
		txtCannyThreshold.setText(String.valueOf(DEFAULT_CANNY_THRESHOLD));
		sliCannyThreshold.setValue(DEFAULT_CANNY_THRESHOLD);
		txtCannyRatio.setText(String.valueOf(DEFAULT_CANNY_RATIO));
		sliCannyRatio.setValue(DEFAULT_CANNY_RATIO);
		cmbApertureSize.getItems().addAll(3, 5, 7);
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

		txtMinSlope.setText(String.valueOf(DEFAULT_MIN_SLOPE));
		sliMinSlope.setValue(DEFAULT_MIN_SLOPE);
		txtMaxSlope.setText(String.valueOf(DEFAULT_MAX_SLOPE));
		sliMaxSlope.setValue(DEFAULT_MAX_SLOPE);
		txtMinAlpha.setText(String.valueOf(DEFAULT_MIN_ALPHA));
		sliMinAlpha.setValue(DEFAULT_MIN_ALPHA);
		txtMaxAlpha.setText(String.valueOf(DEFAULT_MAX_ALPHA));
		sliMaxAlpha.setValue(DEFAULT_MAX_ALPHA);
		txtAlphaVariance.setText(String.valueOf(DEFAULT_ALPHA_VARIANCE));
		sliAlphaVariance.setValue(DEFAULT_ALPHA_VARIANCE);
		txtFrameWindow.setText(String.valueOf(DEFAULT_FRAME_WINDOW));
		sliFrameWindow.setValue(DEFAULT_FRAME_WINDOW);
		txtNoFrameWindow.setText(String.valueOf(DEFAULT_NO_FRAME_WINDOW));
		sliNoFrameWindow.setValue(DEFAULT_NO_FRAME_WINDOW);
		
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
		
		laneColor = new Scalar(0,0,0);
		red = new double[]{0.0, 0.0, 255.0};
		green = new double[]{0.0, 255.0, 0.0};
	}
	
	private void initVideo(){
		lastLeftStripe = new Point[2];
		lastRightStripe = new Point[2];
		lastLeftStripe[0] = new Point(0, roi.height);
		lastLeftStripe[1] = new Point(0, 0);
		lastRightStripe[0] = new Point(roi.width, 0);
		lastRightStripe[1] = new Point(roi.width, roi.height);
		lastAlpha = Math.toRadians(90);
		laneFrameCount = 0;
		fillLane = false;

		frameCounter = -1;
		speedMultiplier = 1;
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
//				double frameCount = capture.get(Videoio.CAP_PROP_FRAME_COUNT);
//				System.out.println("frameCount = " + frameCount);
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
				noLaneFrameCount = 0;
				fillLane = false;
				
				frameCounter = 0;

				initVideo();

				frameGrabber = new Runnable() {

					@Override
					public void run() {
						currentFrame = grabFrame();
						processAndShowFrame();
						showInfo();
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
			stage.setWidth(stage.getWidth() + 235);

		} else {
			mainSplitPane.getItems().remove(debugPane);
			stage.setWidth(stage.getWidth() - 235);
		}
	}

	protected void setGUIDisabled(boolean value) {
		paneControls.setDisable(value);
		splitPaneDebug.setDisable(value);
		if (value) {
			// schiarisco i colori del rettangolo ROI
			rectROI.setVisible(false);
			stackpROI.setStyle("-fx-border-color: lightgrey ;");
		} else {
			// attivo il rettangolo azzurro e lo posiziono in base ai cursori
			hPaddingMax = stackpROI.getWidth() - rectROI.getWidth();
			vPaddingMax = stackpROI.getHeight() - rectROI.getHeight();
			Double padding = hPaddingMax / 2;
			spROIPadding = stackpROI.getPadding();
			stackpROI.setPadding(new Insets(spROIPadding.getTop(), spROIPadding.getRight(), 0.0, padding));
			curROIPaneWidth = stackpROI.getWidth();
			curROIPaneHeight = stackpROI.getHeight();
//			System.out.println("padding: " + padding);
			rectROI.setVisible(true);
			stackpROI.setStyle("-fx-border-color: grey ;");
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
			Utils.onFXThread(currentImage.imageProperty(), null);
		}

	}

	/**
	 * Ottiene un nuovo frame
	 * 
	 * @return Mat contenente il nuovo frame
	 */
	private Mat grabFrame() {

		Mat frame = new Mat();

		if (this.capture.isOpened()) {
			try {
				this.capture.read(frame);

				if (frame.empty()) {
					setStop();
					return null;
				}
			} catch (Exception e) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error while processing the image");
				alert.setHeaderText(e.getMessage());
				alert.showAndWait();
			}
		}
		frameCounter++;
		return frame;
	}

	/**
	 * Processa il frame corrente
	 */
	@FXML
	private void processAndShowFrame() {
		if (currentFrame != null) {
			Mat frame = currentFrame.clone();
			Mat imageROI = frame.submat(roi);
			Mat workingROI = imageROI.clone();

			Imgproc.cvtColor(imageROI, workingROI, Imgproc.COLOR_BGR2GRAY);
			Imgproc.blur(workingROI, workingROI, new Size(sliBlur.getValue(), sliBlur.getValue()));
			Imgproc.Canny(workingROI, workingROI, sliCannyThreshold.getValue(),
					sliCannyRatio.getValue() * sliCannyThreshold.getValue(), (int) cmbApertureSize.getValue(), false);

			Mat lines = new Mat();
			Imgproc.HoughLinesP(workingROI, lines, sliHoughRho.getValue(), Math.PI / sliHoughTheta.getValue(),
					(int) sliHoughThreshold.getValue(), sliMinLenght.getValue(), sliMaxGap.getValue());

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
			Point center = new Point(workingROI.cols() / 2, workingROI.rows());

			for (int i = 0; i < lines.rows(); i++) {
				double[] val = lines.get(i, 0);

				// P2 come punto pi basso del segmento
				Point p1, p2;
				if (val[1] < val[3]) {
					p1 = new Point(val[0], val[1]);
					p2 = new Point(val[2], val[3]);
				} else {
					p1 = new Point(val[2], val[3]);
					p2 = new Point(val[0], val[1]);
				}

				if (chkDetectedSegments.isSelected())
					Imgproc.line(imageROI, p1, p2, new Scalar(255, 0, 255), 2);

				double slope = Math.atan2(p2.y - p1.y, p2.x - p1.x);

				if (p2.x <= center.x && slope > Math.toRadians(180 - sliMaxSlope.getValue())
						&& slope < Math.toRadians(180 - sliMinSlope.getValue())
						&& Utils.EuclideanDistance(p2, center) < Utils.EuclideanDistance(leftStripe[0], center)) {
					// P1  a destra del centro, ha una pendenza sensata, ed  a
					// minor distanza dal centro rispetto a quello in memoria
					leftStripe[0] = p1;
					leftStripe[1] = p2;
					leftSlope = slope;
					leftFound = true;

				} else if (p2.x > center.x && slope > Math.toRadians(sliMinSlope.getValue())
						&& slope < Math.toRadians(sliMaxSlope.getValue())
						&& Utils.EuclideanDistance(p2, center) < Utils.EuclideanDistance(rightStripe[0], center)) {
					// P1  a destra del centro, ha una pendenza sensata, ed  a
					// minor distanza dal centro rispetto a quello in memoria
					rightStripe[0] = p1;
					rightStripe[1] = p2;
					rightSlope = slope;
					rightFound = true;
				}
			}

			double alpha = leftSlope - rightSlope;

			if (Math.abs(lastAlpha - alpha) < Math.toRadians(sliAlphaVariance.getValue())) {
				laneFrameCount++;
			} else {
				laneFrameCount = 0;
			}
			
			if (chkDetectedSegments.isSelected())
				Imgproc.line(imageROI, p1, p2, new Scalar(255, 0, 255), 2);
									
			double slope = Math.atan2(p2.y - p1.y, p2.x - p1.x);

			if (p2.x <= center.x && slope > Math.toRadians(180 - sliMaxSlope.getValue()) && slope < Math.toRadians(180 - sliMinSlope.getValue())
					&& Utils.EuclideanDistance(p2, center) < Utils.EuclideanDistance(leftStripe[0], center)) { 
				// P1 è a destra del centro, ha una pendenza sensata, ed è a minor distanza dal centro rispetto a quello in memoria
				leftStripe[0] = p1;
				leftStripe[1] = p2;
				leftSlope = slope;
				leftFound = true;
				
			} else if (p2.x > center.x && slope > Math.toRadians(sliMinSlope.getValue()) && slope < Math.toRadians(sliMaxSlope.getValue()) 
					&& Utils.EuclideanDistance(p2, center) < Utils.EuclideanDistance(rightStripe[0], center)) {
				// P1 è a destra del centro, ha una pendenza sensata, ed è a minor distanza dal centro rispetto a quello in memoria
				rightStripe[0] = p1;
				rightStripe[1] = p2;
				rightSlope = slope;
				rightFound = true;
			}						
		}
						
		double alpha = leftSlope - rightSlope;
		
		if (Math.abs(lastAlpha - alpha) < Math.toRadians(sliAlphaVariance.getValue())) {				
			laneFrameCount++;
		} else {
			laneFrameCount = 0;
			noLaneFrameCount++;
		}
		
		lastAlpha = alpha;
		
		// Se ha trovato entrambe e insieme non formano troppo grande o troppo piccolo, e sono regolari da n frame
		if (leftFound && rightFound 
				&& alpha > Math.toRadians(sliMinAlpha.getValue()) && alpha < Math.toRadians(sliMaxAlpha.getValue())
				&& laneFrameCount >= (int) sliFrameWindow.getValue()) {					
			
			fillLane = true;
			noLaneFrameCount = 0;
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
		}else{	//se per m frame non trovo nulla, rendo invisibile la corsia colorata
			if(noLaneFrameCount >= (int) sliNoFrameWindow.getValue()){
				fillLane = false;
			}
		}
							
		Point intersection = Utils.Intersection(lastLeftStripe[0], lastLeftStripe[1], lastRightStripe[0], lastRightStripe[1]);
		//Point topLeft = new Point(lastLeftStripe[1].x, intersection.y);
		//Point bottomRight = new Point(lastRightStripe[1].x, 0);
		if (chkStripes.isSelected() && (leftFound && rightFound)) {
			Mat stripesROI = imageROI.clone();
			Imgproc.line(stripesROI, intersection, lastLeftStripe[1], new Scalar(0, 0, 255), 4);	//rosse
			Imgproc.line(stripesROI, intersection, lastRightStripe[1], new Scalar(0, 0, 255), 4);	//rosse
			Core.addWeighted(imageROI, 1.0-0.7, stripesROI, 0.7, 0.0, imageROI);
		}
		roiCenter = leftTopPointROI.x + center.x;
		/*if(fillLane)
			System.out.println("lastLeftStripe.x = " + lastLeftStripe[1].x + "; lastRightStripe.x = " + lastRightStripe[1].x +
				"\nrightBottomPointROI.x = " + rightBottomPointROI.x + "; leftTopPointROI.x = " + leftTopPointROI.x + "; roiCenter = " + roiCenter + "\n");*/

		
		if (fillLane && chkLane.isSelected() && (rightFound && leftFound)){
			if(lastLeftStripe[1].x <= (roiCenter - ((rightBottomPointROI.x - leftTopPointROI.x)/(2*3))) &&
					lastRightStripe[1].x >= (roiCenter + ((rightBottomPointROI.x - leftTopPointROI.x)/(2*3)))){
				laneColor.set(green);
			}else laneColor.set(red);
			
			Mat laneROI = imageROI.clone();
			//laneROI = new Mat(imageROI.rows(),imageROI.cols(), imageROI.type(), new Scalar(0,0,0));
			MatOfPoint lane = new MatOfPoint(intersection, lastLeftStripe[1], lastRightStripe[1], intersection);
			//Utils.FillWithGradient(imageROI, intersection, lastLeftStripe[1], lastRightStripe[1]);
			Imgproc.fillConvexPoly(laneROI, lane, laneColor);
			Core.addWeighted(imageROI, 1.0-0.4, laneROI, 0.4, 0.0, imageROI);
		}
		
		if (leftFound && chkChosenSegments.isSelected()) Imgproc.line(workingROI, leftStripe[0], leftStripe[1], new Scalar(0, 255, 0), 4);	//verdi
		if (rightFound && chkChosenSegments.isSelected()) Imgproc.line(workingROI, rightStripe[0], rightStripe[1], new Scalar(0, 255, 0), 4);
							
		if (chkCenter.isSelected()){
			Imgproc.line(imageROI, center, new Point(center.x, 0), new Scalar(0, 255, 255), 4);
		}
		
		if (chkBorders.isSelected()) {		
			Imgproc.cvtColor(workingROI, workingROI, Imgproc.COLOR_GRAY2BGR);
			Core.addWeighted(imageROI, 1.0, workingROI, 0.7, 0.0, imageROI);
		}					

		
		Imgproc.rectangle(frame, leftTopPointROI, rightBottomPointROI, new Scalar(255, 255, 255), 2);
		
		Image imageToShow = Utils.mat2Image(frame);
		Utils.onFXThread(currentImage.imageProperty(), imageToShow);

			lastAlpha = alpha;

			// Se ha trovato entrambe e insieme non formano troppo grande o
			// troppo piccolo, e sono regolari da n frame
			if (leftFound && rightFound && alpha > Math.toRadians(sliMinAlpha.getValue())
					&& alpha < Math.toRadians(sliMaxAlpha.getValue())
					&& laneFrameCount >= (int) sliFrameWindow.getValue()) {

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

			if (fillLane && chkStripes.isSelected()) {
				Imgproc.line(imageROI, lastLeftStripe[0], lastLeftStripe[1], new Scalar(0, 255, 0), 4);
				Imgproc.line(imageROI, lastRightStripe[0], lastRightStripe[1], new Scalar(0, 255, 0), 4);
			}

			if (fillLane && chkLane.isSelected()) {
				MatOfPoint lane = new MatOfPoint(lastLeftStripe[0], lastLeftStripe[1], lastRightStripe[1],
						lastRightStripe[0]);
				Imgproc.fillConvexPoly(imageROI, lane, new Scalar(255, 0, 0));
			}

			if (leftFound && chkChosenSegments.isSelected())
				Imgproc.line(imageROI, leftStripe[0], leftStripe[1], new Scalar(255, 255, 0), 3);
			if (rightFound && chkChosenSegments.isSelected())
				Imgproc.line(imageROI, rightStripe[0], rightStripe[1], new Scalar(255, 255, 0), 3);

			if (chkBorders.isSelected()) {
				Imgproc.cvtColor(workingROI, workingROI, Imgproc.COLOR_GRAY2BGR);
				Core.addWeighted(imageROI, 1.0, workingROI, 0.7, 0.0, imageROI);
			}

			if (chkROI.isSelected()) {
				Imgproc.rectangle(frame, leftTopPointROI, rightBottomPointROI, new Scalar(255, 255, 255), 2);
				Imgproc.line(imageROI, center, new Point(center.x, 0), new Scalar(255, 255, 255), 2);
			}

			Image imageToShow = Utils.mat2Image(frame);
			Utils.onFXThread(currentImage.imageProperty(), imageToShow);
		}
	}

	/**
	 * Visualizza le informazioni sul video
	 */
	private void showInfo() {
		int millis = frameCounter * 33;
		int seconds = millis / 1000;
		int minutes = (seconds / 60) % 60;
		seconds %= 60;
		millis %= 1000;
		String time = String.format("%02d", minutes) + ":" + String.format("%02d", seconds) + ":"
				+ String.format("%04d", millis);

		Platform.runLater(() -> {
			lblFrame.setText(Integer.toString(frameCounter));
			lblTime.setText(time);
			if (speedMultiplier > 0.9 && speedMultiplier < 1.1) {
				lblSpeed.setVisible(false);
			} else {
				lblSpeed.setVisible(true);
				lblSpeed.setText(new BigDecimal(speedMultiplier)
						.setScale(3, RoundingMode.DOWN)
						.stripTrailingZeros()
						.toString() + "x");
			}
		});
	}

	// Sezione controlli video

	/**
	 * Comando bottone stop
	 */
	@FXML
	private void setStop() {
		speedMultiplier = 1;
		imgPlayPause.setImage(playImg);
		future.cancel(false);
		lblSpeed.setVisible(false);
		
		capture.set(Videoio.CAP_PROP_POS_FRAMES, 0);
		initVideo();
		
		currentFrame = grabFrame();
		processAndShowFrame();
		showInfo();
	}

	/**
	 * Comando bottone play/pause
	 */
	@FXML
	private void setPlayPause() {
		if (imgPlayPause.getImage().equals(playImg)) {
			imgPlayPause.setImage(pauseImg);
			future = timer.scheduleAtFixedRate(frameGrabber, 0, (long) (33 / speedMultiplier), TimeUnit.MILLISECONDS);

		} else {
			imgPlayPause.setImage(playImg);
			future.cancel(false);
			lblSpeed.setVisible(false);
		}
	}
	
	/**
	 * Comando bottone rewind
	 */
	@FXML
	private void setRewind() {

	}
	
	/**
	 * Comando bottone previous frame
	 */
	@FXML
	private void setPreviousFrame() {
		if (imgPlayPause.getImage().equals(pauseImg)) {
			imgPlayPause.setImage(playImg);
			future.cancel(false);
			lblSpeed.setVisible(false);
		}
	
		frameCounter -= 2;
		capture.set(Videoio.CAP_PROP_POS_FRAMES, frameCounter);
		
		currentFrame = grabFrame();	
		processAndShowFrame();
		showInfo();
	}

	/**
	 * Comando bottone next frame
	 */
	@FXML
	private void setNextFrame() {
		if (imgPlayPause.getImage().equals(pauseImg)) {
			imgPlayPause.setImage(playImg);
			future.cancel(false);
			lblSpeed.setVisible(false);
		}
		
		currentFrame = grabFrame();
		processAndShowFrame();
		showInfo();
	}
	
	/**
	 * Comando bottone slow
	 */
	@FXML
	private void setSlow() {
		if (imgPlayPause.getImage().equals(playImg))
			imgPlayPause.setImage(pauseImg);
		
		if (speedMultiplier > (float) 1 / 8)
			speedMultiplier /= 2;
		
		future.cancel(false);
		future = timer.scheduleAtFixedRate(frameGrabber, 0, (long) (33 / speedMultiplier), TimeUnit.MILLISECONDS);
	}
	
	/**
	 * Comando bottone fast forward
	 */
	@FXML
	private void setFastForward() {
		if (imgPlayPause.getImage().equals(playImg))
			imgPlayPause.setImage(pauseImg);
		
		if (speedMultiplier < 32)
			speedMultiplier *= 2;
		
		future.cancel(false);
		future = timer.scheduleAtFixedRate(frameGrabber, 0, (long) (33 / speedMultiplier), TimeUnit.MILLISECONDS);
	}

	// Sezione ROI

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
		roi = new Rect(leftTopPointROI, rightBottomPointROI);
		
		processAndShowFrame();
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
		imgMoveROIUpDown.setVisible(true);
		rectROI.setVisible(false);
	}

	@FXML
	private void mouseExitedMoveROIUpDown() {
		imgMoveROIUpDown.setVisible(false);
		rectROI.setVisible(true);
	}

	// Sezione Canny

	@FXML
	private void dragBlur() {
		int value = (int) sliBlur.getValue();
		txtBlur.setText(String.valueOf(value));
		processAndShowFrame();
	}

	@FXML
	private void setBlur() {
		String text = txtBlur.getText();
		if (text.matches("\\d*|\\d*\\.\\d")) {
			sliBlur.setValue(Integer.valueOf(text));
		}
		txtBlur.setText(String.valueOf((int) sliBlur.getValue()));
		processAndShowFrame();
	}

	@FXML
	private void dragCannyThreshold() {
		int value = (int) sliCannyThreshold.getValue();
		txtCannyThreshold.setText(String.valueOf(value));
		processAndShowFrame();
	}

	@FXML
	private void setCannyThreshold() {
		String text = txtCannyThreshold.getText();
		if (text.matches("\\d*")) {
			sliCannyThreshold.setValue(Integer.valueOf(text));
		}
		txtCannyThreshold.setText(String.valueOf((int) sliCannyThreshold.getValue()));
		processAndShowFrame();
	}

	@FXML
	private void dragCannyRatio() {
		int value = (int) sliCannyRatio.getValue();
		txtCannyRatio.setText(String.valueOf(value));
		processAndShowFrame();
	}

	@FXML
	private void setCannyRatio() {
		String text = txtCannyRatio.getText();
		if (text.matches("\\d*|\\d*\\.\\d")) {
			sliCannyRatio.setValue(Integer.valueOf(text));
		}
		txtCannyRatio.setText(String.valueOf((int) sliCannyRatio.getValue()));
		processAndShowFrame();
	}

	// Sezione Hough

	@FXML
	private void dragHoughRho() {
		int value = (int) sliHoughRho.getValue();
		txtHoughRho.setText(String.valueOf(value));
		processAndShowFrame();
	}

	@FXML
	private void setHoughRho() {
		String text = txtHoughRho.getText();
		if (text.matches("\\d*|\\d*\\.\\d")) {
			sliHoughRho.setValue(Integer.valueOf(text));
		}
		txtHoughRho.setText(String.valueOf((int) sliHoughRho.getValue()));
		processAndShowFrame();
	}

	@FXML
	private void dragHoughTheta() {
		int value = (int) sliHoughTheta.getValue();
		txtHoughTheta.setText(String.valueOf(value));
		processAndShowFrame();
	}

	@FXML
	private void setHoughTheta() {
		String text = txtHoughTheta.getText();
		if (text.matches("\\d*|\\d*\\.\\d")) {
			sliHoughTheta.setValue(Integer.valueOf(text));
		}
		txtHoughTheta.setText(String.valueOf((int) sliHoughTheta.getValue()));
		processAndShowFrame();
	}

	@FXML
	private void dragHoughThreshold() {
		int value = (int) sliHoughThreshold.getValue();
		txtHoughThreshold.setText(String.valueOf(value));
		processAndShowFrame();
	}

	@FXML
	private void setHoughThreshold() {
		String text = txtHoughThreshold.getText();
		if (text.matches("\\d*|\\d*\\.\\d")) {
			sliHoughThreshold.setValue(Integer.valueOf(text));
		}
		txtHoughThreshold.setText(String.valueOf((int) sliHoughThreshold.getValue()));
		processAndShowFrame();
	}

	@FXML
	private void dragMinLenght() {
		int value = (int) sliMinLenght.getValue();
		txtMinLenght.setText(String.valueOf(value));
		processAndShowFrame();
	}

	@FXML
	private void setMinLenght() {
		String text = txtMinLenght.getText();
		if (text.matches("\\d*|\\d*\\.\\d")) {
			sliMinLenght.setValue(Integer.valueOf(text));
		}
		txtMinLenght.setText(String.valueOf((int) sliMinLenght.getValue()));
		processAndShowFrame();
	}

	@FXML
	private void dragMaxGap() {
		int value = (int) sliMaxGap.getValue();
		txtMaxGap.setText(String.valueOf(value));
		processAndShowFrame();
	}

	@FXML
	private void setMaxGap() {
		String text = txtMaxGap.getText();
		if (text.matches("\\d*|\\d*\\.\\d")) {
			sliMaxGap.setValue(Integer.valueOf(text));
		}
		txtMaxGap.setText(String.valueOf((int) sliMaxGap.getValue()));
		processAndShowFrame();
	}

	// Sezione Constraints
	@FXML
	private void dragMinSlope() {
		int value = (int) sliMinSlope.getValue();
		txtMinSlope.setText(String.valueOf(value));
		processAndShowFrame();
	}

	@FXML
	private void setMinSlope() {
		String text = txtMinSlope.getText();
		if (text.matches("\\d*|\\d*\\.\\d")) {
			sliMinSlope.setValue(Integer.valueOf(text));
		}
		txtMinSlope.setText(String.valueOf((int) sliMinSlope.getValue()));
		processAndShowFrame();
	}

	@FXML
	private void dragMaxSlope() {
		int value = (int) sliMaxSlope.getValue();
		txtMaxSlope.setText(String.valueOf(value));
		processAndShowFrame();
	}

	@FXML
	private void setMaxSlope() {
		String text = txtMaxSlope.getText();
		if (text.matches("\\d*|\\d*\\.\\d")) {
			sliMaxSlope.setValue(Integer.valueOf(text));
		}
		txtMaxSlope.setText(String.valueOf((int) sliMaxSlope.getValue()));
		processAndShowFrame();
	}

	@FXML
	private void dragMinAlpha() {
		int value = (int) sliMinAlpha.getValue();
		txtMinAlpha.setText(String.valueOf(value));
		processAndShowFrame();
	}

	@FXML
	private void setMinAlpha() {
		String text = txtMinAlpha.getText();
		if (text.matches("\\d*|\\d*\\.\\d")) {
			sliMinAlpha.setValue(Integer.valueOf(text));
		}
		txtMinAlpha.setText(String.valueOf((int) sliMinAlpha.getValue()));
		processAndShowFrame();
	}

	@FXML
	private void dragMaxAlpha() {
		int value = (int) sliMaxAlpha.getValue();
		txtMaxAlpha.setText(String.valueOf(value));
		processAndShowFrame();
	}

	@FXML
	private void setMaxAlpha() {
		String text = txtMaxAlpha.getText();
		if (text.matches("\\d*|\\d*\\.\\d")) {
			sliMaxAlpha.setValue(Integer.valueOf(text));
		}
		txtMaxAlpha.setText(String.valueOf((int) sliMaxAlpha.getValue()));
		processAndShowFrame();
	}

	@FXML
	private void dragAlphaVariance() {
		int value = (int) sliAlphaVariance.getValue();
		txtAlphaVariance.setText(String.valueOf(value));
		processAndShowFrame();
	}

	@FXML
	private void setAlphaVariance() {
		String text = txtAlphaVariance.getText();
		if (text.matches("\\d*|\\d*\\.\\d")) {
			sliAlphaVariance.setValue(Integer.valueOf(text));
		}
		txtAlphaVariance.setText(String.valueOf((int) sliAlphaVariance.getValue()));
		processAndShowFrame();
	}

	@FXML
	private void dragFrameWindow() {
		int value = (int) sliFrameWindow.getValue();
		txtFrameWindow.setText(String.valueOf(value));
		processAndShowFrame();
	}

	@FXML
	private void setFrameWindow() {
		String text = txtFrameWindow.getText();
		if (text.matches("\\d*|\\d*\\.\\d")) {
			sliFrameWindow.setValue(Integer.valueOf(text));
		}
		txtFrameWindow.setText(String.valueOf((int) sliFrameWindow.getValue()));
		processAndShowFrame();
	}
	
	@FXML
	private void dragNoFrameWindow() {
		int value = (int) sliNoFrameWindow.getValue();
		txtNoFrameWindow.setText(String.valueOf(value));
		processAndShowFrame();
	}
	
	@FXML
	private void setNoFrameWindow() {
		String text = txtNoFrameWindow.getText();
		if (text.matches("\\d*|\\d*\\.\\d")) {								
			sliFrameWindow.setValue(Integer.valueOf(text));
		}
		txtNoFrameWindow.setText(String.valueOf((int) sliFrameWindow.getValue()));	
		processAndShowFrame();
	}
}
