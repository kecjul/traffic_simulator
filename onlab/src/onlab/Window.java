package onlab;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import onlab.Controller.Status;

public class Window extends JFrame implements ActionListener, MouseListener {
	Controller c;
	Surface surface;
	JPanel settings, combo, options, car, block, highWay, select, scar, sblock;
	JLabel newObjectTitle, carTitle, driverTitle, blockTitle, aditional, selectedTitle, selectedCarObject, selectedBlockObject;
	JLabel lcarMaxSpeed, lcarMaxAcc, lcarColor, ldriverPrefSpeed, ldriverRange, ldriverSafety, lblockDuration, lhwLenght;
	JTextField tcarMaxSpeed, tcarMaxAcc, tdriverPrefSpeed, tdriverRange, tdriverSafety, tblockDuration, thwLenght;
	JLabel  slcarMaxSpeed, slcarMaxAcc, slcarColor, sldriverPrefSpeed, sldriverRange, sldriverSafety, slblockDuration, slhwLenght;
	JTextField stcarMaxSpeed, stcarMaxAcc, stdriverPrefSpeed, stdriverRange, stdriverSafety, stblockDuration, sthwLenght;
	JComboBox typeChooser , colorChooser, scolorChooser;
	JButton bPause, bSave, bLoad, bcAccept, bbAccept, bcDel, bbDel;
	JSplitPane settingsSplit, selectSplit; 
	final static String CARPANEL = "Car";
	final static String BLOCKPANEL = "Road Block";
	float size = 450;
	String[] types = {CARPANEL, BLOCKPANEL};
	String[] colorsS = {"White", "Yellow", "Green", "Cyan", "Blue", "Pink", "Orange"};
	Color[] colorsC = {Color.WHITE ,Color.YELLOW ,Color.GREEN ,Color.CYAN ,Color.BLUE ,Color.PINK ,Color.ORANGE};
	RoadObject selected;

	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	int screenWidth = screenSize.width;
	int screenHeight = screenSize.height;
		
	public Window(Controller c) {
		initUI();
		this.c = c;
	}

	private void initUI() {
		Dimension surfaceMinimumSize = new Dimension(screenWidth - 280, screenHeight - 150);
		Dimension settingsMinimumSize = new Dimension(280, screenHeight);
		int settingsSplitPositionY = screenWidth - 280;
		int selectSplitPositionX = screenHeight - 200;
		
		surface = new Surface();
		surface.addMouseListener(this);
		settings = new JPanel();
		settings.setLayout(new BoxLayout(settings, BoxLayout.Y_AXIS));
		setOptions();		

		settingsSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		settingsSplit.setLeftComponent(surface);
		settingsSplit.setRightComponent(settings);
		settingsSplit.setDividerLocation(settingsSplitPositionY);
		settingsSplit.disable();
		
		surface.setMinimumSize(surfaceMinimumSize);
		settings.setMinimumSize(settingsMinimumSize);
		
		select = new JPanel();
		setSelectPanel();
		selectSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		selectSplit.setTopComponent(settingsSplit);
		selectSplit.setBottomComponent(select);
		selectSplit.setDividerLocation(selectSplitPositionX);
		selectSplit.disable();
		
		add(selectSplit);
		
		setTitle("Traffic jam simulator");
		
		setMinimumSize(new Dimension(700, 650));
		setSize(screenSize);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
	}
	
