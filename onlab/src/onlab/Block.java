package onlab;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;


public class Block extends RoadObject{
	
	float duration = 20;
	float currentTime = 0;
	
	Block(){
		setSize(Surface.blockSize);
	}
	
	Block(Point2D.Float startPosition){
		this.setPosition(startPosition);
		count++;
		id = count;	
		setSize(Surface.blockSize);
		currentTime = 0;
	}
	
	public Block(Float startPosition, int lane, float duration) {
		count++;
		id = count;	
		setSize(Surface.blockSize);
		this.setPosition(startPosition);
		this.setLane(lane);
		currentTime = 0;
		this.duration = duration;
	}

	public boolean tick(){
		currentTime += Controller.tickTime;
		if (currentTime / 1000 > duration){
			return true;
		} 
		return false;
	}
}
