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



import java.util.HashMap;
import java.util.Set;

import javax.swing.JPanel;

import javafx.scene.shape.Ellipse;

public class Surface extends JPanel{
	ArrayList<RoadObject> roadObjects = new ArrayList<RoadObject>();
	private static int laneCount = 2;
	
	float size = 450;
	static int carSize = 20;
	static int blockSize = 15;
	Color grass = new Color(0, 100, 0);
	Color concrete = new Color(100, 100, 100);
	
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	int screenWidth = screenSize.width;
	int screenHeight = screenSize.height;

	int maxWidth = screenWidth - 280;
	int maxHeight = screenHeight - 200;
	
	int firstBorder = 25;
	int lane = 25;
	int ovalBorder = (maxHeight - firstBorder*2)/2;
	
	int firstOvalPartSize = maxHeight - firstBorder*2;
	int firstRectPartSize = maxWidth - firstOvalPartSize/2-firstBorder;
	

	Point2D.Float centre = new Float(firstBorder + firstOvalPartSize/2, firstBorder + firstOvalPartSize/2);
	
	private void doDrawing(Graphics g) {
		drawLanes(g);
		Graphics2D g2d = (Graphics2D) g;
		
		for (RoadObject roadObject : roadObjects) {
			Point2D.Float pos = roadObject.position;
    		FontMetrics fm = g.getFontMetrics();
            double textWidth = fm.getStringBounds(Integer.toString(roadObject.id), g).getWidth();
			if(roadObject instanceof Car){
				g2d.setColor(((Car) roadObject).color); 
        		g2d.fillOval((int)pos.getX() - carSize/2, (int)pos.getY() - carSize/2, carSize, carSize);
        		
        		if(((Car) roadObject).color == Color.BLUE)
					g2d.setColor(new Color(255, 255, 255)); 
        		else
					g2d.setColor(new Color(0, 0, 0)); 
        		g2d.drawString(Integer.toString(roadObject.id), (int)(pos.getX()- textWidth/2), (int)(pos.getY()+ fm.getMaxAscent() / 2));
			}else if(roadObject instanceof Block){
				g2d.setColor(new Color(255, 0, 0));
        		g2d.fillOval((int)pos.getX() - blockSize/2, (int)pos.getY() - blockSize/2, blockSize, blockSize);
        		
				g2d.setColor(new Color(0, 0, 0)); 
        		g2d.drawString(Integer.toString(roadObject.id), (int)(pos.getX()- textWidth/2), (int)(pos.getY()+ fm.getMaxAscent() / 2));
			
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
			g2d.fillOval(firstBorder+lane-1, firstBorder+lane-1, firstOvalPartSize-lane*2+2, firstOvalPartSize-lane*2+2);	
			
			g2d.setColor(grass);
			g2d.fillOval(firstBorder+lane, firstBorder+lane, firstOvalPartSize-lane*2, firstOvalPartSize-lane*2);
			
			g2d.setColor(Color.BLACK);
			g2d.fillRect(firstOvalPartSize/2+firstBorder, firstBorder+lane-1, firstRectPartSize, firstOvalPartSize-lane*2+2);	

			g2d.setColor(grass);
			g2d.fillRect(firstOvalPartSize/2+firstBorder, firstBorder+lane, firstRectPartSize, firstOvalPartSize-lane*2);
		} else {
			for (int i = 1; i < getLaneCount(); i++) {
				int border = firstBorder + i*lane;
				int ovalPartSize = firstOvalPartSize - i*(lane*2);
				int rectPartSize = firstRectPartSize;
	
				g2dDashed.setColor(Color.WHITE);
				g2dDashed.draw(new Ellipse2D.Double(new Double(border-1), new Double(border-1)
						, new Double((ovalPartSize+1)), new Double((ovalPartSize+1))));
	
				g2d.setColor(concrete);
				g2d.fillOval(border, border, ovalPartSize, ovalPartSize);
				
				if(i == getLaneCount()-1){
					g2d.setColor(Color.BLACK);
					g2d.fillOval(border+lane-1, border+lane-1, ovalPartSize-lane*2+2, ovalPartSize-lane*2+2);
					
					g2d.setColor(grass);
					g2d.fillOval(border+lane, border+lane, ovalPartSize-lane*2, ovalPartSize-lane*2);					
				}
	
				g2dDashed.setColor(Color.WHITE);
				g2dDashed.draw(new Rectangle(ovalPartSize/2+border, border-1, rectPartSize, ovalPartSize+2));
				
				g2d.setColor(concrete);
				g2d.fillRect(ovalPartSize/2+border, border, rectPartSize, ovalPartSize);
				if(i == getLaneCount()-1){
					g2d.setColor(Color.BLACK);
					g2d.fillRect(ovalPartSize/2+border, border+lane-1, rectPartSize, ovalPartSize-lane*2+2);	

					g2d.setColor(grass);
					g2d.fillRect(ovalPartSize/2+border, border+lane, rectPartSize, ovalPartSize-lane*2);
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
	
	public int getLane(Point2D.Float newPoint) {
		int actLane = 0;
		double x = newPoint.getX();
		double y = newPoint.getY();
		if( x < ovalBorder){
			//TODO
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
		} else {
			int i = 0;
			while(actLane == 0){
				if( y < firstBorder + (i+1) * lane || y > firstOvalPartSize + firstBorder - (i+1) * lane){
					actLane = i+1;
				} 
				if( i < getLaneCount()-1){
					i++;
				} else {
					actLane = getLaneCount();
				}
			}
		}
		return actLane;						
	}
	
	public Point2D.Float getRoadPoint(Point2D.Float newPoint, int actLane) {
		Point2D.Float roadPoint = null;
		float x = newPoint.x;
		float y = newPoint.y;
		if( x < ovalBorder){
			//TODO
			roadPoint = getCirclePoint(newPoint, actLane);
		} else {
			if( y < firstBorder + firstOvalPartSize/2){
				roadPoint = new Point2D.Float(x, (firstBorder + (actLane-1)*lane + lane/2));
			} else {
				roadPoint = new Point2D.Float(x, (firstBorder + firstOvalPartSize - (actLane-1)*lane - lane/2));
			}
		}
		
		return roadPoint;
	}

	public RoadObject getObject(Point2D.Float newPoint){
		for (RoadObject roadObject : roadObjects) {
			if(roadObject instanceof Block && getDistance(newPoint, roadObject.position) < blockSize){
				return roadObject;
			}else if(roadObject instanceof Car && getDistance(newPoint, roadObject.position) < carSize){
				return roadObject;
			}
		}	
		return null;
	}
	
	private Point2D.Float getCirclePoint(Float newPoint, int actLane) {
		int r = firstOvalPartSize/2 - (actLane-1) * lane - lane/2;
		float x = centre.x + r*(newPoint.x-centre.x)/getDistance(newPoint,centre);
		float y = centre.y + r*(newPoint.y-centre.y)/getDistance(newPoint,centre);
		
		Point2D.Float circlePoint = new Float(x, y);		
		return circlePoint;
	}
		
	private boolean isInsideCircle(Float newPoint, int i) {
		float distance = getDistance(newPoint, centre);
		if (distance < firstOvalPartSize/2 - i*lane){
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
		if(laneCount  > 0 && laneCount < 6){
			Surface.laneCount = laneCount;
		}
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