	private void setSelectPanel() {
		SpringLayout carLayout = new SpringLayout();
		scar = new JPanel(carLayout);
		
		selectedCarObject = new JLabel("Car");
		scar.add(selectedCarObject);
		carLayout.putConstraint(SpringLayout.WEST, selectedCarObject, 5, SpringLayout.WEST, scar);
		carLayout.putConstraint(SpringLayout.NORTH, selectedCarObject, 5, SpringLayout.NORTH, scar);

		slcarMaxSpeed = new JLabel("Max Speed: ");
		slcarMaxAcc = new JLabel("Max Acceleration: ");
		slcarColor = new JLabel("Color: ");
		scar.add(slcarMaxSpeed);
		scar.add(slcarMaxAcc);
		scar.add(slcarColor);
		carLayout.putConstraint(SpringLayout.WEST, slcarMaxSpeed, 5, SpringLayout.WEST, scar);
		carLayout.putConstraint(SpringLayout.NORTH, slcarMaxSpeed, 25, SpringLayout.NORTH, selectedCarObject);
		carLayout.putConstraint(SpringLayout.WEST, slcarMaxAcc, 5, SpringLayout.WEST, scar);
		carLayout.putConstraint(SpringLayout.NORTH, slcarMaxAcc, 25, SpringLayout.NORTH, slcarMaxSpeed);
		carLayout.putConstraint(SpringLayout.WEST, slcarColor, 5, SpringLayout.WEST, scar);
		carLayout.putConstraint(SpringLayout.NORTH, slcarColor, 25, SpringLayout.NORTH, slcarMaxAcc);


		stcarMaxSpeed = new JTextField(10);
		stcarMaxAcc = new JTextField(10);
		scolorChooser = new JComboBox(colorsS);
		scar.add(stcarMaxSpeed);
		scar.add(stcarMaxAcc);
		scar.add(scolorChooser);
		carLayout.putConstraint(SpringLayout.WEST, stcarMaxSpeed, 120, SpringLayout.WEST, scar);
		carLayout.putConstraint(SpringLayout.NORTH, stcarMaxSpeed, 25, SpringLayout.NORTH, selectedCarObject);
		carLayout.putConstraint(SpringLayout.WEST, stcarMaxAcc, 120, SpringLayout.WEST, scar);
		carLayout.putConstraint(SpringLayout.NORTH, stcarMaxAcc, 25, SpringLayout.NORTH, slcarMaxSpeed);
		carLayout.putConstraint(SpringLayout.WEST, scolorChooser, 120, SpringLayout.WEST, scar);
		carLayout.putConstraint(SpringLayout.NORTH, scolorChooser, 25, SpringLayout.NORTH, stcarMaxAcc);
		
		
		sldriverPrefSpeed = new JLabel("Preffered Speed: ");
		sldriverRange = new JLabel("Range of View: ");
		sldriverSafety = new JLabel("Safety gap: ");
		scar.add(sldriverPrefSpeed);
		scar.add(sldriverRange);
		scar.add(sldriverSafety);
		carLayout.putConstraint(SpringLayout.WEST, sldriverPrefSpeed, 240, SpringLayout.WEST, scar);
		carLayout.putConstraint(SpringLayout.NORTH, sldriverPrefSpeed, 30, SpringLayout.NORTH, scar);
		carLayout.putConstraint(SpringLayout.WEST, sldriverRange, 240, SpringLayout.WEST, scar);
		carLayout.putConstraint(SpringLayout.NORTH, sldriverRange, 25, SpringLayout.NORTH, sldriverPrefSpeed);
		carLayout.putConstraint(SpringLayout.WEST, sldriverSafety, 240, SpringLayout.WEST, scar);
		carLayout.putConstraint(SpringLayout.NORTH, sldriverSafety, 25, SpringLayout.NORTH, sldriverRange);
		
		stdriverPrefSpeed = new JTextField(10);
		stdriverRange = new JTextField(10);
		stdriverSafety = new JTextField(10);
		scar.add(stdriverPrefSpeed);
		scar.add(stdriverRange);
		scar.add(stdriverSafety);
		carLayout.putConstraint(SpringLayout.WEST, stdriverPrefSpeed, 360, SpringLayout.WEST, scar);
		carLayout.putConstraint(SpringLayout.NORTH, stdriverPrefSpeed, 30, SpringLayout.NORTH, scar);
		carLayout.putConstraint(SpringLayout.WEST, stdriverRange, 360, SpringLayout.WEST, scar);
		carLayout.putConstraint(SpringLayout.NORTH, stdriverRange, 25, SpringLayout.NORTH, sldriverPrefSpeed);
		carLayout.putConstraint(SpringLayout.WEST, stdriverSafety, 360, SpringLayout.WEST, scar);
		carLayout.putConstraint(SpringLayout.NORTH, stdriverSafety, 25, SpringLayout.NORTH, sldriverRange);
		
		bcAccept = new JButton("Accept");
		bcAccept.addActionListener(this);
		scar.add(bcAccept);
		carLayout.putConstraint(SpringLayout.WEST, bcAccept, 480, SpringLayout.WEST, scar);
		carLayout.putConstraint(SpringLayout.NORTH, bcAccept, 30, SpringLayout.NORTH, scar);
		bcDel = new JButton("Delete");
		bcDel.addActionListener(this);
		scar.add(bcDel);
		carLayout.putConstraint(SpringLayout.WEST, bcDel, 80, SpringLayout.WEST, bcAccept);
		carLayout.putConstraint(SpringLayout.NORTH, bcDel, 30, SpringLayout.NORTH, scar);
		
		SpringLayout blockLayout = new SpringLayout();
		sblock = new JPanel(blockLayout);
		
		selectedBlockObject = new JLabel("RoadBlock");
		sblock.add(selectedBlockObject);
		blockLayout.putConstraint(SpringLayout.WEST, selectedBlockObject, 5, SpringLayout.WEST, sblock);
		blockLayout.putConstraint(SpringLayout.NORTH, selectedBlockObject, 5, SpringLayout.NORTH, sblock);
		

		slblockDuration = new JLabel("Duration: ");
		sblock.add(slblockDuration);
		blockLayout.putConstraint(SpringLayout.WEST, slblockDuration, 5, SpringLayout.WEST, sblock);
		blockLayout.putConstraint(SpringLayout.NORTH, slblockDuration, 25, SpringLayout.NORTH, selectedBlockObject);

		stblockDuration = new JTextField(10);
		sblock.add(stblockDuration);
		blockLayout.putConstraint(SpringLayout.WEST, stblockDuration, 120, SpringLayout.WEST, sblock);
		blockLayout.putConstraint(SpringLayout.NORTH, stblockDuration, 25, SpringLayout.NORTH, selectedBlockObject);
		
		bbAccept = new JButton("Accept");
		bbAccept.addActionListener(this);
		sblock.add(bbAccept);
		blockLayout.putConstraint(SpringLayout.WEST, bbAccept, 240, SpringLayout.WEST, sblock);
		blockLayout.putConstraint(SpringLayout.NORTH, bbAccept, 25, SpringLayout.NORTH, sblock);
		bbDel = new JButton("Delete");
		bbDel.addActionListener(this);
		sblock.add(bbDel);
		blockLayout.putConstraint(SpringLayout.WEST, bbDel, 80, SpringLayout.WEST, bbAccept);
		blockLayout.putConstraint(SpringLayout.NORTH, bbDel, 25, SpringLayout.NORTH, sblock);
		
	}

