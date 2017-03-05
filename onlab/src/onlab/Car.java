package onlab;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import onlab.Driver.Status;

public class Car extends RoadObject{
	private float maxSpeed = 50;
	private float maxAcc = 2;
	private Color color = Color.GREEN;
	private float actualSpeed;
	private Driver driver;
	private LinkedList<RoadObject> sights = new LinkedList<RoadObject>();
	private LinkedList<Float> times = new LinkedList<Float>();
	
	Car(){setDriver(new Driver());}
	
	Car(Point2D.Float startPosition){
		setPosition(startPosition);
		setDriver(new Driver());
		count++;
		id = count;
		setActualSpeed(100);
	}
	
	Car(Point2D.Float startPosition, float maxSpeed, float maxAcc, Color color, float prefSpeed, float range, float safety){
		count++;
		this.id = count;
		this.setPosition(startPosition);
		this.setMaxSpeed(maxSpeed);
		this.setMaxAcc(maxAcc);
		this.setColor(color);
		this.setActualSpeed(0);
		this.setDriver(new Driver(prefSpeed, range, safety));
	}
	
	public void drive(RoadObject inSight) {
		float acc = 0;
		getSights().add(inSight);
		getTimes().add((float) System.nanoTime());
		if ((getTimes().peekLast() - getTimes().peekFirst()) > (getDriver().getReactionTime() * 1000000000)) {
			getTimes().pop();
			acc = getDriver().drive(getSights().pop(), this);
		}
		// 1 because this get called 100 times a sec
		if (acc > 1/getMaxAcc()) {
			acc = 1/getMaxAcc();
		}
		
		setActualSpeed(getActualSpeed() + acc);
		
		if (getActualSpeed() <= 0.0001){
			setActualSpeed(0);
			getDriver().s = Status.STOPPED;
		}
		if (getActualSpeed() > getMaxSpeed()){
			setActualSpeed(getMaxSpeed());
			getDriver().s = Status.DRIVING;
		}		
	}

	public float getAdvance(float timeSpeed){
		double tenMsInHour =  10d/1000/60/60 * timeSpeed;
		return (float) (getActualSpeed() * tenMsInHour);
	}

	float getRange(){
		return getDriver().getRangeOfView();
	}

	public float getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(float maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public float getMaxAcc() {
		return maxAcc;
	}

	public void setMaxAcc(float maxAcc) {
		this.maxAcc = maxAcc;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public float getActualSpeed() {
		return actualSpeed;
	}

	public void setActualSpeed(float actualSpeed) {
		this.actualSpeed = actualSpeed;
	}

	public Driver getDriver() {
		return driver;
	}

	public void setDriver(Driver driver) {
		this.driver = driver;
	}

	public LinkedList<RoadObject> getSights() {
		return sights;
	}

	public void setSights(LinkedList<RoadObject> sights) {
		this.sights = sights;
	}

	public LinkedList<Float> getTimes() {
		return times;
	}

	public void setTimes(LinkedList<Float> times) {
		this.times = times;
	}
}
