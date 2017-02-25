package onlab;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.LinkedList;

public class Car extends RoadObject{
	float maxSpeed = 50;
	float maxAcc = 2;
	Color color = Color.GREEN;
	float actualSpeed;
	Driver driver;
	LinkedList<RoadObject> sights = new LinkedList<RoadObject>();
	LinkedList<Float> times = new LinkedList<Float>();
	
	Car(){driver = new Driver();}
	
	Car(Point2D.Float startPosition){
		this.position = startPosition;
		driver = new Driver();
		count++;
		id = count;
		actualSpeed = 0;
	}
	
	void drive(RoadObject inSight){
		float acc = 0;
		sights.add(inSight);
		times.add((float)System.nanoTime());
		if((times.peekLast() - times.peekFirst()) > (driver.reactionTime*1000000000)){
			times.pop();
			acc = driver.drive(sights.pop(), this);
		}else{
			acc = driver.drive(null, this);
		}
			if(acc > maxAcc){
				acc = maxAcc;
			}
			actualSpeed += acc;
			if(actualSpeed<0)
				actualSpeed = 0;
			if (actualSpeed > maxSpeed)
				actualSpeed = maxSpeed;
	}

	float getRange(){
		return driver.rangeOfView;
	}
}