	public void setOptions(){
		combo = new JPanel();
		newObjectTitle = new JLabel("New Road Object: ");
		typeChooser = new JComboBox(types);
		typeChooser.setSelectedIndex(0);
		typeChooser.addActionListener(this);
		combo.add(newObjectTitle);
		combo.add(typeChooser);
		
		options = new JPanel(new CardLayout());
		setCarOptions();
        setBlockOptions();
		options.add(car, CARPANEL);
		options.add(block, BLOCKPANEL);
		
		SpringLayout hwLayout = new SpringLayout();
		highWay = new JPanel(hwLayout);	
		aditional =  new JLabel("Aditional options: ");
		lhwLenght = new JLabel("HighWay lenght: ");
		thwLenght = new JTextField(10);
		thwLenght.addActionListener(this);
		bPause = new JButton("Pause");
		bPause.addActionListener(this);
		bSave = new JButton("Save");
		bSave.addActionListener(this);
		bLoad = new JButton("Load");
		bLoad.addActionListener(this);

		highWay.add(aditional);
		highWay.add(lhwLenght);
		highWay.add(thwLenght);
		highWay.add(bPause);
		highWay.add(bSave);
		highWay.add(bLoad);
		hwLayout.putConstraint(SpringLayout.WEST, aditional, 5, SpringLayout.WEST, highWay);
		hwLayout.putConstraint(SpringLayout.NORTH, aditional, 25, SpringLayout.NORTH, highWay);
		hwLayout.putConstraint(SpringLayout.WEST, lhwLenght, 5, SpringLayout.WEST, highWay);
		hwLayout.putConstraint(SpringLayout.NORTH, lhwLenght, 25, SpringLayout.NORTH, aditional);
		hwLayout.putConstraint(SpringLayout.WEST, thwLenght, 120, SpringLayout.WEST, highWay);
		hwLayout.putConstraint(SpringLayout.NORTH, thwLenght, 25, SpringLayout.NORTH, aditional);
		hwLayout.putConstraint(SpringLayout.WEST, bPause, 5, SpringLayout.WEST, highWay);
		hwLayout.putConstraint(SpringLayout.NORTH, bPause, 25, SpringLayout.NORTH, lhwLenght);
		hwLayout.putConstraint(SpringLayout.WEST, bSave, 5, SpringLayout.WEST, highWay);
		hwLayout.putConstraint(SpringLayout.NORTH, bSave, 35, SpringLayout.NORTH, bPause);
		hwLayout.putConstraint(SpringLayout.WEST, bLoad, 75, SpringLayout.WEST, highWay);
		hwLayout.putConstraint(SpringLayout.NORTH, bLoad, 35, SpringLayout.NORTH, bPause);
		
		settings.add(combo);
		settings.add(options);
		settings.add(highWay);
	}
	
