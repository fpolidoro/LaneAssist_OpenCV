package laneassist;

import laneassist.utils.Line;
import laneassist.utils.LinePair;
import laneassist.utils.Utils;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
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

public class Controller {

	private final Double ROI_WIDTH = 120.0;
	private final Double ROI_HEIGHT = 35.0;

	private final int DEFAULT_BLUR = 3;
	private final int DEFAULT_CANNY_THRESHOLD = 29;
	private final int DEFAULT_CANNY_RATIO = 3;
	private final int DEFAULT_APERTURE_SIZE = 3;

	private final int DEFAULT_HOUGH_RHO = 1;
	private final int DEFAULT_HOUGH_THETA = 180;
	private final int DEFAULT_HOUGH_THRESHOLD = 26;
	private final int DEFAULT_MIN_LENGHT = 20;
	private final int DEFAULT_MAX_GAP = 5;

	private final int DEFAULT_MIN_SLOPE = 10;
	private final int DEFAULT_MAX_SLOPE = 90;
	private final int DEFAULT_CANDIDATES_NUMBER = 7;
	private final int DEFAULT_MIN_ALPHA = 65;
	private final int DEFAULT_MAX_ALPHA = 120;
	private final int DEFAULT_HORIZON_VARIANCE = 30;
	private final int DEFAULT_FRAMES_TO_SHOW = 9;
	private final int DEFAULT_FRAMES_TO_HIDE = 9;

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
	private Slider sliCandidatesNumber;
	@FXML
	private TextField txtCandidatesNumber;
	@FXML
	private Slider sliMinAlpha;
	@FXML
	private TextField txtMinAlpha;
	@FXML
	private Slider sliMaxAlpha;
	@FXML
	private TextField txtMaxAlpha;
	@FXML
	private Slider sliHorizonVariance;
	@FXML
	private TextField txtHorizonVariance;
	@FXML
	private Slider sliFramesToShow;
	@FXML
	private TextField txtFramesToShow;
	@FXML
	private Slider sliFramesToHide;
	@FXML
	private TextField txtFramesToHide;
	@FXML
	private CheckBox chkBorders;
	@FXML
	private CheckBox chkDetectedSegments;
	@FXML
	private CheckBox chkCandidateStripes;
	@FXML
	private CheckBox chkFinalStripes;
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
	private Runnable forwardFrameGrabber;
	private Runnable backwardFrameGrabber;
	private Mat currentFrame;
	
	private int framesToShowCounter;
	private int framesToHideCounter;
	private LinePair lastLane;

	private int frameCounter;
	private float speedMultiplier;
	private boolean rewind;

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
		txtCandidatesNumber.setText(String.valueOf(DEFAULT_CANDIDATES_NUMBER));
		sliCandidatesNumber.setValue(DEFAULT_CANDIDATES_NUMBER);
		txtMinAlpha.setText(String.valueOf(DEFAULT_MIN_ALPHA));
		sliMinAlpha.setValue(DEFAULT_MIN_ALPHA);
		txtMaxAlpha.setText(String.valueOf(DEFAULT_MAX_ALPHA));
		sliMaxAlpha.setValue(DEFAULT_MAX_ALPHA);
		txtHorizonVariance.setText(String.valueOf(DEFAULT_HORIZON_VARIANCE));
		sliHorizonVariance.setValue(DEFAULT_HORIZON_VARIANCE);
		txtFramesToShow.setText(String.valueOf(DEFAULT_FRAMES_TO_SHOW));
		sliFramesToShow.setValue(DEFAULT_FRAMES_TO_SHOW);
		txtFramesToHide.setText(String.valueOf(DEFAULT_FRAMES_TO_HIDE));
		sliFramesToHide.setValue(DEFAULT_FRAMES_TO_HIDE);

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

	private void initVideo() {	
		framesToShowCounter = 0;
		framesToHideCounter = 0;
		
		lastLane = new LinePair(new Line(), new Line());
		
		frameCounter = 0;
		speedMultiplier = 1;
		rewind = false;
	}
	
