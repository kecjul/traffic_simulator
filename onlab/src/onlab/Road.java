package onlab;

import java.awt.geom.Point2D;

public class Road {
	private float borderPoint;
	private float borderSize;
	private float ovalPartSize;
	private float rectPartSize;
	private float laneSize;
	private Point2D.Float startPosition;
	
	public Road(int ovalBorder, int borderSize, int firstOvalPartSize, int firstRectPartSize, int laneSize2, Point2D.Float startPosition) {
		setBorderPoint(ovalBorder);
		setBorderSize(borderSize);
		setOvalPartSize(firstOvalPartSize);
		setRectPartSize(firstRectPartSize);
		setLaneSize(laneSize2);
		setStartPosition(startPosition);
	}
	public float getBorderPoint() {
		return borderPoint;
	}
	public void setBorderPoint(float borderPoint) {
		this.borderPoint = borderPoint;
	}
	public float getBorderSize() {
		return borderSize;
	}
	public void setBorderSize(float borderSize) {
		this.borderSize = borderSize;
	}
	public float getOvalPartSize() {
		return ovalPartSize;
	}
	public void setOvalPartSize(float ovalPartSize) {
		this.ovalPartSize = ovalPartSize;
	}
	public float getRectPartSize() {
		return rectPartSize;
	}
	public void setRectPartSize(float rectPartSize) {
		this.rectPartSize = rectPartSize;
	}
	public float getLaneSize() {
		return laneSize;
	}
	public void setLaneSize(float laneSize) {
		this.laneSize = laneSize;
	}
	public Point2D.Float getStartPosition() {
		return startPosition;
	}
	public void setStartPosition(Point2D.Float startPosition) {
		this.startPosition = startPosition;
	}	
	public void setStartPosition(float x, float y) {
		this.startPosition = new Point2D.Float(x, y);
	}
}