	public void setCarOptions(){
		SpringLayout carLayout = new SpringLayout();
		car = new JPanel(carLayout);
		
		carTitle = new JLabel("Car");
		car.add(carTitle);
		carLayout.putConstraint(SpringLayout.WEST, carTitle, 5, SpringLayout.WEST, car);
		carLayout.putConstraint(SpringLayout.NORTH, carTitle, 5, SpringLayout.NORTH, car);

		lcarMaxSpeed = new JLabel("Max Speed: ");
		lcarMaxAcc = new JLabel("Max Acceleration: ");
		lcarColor = new JLabel("Color: ");
		car.add(lcarMaxSpeed);
		car.add(lcarMaxAcc);
		car.add(lcarColor);
		carLayout.putConstraint(SpringLayout.WEST, lcarMaxSpeed, 5, SpringLayout.WEST, car);
		carLayout.putConstraint(SpringLayout.NORTH, lcarMaxSpeed, 25, SpringLayout.NORTH, carTitle);
		carLayout.putConstraint(SpringLayout.WEST, lcarMaxAcc, 5, SpringLayout.WEST, car);
		carLayout.putConstraint(SpringLayout.NORTH, lcarMaxAcc, 25, SpringLayout.NORTH, lcarMaxSpeed);
		carLayout.putConstraint(SpringLayout.WEST, lcarColor, 5, SpringLayout.WEST, car);
		carLayout.putConstraint(SpringLayout.NORTH, lcarColor, 25, SpringLayout.NORTH, lcarMaxAcc);


		tcarMaxSpeed = new JTextField(10);
		tcarMaxAcc = new JTextField(10);
		colorChooser = new JComboBox(colorsS);
		car.add(tcarMaxSpeed);
		car.add(tcarMaxAcc);
		car.add(colorChooser);
		carLayout.putConstraint(SpringLayout.WEST, tcarMaxSpeed, 120, SpringLayout.WEST, car);
		carLayout.putConstraint(SpringLayout.NORTH, tcarMaxSpeed, 25, SpringLayout.NORTH, carTitle);
		carLayout.putConstraint(SpringLayout.WEST, tcarMaxAcc, 120, SpringLayout.WEST, car);
		carLayout.putConstraint(SpringLayout.NORTH, tcarMaxAcc, 25, SpringLayout.NORTH, lcarMaxSpeed);
		carLayout.putConstraint(SpringLayout.WEST, colorChooser, 120, SpringLayout.WEST, car);
		carLayout.putConstraint(SpringLayout.NORTH, colorChooser, 25, SpringLayout.NORTH, tcarMaxAcc);
		

		driverTitle = new JLabel("Driver");
		car.add(driverTitle);
		carLayout.putConstraint(SpringLayout.WEST, driverTitle, 5, SpringLayout.WEST, car);
		carLayout.putConstraint(SpringLayout.NORTH, driverTitle, 35, SpringLayout.NORTH, colorChooser);
		
		ldriverPrefSpeed = new JLabel("Preffered Speed: ");
		ldriverRange = new JLabel("Range of View: ");
		ldriverSafety = new JLabel("Safety gap: ");
		car.add(ldriverPrefSpeed);
		car.add(ldriverRange);
		car.add(ldriverSafety);
		carLayout.putConstraint(SpringLayout.WEST, ldriverPrefSpeed, 5, SpringLayout.WEST, car);
		carLayout.putConstraint(SpringLayout.NORTH, ldriverPrefSpeed, 25, SpringLayout.NORTH, driverTitle);
		carLayout.putConstraint(SpringLayout.WEST, ldriverRange, 5, SpringLayout.WEST, car);
		carLayout.putConstraint(SpringLayout.NORTH, ldriverRange, 25, SpringLayout.NORTH, ldriverPrefSpeed);
		carLayout.putConstraint(SpringLayout.WEST, ldriverSafety, 5, SpringLayout.WEST, car);
		carLayout.putConstraint(SpringLayout.NORTH, ldriverSafety, 25, SpringLayout.NORTH, ldriverRange);
		
		tdriverPrefSpeed = new JTextField(10);
		tdriverRange = new JTextField(10);
		tdriverSafety = new JTextField(10);
		car.add(tdriverPrefSpeed);
		car.add(tdriverRange);
		car.add(tdriverSafety);
		carLayout.putConstraint(SpringLayout.WEST, tdriverPrefSpeed, 120, SpringLayout.WEST, car);
		carLayout.putConstraint(SpringLayout.NORTH, tdriverPrefSpeed, 25, SpringLayout.NORTH, driverTitle);
		carLayout.putConstraint(SpringLayout.WEST, tdriverRange, 120, SpringLayout.WEST, car);
		carLayout.putConstraint(SpringLayout.NORTH, tdriverRange, 25, SpringLayout.NORTH, ldriverPrefSpeed);
		carLayout.putConstraint(SpringLayout.WEST, tdriverSafety, 120, SpringLayout.WEST, car);
		carLayout.putConstraint(SpringLayout.NORTH, tdriverSafety, 25, SpringLayout.NORTH, ldriverRange);
		
		
	}
	
