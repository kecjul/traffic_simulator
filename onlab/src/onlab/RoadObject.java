package onlab;

import java.awt.geom.Point2D;

public class RoadObject {
	public static int count = 0;
	public int id;
	private Point2D.Float position = new Point2D.Float();
	private int lane = 1;
	private int size = 20;
	
	public Point2D.Float getPosition() {
		return position;
	}
	public void setPosition(Point2D.Float position) {
		this.position = position;
	}
	public int getLane() {
		return lane;
	}
	public void setLane(int lane) {
		this.lane = lane;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
}
