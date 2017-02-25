package onlab;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import onlab.Driver.Status;

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
	
	Car(Point2D.Float startPosition, float maxSpeed, float maxAcc, Color color, float prefSpeed, float range, float safety){
		count++;
		this.id = count;
		this.position = startPosition;
		this.maxSpeed = maxSpeed;
		this.maxAcc = maxAcc;
		this.color = color;
		this.actualSpeed = 0;
		this.driver = new Driver(prefSpeed, range, safety);
	}
	
	void drive(RoadObject inSight) {
		float acc = 0;
		sights.add(inSight);
		times.add((float) System.nanoTime());
		if ((times.peekLast() - times.peekFirst()) > (driver.reactionTime * 1000000000)) {
			times.pop();
			acc = driver.drive(sights.pop(), this);
		}
		// 1 because this get called 100 times a sec
		if (acc > 1/maxAcc) {
			acc = 1/maxAcc;
		}
		
		actualSpeed += acc;
		
		if (actualSpeed <= 0.0001){
			actualSpeed = 0;
			driver.s = Status.STOPPED;
		}
		if (actualSpeed > maxSpeed){
			actualSpeed = maxSpeed;
			driver.s = Status.DRIVING;
		}
	}

	float getRange(){
		return driver.rangeOfView;
	}
}