	public void setBlockOptions(){
		SpringLayout blockLayout = new SpringLayout();
		block = new JPanel(blockLayout);
		
		blockTitle = new JLabel("RoadBlock");
		block.add(blockTitle);
		blockLayout.putConstraint(SpringLayout.WEST, blockTitle, 5, SpringLayout.WEST, block);
		blockLayout.putConstraint(SpringLayout.NORTH, blockTitle, 5, SpringLayout.NORTH, block);
		

		lblockDuration = new JLabel("Duration: ");
		block.add(lblockDuration);
		blockLayout.putConstraint(SpringLayout.WEST, lblockDuration, 5, SpringLayout.WEST, block);
		blockLayout.putConstraint(SpringLayout.NORTH, lblockDuration, 25, SpringLayout.NORTH, blockTitle);

		tblockDuration = new JTextField(10);
		block.add(tblockDuration);
		blockLayout.putConstraint(SpringLayout.WEST, tblockDuration, 120, SpringLayout.WEST, block);
		blockLayout.putConstraint(SpringLayout.NORTH, tblockDuration, 25, SpringLayout.NORTH, blockTitle);
	}
	
	
	public void typeSelected(String type) {
	    CardLayout cl = (CardLayout)(options.getLayout());
	    cl.show(options, type);
	}
	
