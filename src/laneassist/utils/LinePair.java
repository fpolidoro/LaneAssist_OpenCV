package laneassist.utils;

import org.opencv.core.Point;

public class LinePair {

	private Line left;
	private Line right;
	private Point intersection;

	public LinePair(Line left, Line right) {
		this.left = left;
		this.right = right;
		this.intersection = null;
	}

	public double getAlpha() {
		return left.getSlope() - right.getSlope();
	}
	
	public Point getIntersection() {
		if (intersection == null)
			intersection = Utils.Intersection(left.getUpper(), left.getLower(), right.getUpper(), right.getLower());
		
		return intersection;
	}

	public Line getLeft() {
		return left;
	}

	public Line getRight() {
		return right;
	}
}
