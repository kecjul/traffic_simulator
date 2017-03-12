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

	public float getTimeLapse() {
		return timeLapse;
	}

	public void setTimeLapse(float timeLapse) {
		this.timeLapse = timeLapse;
	}

	/* 
	 * WINDOW
	 */
	
	public void deleteObject(RoadObject roadObject) {
		this.getRoadObjects().remove(roadObject);
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
	
	/* 
	 * LOG
	 */
	
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
					Car thisCar = (Car) roadObject;
					status = thisCar.getDriver().s.toString();
				} else if (roadObject instanceof Block) {
					Block thisBlock = (Block) roadObject;
					status = Float.toString(thisBlock.duration);
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
					Car thisCar = (Car) roadObject;
					speed = Float.toString(thisCar.getCurrentSpeed());
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

	/* 
	 * MOVE
	 */	
	
	@SuppressWarnings("unchecked")
	public void move() {
		ArrayList<RoadObject> ros = (ArrayList<RoadObject>) getRoadObjects().clone();
		ArrayList<RoadObject> deletables = new ArrayList<RoadObject>();
		for (RoadObject roadObject : ros) {
			if (roadObject instanceof Car) {
				Car thisCar = (Car) roadObject;
				RoadObject carInSight = getSight(thisCar);
				thisCar.drive(carInSight);
				newCarPosition(thisCar);
			} else if (roadObject instanceof Block) {
				Block thisBlock = (Block) roadObject;
				if (thisBlock.tick()) {
					deletables.add(roadObject);
				}
			}
		}
		deleteObjects(deletables);
	}

	private RoadObject getSight(Car asker) {
		//TODO
		RoadObject result = null;
		if(asker.getPosition().x < road.getOvalPartSize()/2 + road.getBorderSize()){
			result = getSightInOvalPart(asker);
		} else {
			result = getSightInRectPart(asker);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private RoadObject getSightInOvalPart(Car asker) {
		RoadObject result = null;
		float minAngle = 360;
		ArrayList<RoadObject> ros = (ArrayList<RoadObject>) getRoadObjects().clone();
		for (RoadObject roadObject : ros) {			
			float angle = (getAngle(roadObject.getPosition(), asker.getLane()) + 360 - getAngle(asker.getPosition(), asker.getLane())) % 360;
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

	@SuppressWarnings("unchecked")
	private RoadObject getSightInRectPart(Car asker) {
		RoadObject result = null;
		float minDistance = road.getRectPartSize();
		ArrayList<RoadObject> ros = (ArrayList<RoadObject>) getRoadObjects().clone();
		for (RoadObject roadObject : ros) {
			if(isROInUpperRectPart(asker) && isROInUpperRectPart(roadObject) 
					&& isROOnLeftSide(asker, roadObject)){				
				float distance = Util.getDistance(asker.getPosition(), roadObject.getPosition());
				if(distance < minDistance){
					minDistance = distance;
					result = roadObject;
				}				
			} else if(!isROInUpperRectPart(asker) && !isROInUpperRectPart(roadObject)
					&& !isROOnLeftSide(asker, roadObject)){
				float distance = Util.getDistance(asker.getPosition(), roadObject.getPosition());
				if(distance < minDistance){
					minDistance = distance;
					result = roadObject;
				}
			}					
		} 		
		return result;
	}
	
	private void newCarPosition(Car thisCar) {
		Point2D.Float newPosition;
		if(thisCar.getPosition().x < road.getBorderPoint()){
			newPosition = ovalPosition(thisCar.getPosition(), thisCar.getAdvance(timeLapse), thisCar.getLane());
		} else {
			newPosition = rectPosition(thisCar.getPosition(), thisCar.getAdvance(timeLapse), thisCar.getLane());
		}
		thisCar.setPosition(newPosition);
	}

	private Point2D.Float ovalPosition(Point2D.Float position, Float advance, int lane){	
		float angle = getAngle(position, lane);
		angle += getAngleFromAdvance(kmToPixel(advance), lane);
		angle%=360;
		return getPos(angle, lane);
	}

	private Point2D.Float rectPosition(Point2D.Float position, float advance, int lane){
		Point2D.Float newPosition = null;
		float pixelAdvance = kmToPixel(advance);
		if(isPosInUpperRectPart(position)){
			pixelAdvance = pixelAdvance * -1;
		}
		newPosition = new Point2D.Float(position.x + pixelAdvance, getRectY(position, lane));
		return newPosition;
	}
	
	//---------//

	private float getAngle(Point2D.Float position, int lane) {
		Point2D.Float circlePosition = circleCenteredCoords(getCirclePoint(position));
		float angle = (float) (Math.atan2(circlePosition.y, circlePosition.x));
		angle = Util.toAngle(angle);
		angle = Util.normalAngle(angle);
		return angle;
	}

	private Point2D.Float getPos(float angle, int lane) {
		float radian = Util.toRadian(angle);
		Point2D.Float pos = new Point2D.Float();
		pos.x = (float) ((Math.cos(radian) * road.getOvalPartSize()/2));
		pos.y = (float) ((Math.sin(radian) * road.getOvalPartSize()/2));
		return getCirclePoint(normalCoords(pos), lane);
	}

	private Point2D.Float circleCenteredCoords(Point2D.Float position) {
		return new Point2D.Float(position.x - road.getBorderSize() - road.getOvalPartSize()/2,
				(road.getOvalPartSize()/2) - (position.y - road.getBorderSize()));
	}
	
	private Point2D.Float normalCoords(Point2D.Float circlePosition) {
		return new Point2D.Float(circlePosition.x + road.getOvalPartSize()/2 + road.getBorderSize(), 
				road.getOvalPartSize()/2 - circlePosition.y + road.getBorderSize());
	}
	
	private Point2D.Float getCirclePoint(Point2D.Float newPoint) {
		float r = road.getOvalPartSize()/2;
		float x = road.getCircleCenter().x + r*(newPoint.x-road.getCircleCenter().x)/Util.getDistance(newPoint,road.getCircleCenter());
		float y = road.getCircleCenter().y + r*(newPoint.y-road.getCircleCenter().y)/Util.getDistance(newPoint,road.getCircleCenter());
		
		Point2D.Float circlePoint = new Point2D.Float(x, y);		
		return circlePoint;
	}
	
	private Point2D.Float getCirclePoint(Point2D.Float newPoint, int lane) {
		float r = getDiameter(lane)/2;
		float x = road.getCircleCenter().x + r*(newPoint.x-road.getCircleCenter().x)/Util.getDistance(newPoint,road.getCircleCenter());
		float y = road.getCircleCenter().y + r*(newPoint.y-road.getCircleCenter().y)/Util.getDistance(newPoint,road.getCircleCenter());
		
		Point2D.Float circlePoint = new Point2D.Float(x, y);		
		return circlePoint;
	}

	private float getDiameter(int lane) {
		return road.getOvalPartSize() - (lane-1+0.5f)*road.getLaneSize()*2;
	}
	
	private float getAngleFromAdvance(Float advance, int lane) {
		return advance / (getOvalPartRoadLength(lane)*2) * 360;
	}
	
	private float getOvalPartRoadLength(int lane){
		float d = getDiameter(lane);
		return (float) (d*Math.PI)/2;
	}

	//------------//
	
	private boolean isROInUpperRectPart(RoadObject asker) {
		if(asker.getPosition().y < (road.getOvalPartSize()/2 + road.getBorderSize())){
			return true;
		}
		return false;
	}
	
	private boolean isROOnLeftSide(Car asker, RoadObject roadObject) {
		if(asker.getPosition().x > roadObject.getPosition().x){
			return true;
		}
		return false;
	}

	private boolean isPosInUpperRectPart(Point2D.Float position) {
		if(position.y < (road.getOvalPartSize()/2 + road.getBorderSize())){
			return true;
		}
		return false;
	}
	
	private float getRectY(Point2D.Float position, int lane) {
		float result = 0;
		if(isPosInUpperRectPart(position)){
			result = (road.getBorderSize() + (lane-1)*road.getLaneSize() + road.getLaneSize()/2);
		} else {
			result = (road.getBorderSize() + road.getOvalPartSize() - (lane-1)*road.getLaneSize() - road.getLaneSize()/2);
		}
		return result;
	}
	
	private float kmToPixel(float advance){
		return pixelLenght/kmLenght * advance;
	}
	
	private void deleteObjects(ArrayList<RoadObject> roadObjects) {
		for (RoadObject roadObject : roadObjects) {
			this.getRoadObjects().remove(roadObject);
		}
	}
}