	public void setRoadObjects(ArrayList<RoadObject> ro){
		surface.setRoadObjects(ro);
	}
	public void reFresh(){
		surface.reFresh();
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() instanceof JComboBox){
			JComboBox cb = (JComboBox)e.getSource();
	        String type = (String)cb.getSelectedItem();
	        typeSelected(type);
		}	
		if(e.getSource() instanceof JTextField){
			JTextField tf = (JTextField)e.getSource();
	        String lenght = (String)tf.getText();
	        if("".equals(lenght))
	        	lenght = new String("1500");
			c.hw.setLenght(Float.parseFloat(lenght));
			if(!Float.toString(c.hw.lenght).equals(thwLenght.getText()))
				thwLenght.setText(Float.toString(c.hw.lenght));
		}
		if(e.getSource() instanceof JButton){
			JButton b = (JButton)e.getSource();
			String text = b.getText();
			if("Pause".equals(text)){
				bPause.setText("Start");
				c.stop();
			} else if("Start".equals(text)){
				bPause.setText("Pause");
				c.stop();
			} else if("Save".equals(text)){
				String extension = "txt";
				JFileChooser fc = new JFileChooser();
				fc.setFileFilter(new FileNameExtensionFilter("TEXT FILES", "txt"));
				Status s = c.s; 
				if(c.s == Status.RUNNING)
					c.stop();
				int returnVal = fc.showSaveDialog(this);
				if(c.s != s)
					c.stop();
		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File file = fc.getSelectedFile();
		        	if(file != null){
			            if(!file.getName().toLowerCase().endsWith('.' + extension.toLowerCase()))
			            	file = new File(file.toString() + '.' + extension);
						c.SaveGame(file);
		        	}
		        } 	
			} else if("Load".equals(text)){
				JFileChooser fc = new JFileChooser();
				fc.setFileFilter(new FileNameExtensionFilter("TEXT FILES", "txt"));
				Status s = c.s; 
				if(c.s == Status.RUNNING)
					c.stop();
				int returnVal = fc.showOpenDialog(this);
				if(c.s != s)
					c.stop();
		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File file = fc.getSelectedFile();
		        	if(file != null)
		        		c.LoadGame(file);
		        } 	
			} else if(b == bcAccept){
				c.hw.modifyCar(selected.id, Float.parseFloat(stcarMaxSpeed.getText()), Float.parseFloat(stcarMaxAcc.getText()), colorsC[scolorChooser.getSelectedIndex()], 
					Float.parseFloat(stdriverPrefSpeed.getText()), Float.parseFloat(stdriverRange.getText()), Float.parseFloat(stdriverSafety.getText()));	
			} else if(b == bbAccept){
				c.hw.modifyBlock(selected.id, Float.parseFloat(stblockDuration.getText()));
			} else if(b == bcDel || b == bbDel){
				c.hw.deleteObject(selected);
			}
		}
	}
	
	public Point2D.Float reConvertCoords(Point2D.Float position){
		return new Point2D.Float(position.x - (size/2), size - (position.y + (size/2)));
	}
	
	public void setSelected(){
		if(selected != null){
			if(selected instanceof Car){
				selectSplit.setBottomComponent(scar);
//				select.remove(sblock);
//				select.add(scar);
				selectedCarObject.setText("Car" + selected.id);
				stcarMaxSpeed.setText(Float.toString(((Car) selected).maxSpeed));
				stcarMaxAcc.setText(Float.toString(((Car) selected).maxAcc));
				for (int i = 0; i < colorsC.length; i++){
					if(colorsC[i] == ((Car) selected).color){
						scolorChooser.setSelectedIndex(i);
					}
				}
				stdriverPrefSpeed.setText(Float.toString(((Car) selected).driver.prefSpeed));
				stdriverRange.setText(Float.toString(((Car) selected).driver.rangeOfView));
				stdriverSafety.setText(Float.toString(((Car) selected).driver.safetyGap));
			}
			else if(selected instanceof Block){
				selectSplit.setBottomComponent(sblock);
//				select.remove(scar);
//				select.add(sblock);
				selectedBlockObject.setText("Block" + selected.id);
				stblockDuration.setText(Float.toString(((Block) selected).duration));
			}
			
		}
		SwingUtilities.updateComponentTreeUI(this);
		selectSplit.invalidate();
		selectSplit.repaint();
		selectSplit.revalidate();
	}
	
	
	@Override
	public void mouseClicked(MouseEvent e) {
		Point2D.Float newPoint = new Point2D.Float();
		newPoint.x = e.getX();
		newPoint.y = e.getY();
		selected = surface.getObject(reConvertCoords(newPoint));
		if(selected == null && typeChooser.getSelectedItem().equals("Car")){
			if(!"".equals(tcarMaxSpeed.getText()) && !"".equals(tcarMaxAcc.getText()) && 
					!"".equals(tdriverPrefSpeed.getText()) && !"".equals(tdriverRange.getText()) && !"".equals(tdriverSafety.getText())){
				c.hw.addCar(reConvertCoords(newPoint), Float.parseFloat(tcarMaxSpeed.getText()), Float.parseFloat(tcarMaxAcc.getText()), colorsC[colorChooser.getSelectedIndex()], 
						Float.parseFloat(tdriverPrefSpeed.getText()), Float.parseFloat(tdriverRange.getText()), Float.parseFloat(tdriverSafety.getText()));	
			}
		}else if (selected == null){
			if(!"".equals(tblockDuration.getText())){
				c.hw.addBlock(reConvertCoords(newPoint), Float.parseFloat(tblockDuration.getText()));
			}
		}
		selected = surface.getObject(reConvertCoords(newPoint));
		setSelected();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}
