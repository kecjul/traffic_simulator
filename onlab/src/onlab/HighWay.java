package onlab;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;

public class HighWay {
	private float pixelLenght = 0;
	private float kmLenght = 5;
	private ArrayList<RoadObject> roadObjects = new ArrayList<RoadObject>();
	private Road road;
	private float timeLapse = 100;

	HighWay(float lenght) {
		this.pixelLenght = lenght;
	}

	public HighWay(Road road) {
		this.road = road;
		setLenght((float) (road.getRectPartSize()*2 + road.getOvalPartSize()*Math.PI/2));
	}

	public float getLenght() {
		return pixelLenght;
	}
	public void setLenght(float lenght) {
		this.pixelLenght = lenght;
	}

	public ArrayList<RoadObject> getRoadObjects() {
		return roadObjects;
	}
	
	public void setRoadObjects(ArrayList<RoadObject> roadObjects) {
		this.roadObjects = roadObjects;
	}

	private void addCar(float startAngle) {
		Car newCar = new Car(getPos(startAngle));
		getRoadObjects().add(newCar);
	}

	private void addCar(Point2D.Float click, float maxSpeed, float maxAcc, Color color,
			float prefSpeed, float range, float safety) {
		Car newCar = new Car(getPos(getAngle(click)), maxSpeed, maxAcc, color, prefSpeed, range, safety);
		getRoadObjects().add(newCar);
	}

	public void addBlock(Point2D.Float click, int lane, float duration) {
		Block newBlock = new Block(click, lane, duration);
		getRoadObjects().add(newBlock);
	}

	public void modifyCar(int id, float maxSpeed, float maxAcc, Color color,
			float prefSpeed, float range, float safety) {
		for (RoadObject roadObject : getRoadObjects()) {
			if (roadObject.id == id) {
				((Car) roadObject).setMaxSpeed(maxSpeed);
				((Car) roadObject).setMaxAcc(maxAcc);
				((Car) roadObject).setColor(color);
				((Car) roadObject).getDriver().setPrefSpeed(prefSpeed);
				((Car) roadObject).getDriver().setRangeOfView(range);
				((Car) roadObject).getDriver().setSafetyGap(safety);
				break;
			}
		}
	}

	public void modifyBlock(int id, float duration) {
		for (RoadObject roadObject : getRoadObjects()) {
			if (roadObject.id == id) {
				((Block) roadObject).duration = duration;
				break;
			}
		}
	}

	public void deleteObject(RoadObject roadObject) {
		this.getRoadObjects().remove(roadObject);
	}

	private void deleteObjects(ArrayList<RoadObject> roadObjects) {
		for (RoadObject roadObject : roadObjects) {
			this.getRoadObjects().remove(roadObject);
		}
	}

	public float toRadian(float angle) {
		return (float) ((angle / 180) * Math.PI);
	}

	public float toAngle(float radian) {
		return (float) (radian * (180 / Math.PI));
	}

	public float normalAngle(float angle) {
		float temp = angle + 360;
		temp = temp % 360;
		return temp;
	}

	public float square(float number) {
		return (number * number);
	}

	public float getDistance(Point2D.Float a, Point2D.Float b) {
		return (float) Math.sqrt(square(b.x - a.x) + square(b.y - a.y));
	}

	public Point2D.Float getPos(float angle) {
		float radian = toRadian(angle);
		Point2D.Float pos = new Point2D.Float();
		pos.x = (float) ((-20) + (Math.cos(radian) * 187.5));
		pos.y = (float) (20 + (Math.sin(radian) * 187.5));
		return pos;
	}

	public float getAngle(Point2D.Float position) {
		float delta_x = (float) (position.x - (-20));
		float delta_y = (float) (position.y - 20);
		float angle = (float) (Math.atan2(delta_y, delta_x));
		angle = toAngle(angle);
		angle = normalAngle(angle);
		return angle;
	}

