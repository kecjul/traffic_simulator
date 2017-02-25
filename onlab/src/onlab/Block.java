package onlab;

import java.awt.geom.Point2D;


public class Block extends RoadObject{
	
	float duration = 20;
	float currentTime = 0;
	
	Block(){}
	
	Block(Point2D.Float startPosition){
		this.position = startPosition;
		count++;
		id = count;	
		currentTime = System.nanoTime() / 1000000000;
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
