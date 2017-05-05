package onlab;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;
import java.util.ArrayList;

import javax.swing.JPanel;


public class Surface extends JPanel{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ArrayList<RoadObject> roadObjects = new ArrayList<RoadObject>();
	private static int laneCount = 3;
	
	float size = 450;
	static int carSize = 17;
	static int blockSize = 15;
	Color grass = new Color(0, 100, 0);
	Color concrete = new Color(100, 100, 100);
	
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	int screenWidth = screenSize.width;
	int screenHeight = screenSize.height;

	int maxWidth = screenWidth - 280;
	int maxHeight = screenHeight - 250;
	
	int firstBorder = 25;
	int laneSize = 25;
	
	int firstOvalPartSize = maxHeight - firstBorder*2;
	int firstRectPartSize = maxWidth - firstOvalPartSize/2-firstBorder;
	
	int ovalBorder = firstBorder + firstOvalPartSize/2;

	Point2D.Float circleCenter = new Float(firstBorder + firstOvalPartSize/2, firstBorder + firstOvalPartSize/2);
	
	private void doDrawing(Graphics g) {
		drawLanes(g);
		Graphics2D g2d = (Graphics2D) g;
		drawRoadObjects(g, g2d);
		
	}
	
	private void drawRoadObjects(Graphics g, Graphics2D g2d) {
		for (RoadObject roadObject : roadObjects) {
			Point2D.Float pos = roadObject.getPosition();
    		FontMetrics fm = g.getFontMetrics();
            double textWidth = fm.getStringBounds(Integer.toString(roadObject.id), g).getWidth()/2.2;
			if(roadObject instanceof Car){
				g2d.setColor(((Car) roadObject).getColor()); 
        		g2d.fillOval((int)pos.getX() - carSize/2, (int)pos.getY() - carSize/2, carSize, carSize);
        		
        		if(Color.BLUE.equals(((Car) roadObject).getColor()))
					g2d.setColor(new Color(255, 255, 255)); 
        		else
					g2d.setColor(new Color(0, 0, 0)); 
        		g2d.drawString(Integer.toString(roadObject.id), (int)(pos.getX()- textWidth), (int)(pos.getY()+ fm.getMaxAscent() / 2));
			}else if(roadObject instanceof Block){
				g2d.setColor(new Color(0, 0, 0));
        		g2d.fillOval((int)pos.getX() - blockSize/2, (int)pos.getY() - blockSize/2, blockSize, blockSize);
        		
				g2d.setColor(new Color(255, 255, 255)); 
        		g2d.drawString(Integer.toString(roadObject.id), (int)(pos.getX()- textWidth), (int)(pos.getY()+ fm.getMaxAscent() / 2));
			
			} else{
				System.out.println("invalid object");
			}
		}
	}
	