	public RoadObject getSight(Car asker) {
		RoadObject result = null;
		float minAngle = 360;
		ArrayList<RoadObject> ros = (ArrayList<RoadObject>) getRoadObjects().clone();
		for (RoadObject roadObject : ros) {
			
			float angle = (getAngle(roadObject.getPosition()) + 360 - getAngle(asker.getPosition())) % 360;
			if (angle < minAngle && asker != roadObject) {
				minAngle = angle;
				result = roadObject;
			}
		}
		if (result != null && minAngle * pixelLenght / 360 >= asker.getRange()) {
			result = null;
		}
		return result;
	}

	public void newAngle(Car thisCar) {
		// /100 because of timing is triggered 100 times a second and *360 because speed is in km/h not km/sec
		float change = thisCar.getActualSpeed()/100*360;
		float angle = getAngle(thisCar.getPosition());
		angle += (change / pixelLenght * 360);
		angle%=360;
		thisCar.setPosition(getPos(angle));
	}




	public String getType(int i){
		String type = null;
		for (RoadObject roadObject : getRoadObjects()) {
			if (i == (roadObject.id)) {
				if (roadObject instanceof Car) {
					type = "Car";
				} else if (roadObject instanceof Block) {
					type = "Block";
				}
			}
		}
		return type;
	}
	public String getStatus(int i) {
		String status = null;
		for (RoadObject roadObject : getRoadObjects()) {
			if (i == (roadObject.id)) {
				if (roadObject instanceof Car) {
					Car actualCar = (Car) roadObject;
					status = actualCar.getDriver().s.toString();
				} else if (roadObject instanceof Block) {
					Block actualBlock = (Block) roadObject;
					status = Float.toString(actualBlock.duration);
				}
				break;
			}
		}
		return status;
	}
	public String getSpeed(int i){
		String speed = null;
		for (RoadObject roadObject : getRoadObjects()) {
			if (i == (roadObject.id)) {
				if (roadObject instanceof Car) {
					Car actualCar = (Car) roadObject;
					speed = Float.toString(actualCar.getActualSpeed());
				}else{
					System.out.println("Blocks don't have speed!");
				}
				break;
			}
		}
		return speed;
	}

	public void newCar() {
		getRoadObjects().add(new Car(road.getStartPosition()));
	}

	private void newCarPosition(Car actualCar) {
		Point2D.Float newPosition;
		if(actualCar.getPosition().x < road.getBorderPoint()){
			newPosition = ovalPosition(actualCar.getPosition(), actualCar.getAdvance(timeLapse));
		} else {
			newPosition = rectPosition(actualCar.getPosition(), actualCar.getAdvance(timeLapse));
		}
		actualCar.setPosition(newPosition);
	}
	private Point2D.Float ovalPosition(Point2D.Float position, float advance){
		Point2D.Float newPosition = null;
		//TODO
		return newPosition;
	}
	
	private Point2D.Float rectPosition(Point2D.Float position, float advance){
		Point2D.Float newPosition = null;
		float pixelAdvance = kmToPixel(advance);
		if(position.y < (road.getOvalPartSize()/2 + road.getBorderSize())){
			pixelAdvance = pixelAdvance * -1;
		}
		newPosition = new Point2D.Float(position.x + pixelAdvance, position.y);
		return newPosition;
	}
	
	private float kmToPixel(float advance){
		return pixelLenght/kmLenght * advance;
	}

	public void move() {
		ArrayList<RoadObject> ros = (ArrayList<RoadObject>) getRoadObjects().clone();
		ArrayList<RoadObject> deletables = new ArrayList<RoadObject>();
		for (RoadObject roadObject : ros) {
			if (roadObject instanceof Car) {
				Car actualCar = (Car) roadObject;
				RoadObject carInSight = null; //getSight(actualCar);
				actualCar.drive(getSight(actualCar));
				newCarPosition(actualCar);
//				newAngle(actualCar);
			} else if (roadObject instanceof Block) {
				Block actualBlock = (Block) roadObject;
				if (actualBlock.tick()) {
					deletables.add(roadObject);
				}
			}
		}
		deleteObjects(deletables);
	}

	public float getTimeLapse() {
		return timeLapse;
	}

	public void setTimeLapse(float timeLapse) {
		this.timeLapse = timeLapse;
	}
}