	private void initScheduler() {
		forwardFrameGrabber = new Runnable() {

			@Override
			public void run() {
				currentFrame = grabFrame();
				processAndShowFrame();
				showInfo();
			}
		};

		backwardFrameGrabber = new Runnable() {

			@Override
			public void run() {
				if (frameCounter > 1) {
					frameCounter -= 2;
					capture.set(Videoio.CAP_PROP_POS_FRAMES, frameCounter);
	
					currentFrame = grabFrame();
					showFrame(currentFrame);
					showInfo();
				} else {
					setStop();
				}
			}
		};

		timer = Executors.newSingleThreadScheduledExecutor();
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

				setGUIDisabled(false);	

				currentFrame = null;
				
				initVideo();
				initScheduler();
				setPlayPause();
				
				computeROIDimension();
				computePointsForROI();
				dragROIHeight();
				dragROIWidth();
				dragROIHorizontalPosition();
				dragROIVerticalPosition();	
				
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
			// System.out.println("padding: " + padding);
			rectROI.setVisible(true);
			stackpROI.setStyle("-fx-border-color: grey ;");
		}
	}

	protected void setClosed() {	
		if (this.timer != null && !this.timer.isShutdown()) {
			
			setStop();
			setGUIDisabled(true);
			
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
		if (frameCounter < 0) {
			setStop();
			return null;
		} else {
			Mat frame = new Mat();

			if (this.capture.isOpened()) {
				this.capture.read(frame);

				if (frame.empty()) {
					setStop();
					return null;
				}
			}
			frameCounter = (int) capture.get(Videoio.CAP_PROP_POS_FRAMES);
			return frame;
		}
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

			// Calcola il centro basso e alto della ROI
			Point lowerCenter = new Point(workingROI.cols() / 2, workingROI.rows());
			Point upperCenter = new Point(workingROI.cols() / 2, 0);

			// Due liste di linee, destra e sinistra
			ArrayList<Line> leftList = new ArrayList<Line>();
			ArrayList<Line> rightList = new ArrayList<Line>();

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



				Rect clipping = new Rect(Integer.MIN_VALUE / 2, 0, Integer.MAX_VALUE, workingROI.rows());
				double slope = Math.atan2(p2.y - p1.y, p2.x - p1.x);
				
				//Smista i segmenti che rispettano i vincoli nelle rispettive liste (nel costruttore di Line, vengono anche estesi a linee)
				if (p2.x <= lowerCenter.x 
						&& slope > Math.toRadians(180 - sliMaxSlope.getValue())
						&& slope < Math.toRadians(180 - sliMinSlope.getValue())) {
					
					
					if (chkDetectedSegments.isSelected())
						Imgproc.line(imageROI, p1, p2, new Scalar(255, 0, 255), 2);
					
					leftList.add(new Line(p1, p2, slope, clipping));
				} else if (p2.x > lowerCenter.x 
						&& slope > Math.toRadians(sliMinSlope.getValue())
						&& slope < Math.toRadians(sliMaxSlope.getValue())) {
					
					if (chkDetectedSegments.isSelected())
						Imgproc.line(imageROI, p1, p2, new Scalar(255, 0, 255), 2);

					rightList.add(new Line(p1, p2, slope, clipping));
				}
			}

			// Ordina le due liste in base alla distanza tra la x più bassa della linea e il centro
			leftList.sort((Line l1, Line l2) -> (int) l2.getLower().x - (int) l1.getLower().x);
			rightList.sort((Line l1, Line l2) -> (int) l1.getLower().x - (int) l2.getLower().x);

			// La lista viene potata, restano solo 'Number of candidates' elementi
			leftList = new ArrayList<Line>(leftList.subList(0, Math.min(leftList.size(), (int) sliCandidatesNumber.getValue())));
			rightList = new ArrayList<Line>(rightList.subList(0, Math.min(rightList.size(), (int) sliCandidatesNumber.getValue())));

			// Crea tutte le possibili coppie di linee, e tiene solo quelle che rispettano i vincoli
			ArrayList<LinePair> pairList = new ArrayList<LinePair>();
			for (Line leftLine : leftList) {
				
				if (chkCandidateStripes.isSelected())
					Imgproc.line(imageROI, leftLine.getUpper(), leftLine.getLower(), new Scalar(255, 255, 0), 4);
				
				for (Line rightLine : rightList) {
					
					if (chkCandidateStripes.isSelected())
						Imgproc.line(imageROI, rightLine.getUpper(), rightLine.getLower(), new Scalar(255, 255, 0), 4);
					
					LinePair pair = new LinePair(leftLine, rightLine);
					if (pair.getAlpha() > Math.toRadians(sliMinAlpha.getValue()) && pair.getAlpha() < Math.toRadians(sliMaxAlpha.getValue())
							&& Utils.EuclideanDistance(upperCenter, pair.getIntersection()) < sliHorizonVariance.getValue()) {	
						pairList.add(pair);
					}
				}
			}
					
			// Se la lista non è vuota, vuol dire che c'è almeno una coppia di linee che rispetta i vincoli
			if (!pairList.isEmpty()) {
				
				framesToShowCounter++;
				framesToHideCounter = 0;
				
				pairList.sort((LinePair lp1, LinePair lp2) -> {
					double d1 = Math.abs(lastLane.getAlpha() - lp1.getAlpha());
					double d2 = Math.abs(lastLane.getAlpha() - lp2.getAlpha());
					
					if (d1 < d2)
						return -1;
					else if (d2 > d1)
						return 1;
					else
						return 0;
				});
				
				// Prende la coppia con la minor variazione di alpha rispetto al frame precedente
				lastLane = pairList.get(0);
				
			} else {				
				framesToHideCounter++;
				if (framesToHideCounter > sliFramesToHide.getValue())
					framesToShowCounter = 0;
			}
			
			if (framesToShowCounter > sliFramesToShow.getValue()) {
				
				Line left = lastLane.getLeft();
				Line right = lastLane.getRight();
				Point intersection = lastLane.getIntersection();

				double leftDelta = lowerCenter.x - left.getLower().x;
				double rightDelta = right.getLower().x - lowerCenter.x;

				Scalar laneColor = (leftDelta < rightDelta) ? new Scalar(0, leftDelta, 255 - leftDelta)
						: new Scalar(0, rightDelta, 255 - rightDelta);

				if (chkFinalStripes.isSelected()) {
					Mat stripesROI = imageROI.clone();
					Imgproc.line(stripesROI, intersection, left.getLower(), new Scalar(0, leftDelta, 255 - leftDelta), 6);
					Imgproc.line(stripesROI, intersection, right.getLower(), new Scalar(0, rightDelta, 255 - rightDelta), 6);
					Core.addWeighted(imageROI, 1.0 - 0.7, stripesROI, 0.7, 0.0, imageROI);
				}

				if (chkLane.isSelected()) {
					Mat laneROI = imageROI.clone();
					MatOfPoint lane = new MatOfPoint(intersection, left.getLower(), right.getLower(), intersection);
					Imgproc.fillConvexPoly(laneROI, lane, laneColor);
					Core.addWeighted(imageROI, 1.0 - 0.4, laneROI, 0.4, 0.0, imageROI);
				}
			}

			if (chkBorders.isSelected()) {
				Imgproc.cvtColor(workingROI, workingROI, Imgproc.COLOR_GRAY2BGR);
				Core.addWeighted(imageROI, 1.0, workingROI, 0.7, 0.0, imageROI);
			}

			if (chkROI.isSelected()) {
				Imgproc.rectangle(frame, leftTopPointROI, rightBottomPointROI, new Scalar(255, 255, 255), 2);
				Imgproc.line(imageROI, lowerCenter, new Point(lowerCenter.x, 0), new Scalar(255, 255, 255), 2);
			}

			showFrame(frame);
		}
	}

	/**
	 * Mostra il frame
	 */
	private void showFrame(Mat frame) {
		Image imageToShow = Utils.mat2Image(frame);
		Utils.onFXThread(currentImage.imageProperty(), imageToShow);
	}

	/**
	 * Visualizza le informazioni sul video
	 */
	private void showInfo() {
		int millis = (frameCounter - 1) * 33;
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
				lblSpeed.setText(
						new BigDecimal(speedMultiplier).setScale(3, RoundingMode.DOWN).stripTrailingZeros().toString()
								+ "x");
			}
		});
	}

	// Sezione controlli video

	/**
	 * Comando bottone stop
	 */
	@FXML
	private void setStop() {
		rewind = false;
		speedMultiplier = 1;
		imgPlayPause.setImage(playImg);
		if (!future.isCancelled())
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
			if (rewind) {
				if (!future.isCancelled())
					future.cancel(false);
				rewind = false;
				speedMultiplier = 1;
			}

			lblSpeed.setVisible(true);
			imgPlayPause.setImage(pauseImg);
			future = timer.scheduleAtFixedRate(forwardFrameGrabber, 0, (long) (33 / speedMultiplier),
					TimeUnit.MILLISECONDS);
		} else {
			imgPlayPause.setImage(playImg);
			if (!future.isCancelled())
				future.cancel(false);
			lblSpeed.setVisible(false);
		}
	}

	/**
	 * Comando bottone previous frame
	 */
	@FXML
	private void setPreviousFrame() {
		if (imgPlayPause.getImage().equals(pauseImg) || rewind) {
			rewind = false;
			imgPlayPause.setImage(playImg);
			if (!future.isCancelled())
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
		if (imgPlayPause.getImage().equals(pauseImg) || rewind) {
			rewind = false;
			imgPlayPause.setImage(playImg);
			if (!future.isCancelled())
				future.cancel(false);
			lblSpeed.setVisible(false);
		}

		currentFrame = grabFrame();
		processAndShowFrame();
		showInfo();
	}

	/**
	 * Comando bottone rewind
	 */
	@FXML
	private void setRewind() {
		if (!rewind) {
			speedMultiplier = 1;
			imgPlayPause.setImage(playImg);
			rewind = true;
		}

		if (speedMultiplier < 32) {
			speedMultiplier *= 2;
		}

		lblSpeed.setVisible(true);

		if (!future.isCancelled())
			future.cancel(false);
		future = timer.scheduleAtFixedRate(backwardFrameGrabber, 0, (long) (33 / speedMultiplier),
				TimeUnit.MILLISECONDS);
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

		lblSpeed.setVisible(true);
		rewind = false;

		if (!future.isCancelled())
			future.cancel(false);
		future = timer.scheduleAtFixedRate(forwardFrameGrabber, 0, (long) (33 / speedMultiplier),
				TimeUnit.MILLISECONDS);
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

		lblSpeed.setVisible(true);
		rewind = false;

		if (!future.isCancelled())
			future.cancel(false);
		future = timer.scheduleAtFixedRate(forwardFrameGrabber, 0, (long) (33 / speedMultiplier),
				TimeUnit.MILLISECONDS);
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
	private void dragCandidatesNumber() {
		int value = (int) sliCandidatesNumber.getValue();
		txtCandidatesNumber.setText(String.valueOf(value));
		processAndShowFrame();
	}

	@FXML
	private void setCandidatesNumber() {
		String text = txtCandidatesNumber.getText();
		if (text.matches("\\d*|\\d*\\.\\d")) {
			sliCandidatesNumber.setValue(Integer.valueOf(text));
		}
		txtCandidatesNumber.setText(String.valueOf((int) sliCandidatesNumber.getValue()));
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
	private void dragHorizonVariance() {
		int value = (int) sliHorizonVariance.getValue();
		txtHorizonVariance.setText(String.valueOf(value));
		processAndShowFrame();
	}

	@FXML
	private void setHorizonVariance() {
		String text = txtHorizonVariance.getText();
		if (text.matches("\\d*|\\d*\\.\\d")) {
			sliHorizonVariance.setValue(Integer.valueOf(text));
		}
		txtHorizonVariance.setText(String.valueOf((int) sliHorizonVariance.getValue()));
		processAndShowFrame();
	}

	@FXML
	private void dragFramesToShow() {
		int value = (int) sliFramesToShow.getValue();
		txtFramesToShow.setText(String.valueOf(value));
		processAndShowFrame();
	}

	@FXML
	private void setFramesToShow() {
		String text = txtFramesToShow.getText();
		if (text.matches("\\d*|\\d*\\.\\d")) {
			sliFramesToShow.setValue(Integer.valueOf(text));
		}
		txtFramesToShow.setText(String.valueOf((int) sliFramesToShow.getValue()));
		processAndShowFrame();
	}

	@FXML
	private void dragFramesToHide() {
		int value = (int) sliFramesToHide.getValue();
		txtFramesToHide.setText(String.valueOf(value));
		processAndShowFrame();
	}

	@FXML
	private void setFramesToHide() {
		String text = txtFramesToHide.getText();
		if (text.matches("\\d*|\\d*\\.\\d")) {
			sliFramesToHide.setValue(Integer.valueOf(text));
		}
		txtFramesToHide.setText(String.valueOf((int) sliFramesToHide.getValue()));
		processAndShowFrame();
	}
}
