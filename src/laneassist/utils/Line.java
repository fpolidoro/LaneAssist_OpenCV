package laneassist.utils;

import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

public class Line {
	
	private Point upper;
	private Point lower;
	private double slope;
	
	public Line(){
		upper = new Point(0,0);
		lower = new Point(0,0);		
		slope = 0;
	}
	
	public Line(Point upper, Point lower, double slope, Rect clipping) {	
		this.upper = upper;
		this.lower = lower;
		this.slope = slope;
		
		double dx = Math.cos(slope) * 10000;
		double dy = Math.sin(slope) * 10000;

		upper.x -= dx;
		upper.y -= dy;
		lower.x += dx;
		lower.y += dy;
		
		Imgproc.clipLine(clipping, upper, lower);
	}
	
	public Point getUpper() {
		return upper;
	}
	
	public Point getLower() {
		return lower;
	}
	
	public double getSlope() {
		return slope;
	}
}