	private void drawLanes(Graphics g){
		Graphics2D g2d = (Graphics2D) g;
		Graphics2D g2dDashed = (Graphics2D) g;
		
		Stroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, 
				BasicStroke.JOIN_MITER, 10.0f, new float[]{10}, 0.0f);
		g2dDashed.setStroke(dashed);
		g2dDashed.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		        RenderingHints.VALUE_ANTIALIAS_ON);

		//1 sáv
		
		g2d.setColor(grass);
		g2d.fillRect(0, 0, maxWidth, maxHeight);
		g2d.setColor(Color.BLACK);
		g2d.fillOval(firstBorder-1, firstBorder-1, firstOvalPartSize+2, firstOvalPartSize+2);

		g2d.setColor(concrete);
		g2d.fillOval(firstBorder, firstBorder, firstOvalPartSize, firstOvalPartSize);

		g2d.setColor(Color.BLACK);
		g2d.fillRect(firstOvalPartSize/2+firstBorder, firstBorder-1, firstRectPartSize, firstOvalPartSize+2);
		
		g2d.setColor(concrete);
		g2d.fillRect(firstOvalPartSize/2+firstBorder, firstBorder, firstRectPartSize, firstOvalPartSize);
		if(getLaneCount() == 1){
			g2d.setColor(Color.BLACK);
			g2d.fillOval(firstBorder+laneSize-1, firstBorder+laneSize-1, firstOvalPartSize-laneSize*2+2, firstOvalPartSize-laneSize*2+2);	
			
			g2d.setColor(grass);
			g2d.fillOval(firstBorder+laneSize, firstBorder+laneSize, firstOvalPartSize-laneSize*2, firstOvalPartSize-laneSize*2);
			
			g2d.setColor(Color.BLACK);
			g2d.fillRect(firstOvalPartSize/2+firstBorder, firstBorder+laneSize-1, firstRectPartSize, firstOvalPartSize-laneSize*2+2);	

			g2d.setColor(grass);
			g2d.fillRect(firstOvalPartSize/2+firstBorder, firstBorder+laneSize, firstRectPartSize, firstOvalPartSize-laneSize*2);
		} else {
			for (int i = 1; i < getLaneCount(); i++) {
				int border = firstBorder + i*laneSize;
				int ovalPartSize = firstOvalPartSize - i*(laneSize*2);
				int rectPartSize = firstRectPartSize;
	
				g2dDashed.setColor(Color.WHITE);
				g2dDashed.draw(new Ellipse2D.Double(new Double(border-1), new Double(border-1)
						, new Double((ovalPartSize+1)), new Double((ovalPartSize+1))));
	
				g2d.setColor(concrete);
				g2d.fillOval(border, border, ovalPartSize, ovalPartSize);
				
				if(i == getLaneCount()-1){
					g2d.setColor(Color.BLACK);
					g2d.fillOval(border+laneSize-1, border+laneSize-1, ovalPartSize-laneSize*2+2, ovalPartSize-laneSize*2+2);
					
					g2d.setColor(grass);
					g2d.fillOval(border+laneSize, border+laneSize, ovalPartSize-laneSize*2, ovalPartSize-laneSize*2);					
				}
	
				g2dDashed.setColor(Color.WHITE);
				g2dDashed.draw(new Rectangle(ovalPartSize/2+border, border-1, rectPartSize, ovalPartSize+2));
				
				g2d.setColor(concrete);
				g2d.fillRect(ovalPartSize/2+border, border, rectPartSize, ovalPartSize);
				if(i == getLaneCount()-1){
					g2d.setColor(Color.BLACK);
					g2d.fillRect(ovalPartSize/2+border, border+laneSize-1, rectPartSize, ovalPartSize-laneSize*2+2);	

					g2d.setColor(grass);
					g2d.fillRect(ovalPartSize/2+border, border+laneSize, rectPartSize, ovalPartSize-laneSize*2);
				}
			}
		}
		
	}
	
	public void setRoadObjects(ArrayList<RoadObject> ro){
		roadObjects = ro;
	}
	
	public Point2D.Float convertCoords(Point2D.Float position){
		return new Point2D.Float(position.x + (size/2), size - (position.y + (size/2)));
	}
	
	public int getNewObjectsLane(Point2D.Float newPoint) {
		int actLane = 0;
		float x = newPoint.x;
		if( x < ovalBorder){
			actLane = ovalLane(newPoint);
		} else {
			actLane = rectLane(newPoint);				
		}
		return actLane;						
	}
	
	private int ovalLane(Point2D.Float newPoint){
		int actLane = 0; 
		int i = getLaneCount();
		while(actLane == 0){
			if(isInsideCircle(newPoint, i-1)){
				actLane = i;
			}
			if( i > 1){
				i--;
			} else {
				actLane = 1;
			}
		}
		return actLane;
	}
	
	private int rectLane(Point2D.Float newPoint){
		int actLane = 0; 
		int i = 0;
		float y = newPoint.y;
		while(actLane == 0){
			if( y < firstBorder + (i+1) * laneSize || y > firstOvalPartSize + firstBorder - (i+1) * laneSize){
				actLane = i+1;
			} 
			if( i < getLaneCount()-1){
				i++;
			} else {
				actLane = getLaneCount();
			}
		}
		return actLane;
	}
	
	public Point2D.Float getRoadPoint(Point2D.Float newPoint, int actLane) {
		Point2D.Float roadPoint = null;
		float x = newPoint.x;
		float y = newPoint.y;
		if( x < ovalBorder){
			roadPoint = getCirclePoint(newPoint, actLane);
		} else {
			if( y < firstBorder + firstOvalPartSize/2){
				roadPoint = new Point2D.Float(x, (firstBorder + (actLane-1)*laneSize + laneSize/2));
			} else {
				roadPoint = new Point2D.Float(x, (firstBorder + firstOvalPartSize - (actLane-1)*laneSize - laneSize/2));
			}
		}
		return roadPoint;
	}

	public RoadObject getObject(Point2D.Float newPoint){
		for (RoadObject roadObject : roadObjects) {
			if(roadObject instanceof Block && getDistance(newPoint, roadObject.getPosition()) < blockSize){
				return roadObject;
			}else if(roadObject instanceof Car && getDistance(newPoint, roadObject.getPosition()) < carSize){
				return roadObject;
			}
		}	
		return null;
	}
	
	private Point2D.Float getCirclePoint(Float newPoint, int actLane) {
		int r = firstOvalPartSize/2 - (actLane-1) * laneSize - laneSize/2;
		float x = circleCenter.x + r*(newPoint.x-circleCenter.x)/getDistance(newPoint,circleCenter);
		float y = circleCenter.y + r*(newPoint.y-circleCenter.y)/getDistance(newPoint,circleCenter);
		
		Point2D.Float circlePoint = new Float(x, y);		
		return circlePoint;
	}
		
	private boolean isInsideCircle(Float newPoint, int i) {
		float distance = getDistance(newPoint, circleCenter);
		if (distance < firstOvalPartSize/2 - i*laneSize){
			return true;
		} else {
			return false;
		}
	}
	
	private float getDistance(Point2D.Float a, Point2D.Float b){
		return (float) Math.sqrt(square(b.x-a.x)+square(b.y-a.y));
	}
	
	private float square(float number){
		return (number*number);
	}
	

	public static int getLaneCount() {
		return laneCount;
	}
	public static void setLaneCount(int laneCount) {
		if(laneCount  > 0 && laneCount <= 6){
			Surface.laneCount = laneCount;
		}
	}
	
	public Point2D.Float getStartPosition() {
		Point2D.Float point = new Point2D.Float(maxWidth, firstBorder+laneSize/2);
		return point;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		doDrawing(g);
	}
	public void reFresh(){
	    repaint();
	}
}
