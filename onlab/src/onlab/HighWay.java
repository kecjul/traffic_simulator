package onlab;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;

public class HighWay {
	float lenght = 0;
	int section = 0;
	ArrayList<RoadObject> roadObjects = new ArrayList<RoadObject>();

	HighWay(float lenght) {
		this.lenght = lenght;
		this.section = (int) lenght / 4;
	}

	void setLenght(float lenght) {
		this.lenght = lenght;
		this.section = (int) lenght / 4;
	}

	void addCar(float startAngle) {
		Car newCar = new Car(getPos(startAngle));
		roadObjects.add(newCar);
	}

	void addCar(Point2D.Float click, float maxSpeed, float maxAcc, Color color,
			float prefSpeed, float range, float safety) {
		Car newCar = new Car(getPos(getAngle(click)));
		newCar.maxSpeed = maxSpeed;
		newCar.maxAcc = maxAcc;
		newCar.color = color;
		newCar.driver.prefSpeed = prefSpeed;
		newCar.driver.rangeOfView = range;
		newCar.driver.safetyGap = safety;
		roadObjects.add(newCar);
	}

	void addBlock(Point2D.Float click, float duration) {
		Block newBlock = new Block(getPos(getAngle(click)));
		newBlock.duration = duration;
		roadObjects.add(newBlock);
	}

	void modifyCar(int id, float maxSpeed, float maxAcc, Color color,
			float prefSpeed, float range, float safety) {
		for (RoadObject roadObject : roadObjects) {
			if (roadObject.id == id) {
				((Car) roadObject).maxSpeed = maxSpeed;
				((Car) roadObject).maxAcc = maxAcc;
				((Car) roadObject).color = color;
				((Car) roadObject).driver.prefSpeed = prefSpeed;
				((Car) roadObject).driver.rangeOfView = range;
				((Car) roadObject).driver.safetyGap = safety;
			}
		}
	}

	void modifyBlock(int id, float duration) {
		for (RoadObject roadObject : roadObjects) {
			if (roadObject.id == id) {
				((Block) roadObject).duration = duration;
			}
		}
	}

	void deleteObject(RoadObject roadObject) {
		this.roadObjects.remove(roadObject);
	}

	void deleteObjects(ArrayList<RoadObject> roadObjects) {
		for (RoadObject roadObject : roadObjects) {
			this.roadObjects.remove(roadObject);
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
		for (RoadObject roadObject : roadObjects) {
			float angle = (getAngle(roadObject.position) + 360 - getAngle(asker.position)) % 360;
			if (angle < minAngle && asker != roadObject) {
				minAngle = angle;
				result = roadObject;
			}
		}
		if (result != null && minAngle * lenght / 360 >= asker.getRange()) {
			result = null;
		}
		return result;
	}

	public void newAngle(Car thisCar) {
		float change = thisCar.actualSpeed/100;
		float angle = getAngle(thisCar.position);
		angle += (change / lenght * 360);
		angle%=360;
		thisCar.position = getPos(angle);
	}

	public ArrayList<RoadObject> getRoadObjects() {
		return roadObjects;
	}

	public void move() {
		ArrayList<RoadObject> copyRO = roadObjects;
		ArrayList<RoadObject> deletables = new ArrayList<RoadObject>();
		for (RoadObject roadObject : copyRO) {
			if (roadObject instanceof Car) {
				Car actualCar = (Car) roadObject;
				actualCar.drive(getSight(actualCar));
				newAngle(actualCar);
			} else if (roadObject instanceof Block) {
				Block actualBlock = (Block) roadObject;
				if (actualBlock.tick()) {
					deletables.add(roadObject);
				}
			}
		}
		deleteObjects(deletables);
	}

}
