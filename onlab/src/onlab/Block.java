package onlab;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;


public class Block extends RoadObject{
	
	float duration = 20;
	float currentTime = 0;
	
	Block(){
		setSize(10);
	}
	
	Block(Point2D.Float startPosition){
		this.setPosition(startPosition);
		count++;
		id = count;	
		setSize(10);
		currentTime = System.nanoTime() / 1000000000;
	}
	
	public Block(Float startPosition, int lane, float duration) {
		count++;
		id = count;	
		setSize(10);
		this.setPosition(startPosition);
		this.setLane(lane);
		currentTime = System.nanoTime() / 1000000000;
		this.duration = duration;
	}

	public boolean tick(){
		float lastTime = currentTime;
		currentTime = System.nanoTime() / 1000000000;
		float delta = currentTime - lastTime;
		if(delta<=1)
			duration -= delta;
		if (duration <= 0){
			return true;
		}
		return false;
	}
}
