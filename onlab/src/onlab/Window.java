package onlab;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import onlab.Controller.Status;

public class Window extends JFrame implements ActionListener, ItemListener, MouseListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Controller c;
	Surface surface;
	JFrame chartFrame;
	JPanel settings, combo, options, car, block, highWay, select, scar, sblock, log, profiles, dpPanel;
	JLabel newObjectTitle, carTitle, driverTitle, blockTitle, aditional, selectedTitle, selectedCarObject, selectedBlockObject;
	JLabel lcarMaxSpeed, lcarMaxAcc, lcarColor, ldriverPrefSpeed, ldriverRange, ldriverSafety, lblockDuration, lhwLenght;
	JLabel lLanes, ldpChooser, scolorChooser, lLaneSpan, lcarName, lNewCarTimer, lTimeWarp;
	JLabel lDPTitle, lIDTitle, lNameTitle, lPercentageTitle, lColorTitle, lcarCount, lCarStatus;
	JTextField tcarMaxSpeed, tcarMaxAcc, tdriverPrefSpeed, tdriverRange, tdriverSafety, 
			tblockDuration, thwLenght, stblockDuration, sthwLengh;
	JTextField tcarName, tNewCarTimer, tTimeWarp;
	JLabel  slcarMaxSpeed, slcarMaxAcc, slcarColor, sldriverPrefSpeed, sldriverRange, sldriverSafety, slblockDuration, slhwLenght;
	JLabel  stcarMaxSpeed, stcarMaxAcc, stdriverPrefSpeed, stdriverRange, stdriverSafety;
	JComboBox<String> typeChooser , colorChooser, dpChooser, cbCharts;
	JComboBox<Integer> cbLaneSpan, cbLanes;
	JButton bRestart, bPause, bSave, bLoad, bcAccept, bbAccept, bcDel, bbDel, bChart, bAddDriverProfile;
	JTextArea tLog;
	JScrollPane logScroll;
	JSplitPane split1, split2, split3; 
	JSplitPane settingsSplit, selectSplit, logSplit, profilesSplit; 
	final static String CARPANEL = "Driver Profile";
	final static String BLOCKPANEL = "Road Block";
	float size = 450;
	String[] types = {CARPANEL, BLOCKPANEL};
	String[] colorsS = {"White", "Yellow", "Green", "Cyan", "Blue", "Pink", "Orange", "Red"};
	Color[] colorsC = {Color.WHITE ,Color.YELLOW ,Color.GREEN ,Color.CYAN ,Color.BLUE ,Color.PINK ,Color.ORANGE, Color.RED};
	Integer[] lanes = {1, 2, 3, 4, 5, 6};
	String[] charts = {"Car speed", "Profile speed", "Driver status"};
	RoadObject selected;
	Map<String, TimeSeries> series = new HashMap<String, TimeSeries>();
	ArrayList<TimeSeriesCollection> dataset= new ArrayList<TimeSeriesCollection>();
	JFreeChart chart;
	XYPlot plot;
	ChartPanel chartPanel;
	JPanel content;

	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	int screenWidth = screenSize.width;
	int screenHeight = screenSize.height;

	Dimension surfaceMinimumSize = new Dimension(screenWidth - 280, screenHeight - 150);
	Dimension settingsMinimumSize = new Dimension(280, screenHeight);
	Dimension logMinimumSize = new Dimension(280, screenHeight);
	Dimension driverProfilePanelMinimumSize = new Dimension(320, screenHeight);
	int settingsSplitPositionY = screenWidth - 280;
	int profileSplitPositionY = screenWidth - 280 - 320;
	int selectSplitPositionX = screenHeight - 250;
	int logSplitPositionX = screenHeight - 200;
		
	public Window(Controller c) {
		this.c = c;
	}

	public Road initUI() {
		surface = new Surface();		
		surface.addMouseListener(this);
		settings = new JPanel();
		settings.setLayout(new BoxLayout(settings, BoxLayout.Y_AXIS));
		setOptions();		
		log = new JPanel();
		log.setLayout(new BorderLayout());
		setLogPanel();	
		
		surface.setMinimumSize(surfaceMinimumSize);
		settings.setMinimumSize(settingsMinimumSize);
		log.setMinimumSize(logMinimumSize);	

		select = new JPanel();
		setSelectPanel();
		
		profiles = new JPanel();
		profiles.setMinimumSize(logMinimumSize);	
		
		logSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		logSplit.setTopComponent(settings);
		logSplit.setBottomComponent(log);
		logSplit.setDividerLocation(logSplitPositionX);
			
		profilesSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		profilesSplit.setLeftComponent(select);
		profilesSplit.setRightComponent(profiles);
		profilesSplit.setDividerLocation(profileSplitPositionY);
		profilesSplit.setEnabled(false);	
		
		selectSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		selectSplit.setTopComponent(surface);
		selectSplit.setBottomComponent(profilesSplit);
		selectSplit.setDividerLocation(selectSplitPositionX);
		selectSplit.setEnabled(false);
				
		settingsSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		settingsSplit.setLeftComponent(selectSplit);
		settingsSplit.setRightComponent(logSplit);
		settingsSplit.setDividerLocation(settingsSplitPositionY);
		settingsSplit.setEnabled(false);

		
		add(settingsSplit);
		
		setTitle("Traffic jam simulator");
		
//		setMinimumSize(new Dimension(700, 650));
//		setSize(screenSize);
//		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		setResizable(false);
		
		return getRoad();
	}
	
	public void setProfilesPanel() {
		String[] names = HighWay.getDriverProfileNames();
		Integer[] percentages = HighWay.getDriverProfilePercentages();
		Color[] colors = HighWay.getDriverProfileColors();
		
		SpringLayout profileLayout = new SpringLayout();
		dpPanel = new JPanel(profileLayout);
		
		lDPTitle = new JLabel("Driving Profiles:");
		lIDTitle = new JLabel("ID");
		lNameTitle = new JLabel("Name");
		lPercentageTitle = new JLabel("Percentage");
		lColorTitle = new JLabel("Color");
		dpPanel.add(lDPTitle);
		dpPanel.add(lIDTitle);
		dpPanel.add(lNameTitle);
		dpPanel.add(lPercentageTitle);
		dpPanel.add(lColorTitle);

		profileLayout.putConstraint(SpringLayout.WEST, lDPTitle, 5, SpringLayout.WEST, dpPanel);
		profileLayout.putConstraint(SpringLayout.NORTH, lDPTitle, 5, SpringLayout.NORTH, dpPanel);
		profileLayout.putConstraint(SpringLayout.WEST, lIDTitle, 5, SpringLayout.WEST, dpPanel);
		profileLayout.putConstraint(SpringLayout.NORTH, lIDTitle, 25, SpringLayout.NORTH, dpPanel);
		profileLayout.putConstraint(SpringLayout.WEST, lNameTitle, 25, SpringLayout.WEST, dpPanel);
		profileLayout.putConstraint(SpringLayout.NORTH, lNameTitle, 25, SpringLayout.NORTH, dpPanel);
		profileLayout.putConstraint(SpringLayout.EAST, lPercentageTitle, -60, SpringLayout.EAST, dpPanel);
		profileLayout.putConstraint(SpringLayout.NORTH, lPercentageTitle, 25, SpringLayout.NORTH, dpPanel);
		profileLayout.putConstraint(SpringLayout.EAST, lColorTitle, -5, SpringLayout.EAST, dpPanel);
		profileLayout.putConstraint(SpringLayout.NORTH, lColorTitle, 25, SpringLayout.NORTH, dpPanel);
		
		JLabel id;
		JLabel name;
		JSpinner percentage;
		JLabel color;
		for (int i = 0; i < names.length; i++) {
			int index = i;
			id = new JLabel(Integer.toString(i));
			name = new JLabel(names[i]);
			SpinnerModel percentageModel = new SpinnerNumberModel((int)percentages[i], 0, 100, 1);
			percentage =  new JSpinner(percentageModel);
			percentage.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent e) {
					JSpinner s = (JSpinner) e.getSource();
					Integer value = (Integer) s.getValue();
					Float result = (float)value;
					System.out.println("SPINNER: " + value);
					HighWay.setDriverProfilesPercentage(index, result);			
					setProfilesPanel();
				}
			});
			color = new JLabel(getColorName(colors[i]));
			color.setForeground(colors[i]);
			dpPanel.add(id);
			dpPanel.add(name);
			dpPanel.add(percentage);
			dpPanel.add(color);

			profileLayout.putConstraint(SpringLayout.WEST, id, 5, SpringLayout.WEST, dpPanel);
			profileLayout.putConstraint(SpringLayout.NORTH, id,(i+1) * 25, SpringLayout.NORTH, lIDTitle);
			profileLayout.putConstraint(SpringLayout.WEST, name, 25, SpringLayout.WEST, dpPanel);
			profileLayout.putConstraint(SpringLayout.NORTH, name, (i+1) * 25, SpringLayout.NORTH, lIDTitle);
			profileLayout.putConstraint(SpringLayout.EAST, percentage, -60, SpringLayout.EAST, dpPanel);
			profileLayout.putConstraint(SpringLayout.NORTH, percentage, (i+1) * 25, SpringLayout.NORTH, lIDTitle);
			profileLayout.putConstraint(SpringLayout.EAST, color, -5, SpringLayout.EAST, dpPanel);
			profileLayout.putConstraint(SpringLayout.NORTH, color, (i+1) * 25, SpringLayout.NORTH, lIDTitle);
		}
		dpPanel.setMinimumSize(driverProfilePanelMinimumSize);
		profilesSplit.setRightComponent(dpPanel);
		profilesSplit.setDividerLocation(profileSplitPositionY);
	}
	
	private String getColorName(Color c){
		for (int i = 0; i < colorsC.length; i++) {
			if(c.equals(colorsC[i])){
				return colorsS[i];
			}
		}
		return null;
	}

	private void setSelectPanel() {
		SpringLayout carLayout = new SpringLayout();
		scar = new JPanel(carLayout);
		
		selectedCarObject = new JLabel("ID: ");
		ldpChooser = new JLabel("Driver Profile: ");
		dpChooser = new JComboBox<String>(getDriverProfiles());
		dpChooser.setSelectedIndex(0);
		dpChooser.addActionListener(this);
		lCarStatus = new JLabel("Status: ");
		scar.add(ldpChooser);
		scar.add(dpChooser);
		scar.add(selectedCarObject);
		scar.add(lCarStatus);
		carLayout.putConstraint(SpringLayout.WEST, ldpChooser, 5, SpringLayout.WEST, scar);
		carLayout.putConstraint(SpringLayout.NORTH, ldpChooser, 10, SpringLayout.NORTH, scar);
		carLayout.putConstraint(SpringLayout.WEST, dpChooser, 5, SpringLayout.EAST, ldpChooser);
		carLayout.putConstraint(SpringLayout.NORTH, dpChooser, 5, SpringLayout.NORTH, scar);
		carLayout.putConstraint(SpringLayout.WEST, selectedCarObject, 10, SpringLayout.EAST, dpChooser);
		carLayout.putConstraint(SpringLayout.NORTH, selectedCarObject, 10, SpringLayout.NORTH, scar);
		carLayout.putConstraint(SpringLayout.WEST, lCarStatus, 50, SpringLayout.WEST, selectedCarObject);
		carLayout.putConstraint(SpringLayout.NORTH, lCarStatus, 10, SpringLayout.NORTH, scar);

		slcarMaxSpeed = new JLabel("Max Speed: ");
		slcarMaxAcc = new JLabel("Max Acceleration: ");
		slcarColor = new JLabel("Color: ");
		scar.add(slcarMaxSpeed);
		scar.add(slcarMaxAcc);
		scar.add(slcarColor);
		carLayout.putConstraint(SpringLayout.WEST, slcarMaxSpeed, 5, SpringLayout.WEST, scar);
		carLayout.putConstraint(SpringLayout.NORTH, slcarMaxSpeed, 40, SpringLayout.NORTH, selectedCarObject);
		carLayout.putConstraint(SpringLayout.WEST, slcarMaxAcc, 5, SpringLayout.WEST, scar);
		carLayout.putConstraint(SpringLayout.NORTH, slcarMaxAcc, 25, SpringLayout.NORTH, slcarMaxSpeed);
		carLayout.putConstraint(SpringLayout.WEST, slcarColor, 5, SpringLayout.WEST, scar);
		carLayout.putConstraint(SpringLayout.NORTH, slcarColor, 25, SpringLayout.NORTH, slcarMaxAcc);


		stcarMaxSpeed = new JLabel();
		stcarMaxAcc = new JLabel();
		stcarMaxSpeed.setToolTipText("km/h");
		stcarMaxAcc.setToolTipText("the time it takes to get from 0 to 100 km/h (sec)");
		scolorChooser = new JLabel();
		scar.add(stcarMaxSpeed);
		scar.add(stcarMaxAcc);
		scar.add(scolorChooser);
		carLayout.putConstraint(SpringLayout.WEST, stcarMaxSpeed, 120, SpringLayout.WEST, scar);
		carLayout.putConstraint(SpringLayout.NORTH, stcarMaxSpeed, 40, SpringLayout.NORTH, selectedCarObject);
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
		carLayout.putConstraint(SpringLayout.WEST, sldriverPrefSpeed, 200, SpringLayout.WEST, scar);
		carLayout.putConstraint(SpringLayout.NORTH, sldriverPrefSpeed, 40, SpringLayout.NORTH, selectedCarObject);
		carLayout.putConstraint(SpringLayout.WEST, sldriverRange, 200, SpringLayout.WEST, scar);
		carLayout.putConstraint(SpringLayout.NORTH, sldriverRange, 25, SpringLayout.NORTH, sldriverPrefSpeed);
		carLayout.putConstraint(SpringLayout.WEST, sldriverSafety, 200, SpringLayout.WEST, scar);
		carLayout.putConstraint(SpringLayout.NORTH, sldriverSafety, 25, SpringLayout.NORTH, sldriverRange);

		stdriverPrefSpeed = new JLabel();
		stdriverRange = new JLabel();
		stdriverSafety = new JLabel();
		stdriverPrefSpeed.setToolTipText("km/h");
		stdriverRange.setToolTipText("km");
		stdriverSafety.setToolTipText("km");
		scar.add(stdriverPrefSpeed);
		scar.add(stdriverRange);
		scar.add(stdriverSafety);
		carLayout.putConstraint(SpringLayout.WEST, stdriverPrefSpeed, 300, SpringLayout.WEST, scar);
		carLayout.putConstraint(SpringLayout.NORTH, stdriverPrefSpeed, 40, SpringLayout.NORTH, selectedCarObject);
		carLayout.putConstraint(SpringLayout.WEST, stdriverRange, 300, SpringLayout.WEST, scar);
		carLayout.putConstraint(SpringLayout.NORTH, stdriverRange, 25, SpringLayout.NORTH, sldriverPrefSpeed);
		carLayout.putConstraint(SpringLayout.WEST, stdriverSafety, 300, SpringLayout.WEST, scar);
		carLayout.putConstraint(SpringLayout.NORTH, stdriverSafety, 25, SpringLayout.NORTH, sldriverRange);
		
		bcAccept = new JButton("Accept");
		bcAccept.addActionListener(this);
		scar.add(bcAccept);
		carLayout.putConstraint(SpringLayout.WEST, bcAccept, 5, SpringLayout.WEST, scar);
		carLayout.putConstraint(SpringLayout.NORTH, bcAccept, 30, SpringLayout.NORTH, scolorChooser);
		bcDel = new JButton("Delete");
		bcDel.addActionListener(this);
		scar.add(bcDel);
		carLayout.putConstraint(SpringLayout.WEST, bcDel, 80, SpringLayout.WEST, bcAccept);
		carLayout.putConstraint(SpringLayout.NORTH, bcDel, 30, SpringLayout.NORTH, scolorChooser);
		
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
		stblockDuration.setToolTipText("sec");
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

	private String[] getDriverProfiles() {
		return HighWay.getDriverProfileNames();
	}

	public void setOptions(){
		combo = new JPanel();
		newObjectTitle = new JLabel("New Road Object: ");
		typeChooser = new JComboBox<String>(types);
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

		lNewCarTimer = new JLabel("New car timer: ");
		tNewCarTimer = new JTextField(10);		
		tNewCarTimer.setText(Float.toString(c.getNewCarTime()));
		tNewCarTimer.setToolTipText("ms");
		tNewCarTimer.addActionListener(this);

		lTimeWarp = new JLabel("Time warp: ");
		tTimeWarp = new JTextField(10);
		tTimeWarp.setText(Float.toString(c.getTimeWarp()));
		tTimeWarp.setToolTipText("how much faster should the time be");
		tTimeWarp.addActionListener(this);
		
		lLanes = new JLabel("Lanes: ");
		cbLanes = new JComboBox<Integer>(lanes);
		cbLanes.setSelectedIndex(2);
		cbLanes.setToolTipText("Number of lanes");
		cbLanes.addItemListener(this);
		
		bRestart = new JButton("Restart");
		bRestart.addActionListener(this);		
		bPause = new JButton("Pause");
		bPause.addActionListener(this);
		bSave = new JButton("Save");
		bSave.addActionListener(this);
		bLoad = new JButton("Load");
		bLoad.addActionListener(this);
		

		cbCharts = new JComboBox<String>(charts);
		cbCharts.setSelectedIndex(0);
		cbCharts.setToolTipText("Type of the chart");
		bChart = new JButton("Show chart");
		bChart.addActionListener(this);
		
		lcarCount = new JLabel("There are " + HighWay.getRoadObjects().size() + " cars on the highway.");

		highWay.add(aditional);
		highWay.add(lNewCarTimer);
		highWay.add(tNewCarTimer);
		highWay.add(lTimeWarp);
		highWay.add(tTimeWarp);
		highWay.add(lLanes);
		highWay.add(cbLanes);
		highWay.add(bRestart);
		highWay.add(bPause);
		highWay.add(bSave);
		highWay.add(bLoad);
		highWay.add(cbCharts);
		highWay.add(bChart);
		highWay.add(lcarCount);
		hwLayout.putConstraint(SpringLayout.WEST, aditional, 5, SpringLayout.WEST, highWay);
		hwLayout.putConstraint(SpringLayout.NORTH, aditional, 5, SpringLayout.NORTH, highWay);	

		hwLayout.putConstraint(SpringLayout.WEST, lNewCarTimer, 5, SpringLayout.WEST, highWay);
		hwLayout.putConstraint(SpringLayout.NORTH, lNewCarTimer, 25, SpringLayout.NORTH, aditional);
		hwLayout.putConstraint(SpringLayout.WEST, tNewCarTimer, 120, SpringLayout.WEST, highWay);
		hwLayout.putConstraint(SpringLayout.NORTH, tNewCarTimer, 25, SpringLayout.NORTH, aditional);
		
		hwLayout.putConstraint(SpringLayout.WEST, lTimeWarp, 5, SpringLayout.WEST, highWay);
		hwLayout.putConstraint(SpringLayout.NORTH, lTimeWarp, 25, SpringLayout.NORTH, lNewCarTimer);
		hwLayout.putConstraint(SpringLayout.WEST, tTimeWarp, 120, SpringLayout.WEST, highWay);
		hwLayout.putConstraint(SpringLayout.NORTH, tTimeWarp, 25, SpringLayout.NORTH, lNewCarTimer);

		hwLayout.putConstraint(SpringLayout.WEST, lLanes, 5, SpringLayout.WEST, highWay);
		hwLayout.putConstraint(SpringLayout.NORTH, lLanes, 25, SpringLayout.NORTH, lTimeWarp);
		hwLayout.putConstraint(SpringLayout.WEST, cbLanes, 120, SpringLayout.WEST, highWay);
		hwLayout.putConstraint(SpringLayout.NORTH, cbLanes, 25, SpringLayout.NORTH, lTimeWarp);
		
		hwLayout.putConstraint(SpringLayout.EAST, bRestart, 120, SpringLayout.WEST, highWay);
		hwLayout.putConstraint(SpringLayout.NORTH, bRestart, 50, SpringLayout.NORTH, lLanes);		
		hwLayout.putConstraint(SpringLayout.WEST, bPause, 125, SpringLayout.WEST, highWay);
		hwLayout.putConstraint(SpringLayout.NORTH, bPause, 50, SpringLayout.NORTH, lLanes);
		
		hwLayout.putConstraint(SpringLayout.EAST, bSave, 120, SpringLayout.WEST, highWay);
		hwLayout.putConstraint(SpringLayout.NORTH, bSave, 35, SpringLayout.NORTH, bPause);
		hwLayout.putConstraint(SpringLayout.WEST, bLoad, 125, SpringLayout.WEST, highWay);
		hwLayout.putConstraint(SpringLayout.NORTH, bLoad, 35, SpringLayout.NORTH, bPause);

		hwLayout.putConstraint(SpringLayout.WEST, cbCharts, 5, SpringLayout.WEST, highWay);
		hwLayout.putConstraint(SpringLayout.NORTH, cbCharts, 35, SpringLayout.NORTH, bSave);

		hwLayout.putConstraint(SpringLayout.WEST, bChart, 120, SpringLayout.WEST, highWay);
		hwLayout.putConstraint(SpringLayout.NORTH, bChart, 35, SpringLayout.NORTH, bSave);
		
		hwLayout.putConstraint(SpringLayout.WEST, lcarCount, 5, SpringLayout.WEST, highWay);
		hwLayout.putConstraint(SpringLayout.NORTH, lcarCount, 50, SpringLayout.NORTH, bChart);
		
		settings.add(combo);
		settings.add(options);
		settings.add(highWay);
	}
	
	public void setCarOptions(){
		SpringLayout carLayout = new SpringLayout();
		car = new JPanel(carLayout);
		
		lcarName = new JLabel("Name: ");
		tcarName = new JTextField(10);
		car.add(lcarName);
		car.add(tcarName);
		carLayout.putConstraint(SpringLayout.WEST, lcarName, 5, SpringLayout.WEST, car);
		carLayout.putConstraint(SpringLayout.NORTH, lcarName, 5, SpringLayout.NORTH, car);
		carLayout.putConstraint(SpringLayout.WEST, tcarName, 120, SpringLayout.WEST, car);
		carLayout.putConstraint(SpringLayout.NORTH, tcarName, 5, SpringLayout.NORTH, car);
		
		carTitle = new JLabel("Car");
		car.add(carTitle);
		carLayout.putConstraint(SpringLayout.WEST, carTitle, 5, SpringLayout.WEST, car);
		carLayout.putConstraint(SpringLayout.NORTH, carTitle, 25, SpringLayout.NORTH, lcarName);

		lcarMaxSpeed = new JLabel("Max Speed: ");
		lcarMaxAcc = new JLabel("Max Acceleration: ");
		lcarColor = new JLabel("Color: ");
		car.add(lcarMaxSpeed);
		car.add(lcarMaxAcc);
		car.add(lcarColor);
		carLayout.putConstraint(SpringLayout.WEST, lcarMaxSpeed, 10, SpringLayout.WEST, car);
		carLayout.putConstraint(SpringLayout.NORTH, lcarMaxSpeed, 25, SpringLayout.NORTH, carTitle);
		carLayout.putConstraint(SpringLayout.WEST, lcarMaxAcc, 10, SpringLayout.WEST, car);
		carLayout.putConstraint(SpringLayout.NORTH, lcarMaxAcc, 25, SpringLayout.NORTH, lcarMaxSpeed);
		carLayout.putConstraint(SpringLayout.WEST, lcarColor, 10, SpringLayout.WEST, car);
		carLayout.putConstraint(SpringLayout.NORTH, lcarColor, 25, SpringLayout.NORTH, lcarMaxAcc);

		tcarMaxSpeed = new JTextField(10);
		tcarMaxAcc = new JTextField(10);
		tcarMaxSpeed.setText("100");
		tcarMaxAcc.setText("10");
		tcarMaxSpeed.setToolTipText("km/h");
		tcarMaxAcc.setToolTipText("from 0 to 100 km/h in sec");
		colorChooser = new JComboBox<String>(colorsS);
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
		carLayout.putConstraint(SpringLayout.WEST, ldriverPrefSpeed, 10, SpringLayout.WEST, car);
		carLayout.putConstraint(SpringLayout.NORTH, ldriverPrefSpeed, 25, SpringLayout.NORTH, driverTitle);
		carLayout.putConstraint(SpringLayout.WEST, ldriverRange, 10, SpringLayout.WEST, car);
		carLayout.putConstraint(SpringLayout.NORTH, ldriverRange, 25, SpringLayout.NORTH, ldriverPrefSpeed);
		carLayout.putConstraint(SpringLayout.WEST, ldriverSafety, 10, SpringLayout.WEST, car);
		carLayout.putConstraint(SpringLayout.NORTH, ldriverSafety, 25, SpringLayout.NORTH, ldriverRange);
		
		tdriverPrefSpeed = new JTextField(10);
		tdriverRange = new JTextField(10);
		tdriverSafety = new JTextField(10);
		tdriverPrefSpeed.setText("80");
		tdriverRange.setText("1.0");
		tdriverSafety.setText("0.5");
		tdriverPrefSpeed.setToolTipText("km/h");
		tdriverRange.setToolTipText("km");
		tdriverSafety.setToolTipText("km");		
		car.add(tdriverPrefSpeed);
		car.add(tdriverRange);
		car.add(tdriverSafety);
		carLayout.putConstraint(SpringLayout.WEST, tdriverPrefSpeed, 120, SpringLayout.WEST, car);
		carLayout.putConstraint(SpringLayout.NORTH, tdriverPrefSpeed, 25, SpringLayout.NORTH, driverTitle);
		carLayout.putConstraint(SpringLayout.WEST, tdriverRange, 120, SpringLayout.WEST, car);
		carLayout.putConstraint(SpringLayout.NORTH, tdriverRange, 25, SpringLayout.NORTH, ldriverPrefSpeed);
		carLayout.putConstraint(SpringLayout.WEST, tdriverSafety, 120, SpringLayout.WEST, car);
		carLayout.putConstraint(SpringLayout.NORTH, tdriverSafety, 25, SpringLayout.NORTH, ldriverRange);
		
		
		bAddDriverProfile = new JButton("Add");
		bAddDriverProfile.addActionListener(this);
		car.add(bAddDriverProfile);

		carLayout.putConstraint(SpringLayout.EAST, bAddDriverProfile, 0, SpringLayout.EAST, tdriverSafety);
		carLayout.putConstraint(SpringLayout.NORTH, bAddDriverProfile, 25, SpringLayout.NORTH, ldriverSafety);
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
		blockLayout.putConstraint(SpringLayout.WEST, lblockDuration, 10, SpringLayout.WEST, block);
		blockLayout.putConstraint(SpringLayout.NORTH, lblockDuration, 25, SpringLayout.NORTH, blockTitle);

		tblockDuration = new JTextField(10);
		tblockDuration.setText("10");
		tblockDuration.setToolTipText("sec");
		block.add(tblockDuration);
		blockLayout.putConstraint(SpringLayout.WEST, tblockDuration, 120, SpringLayout.WEST, block);
		blockLayout.putConstraint(SpringLayout.NORTH, tblockDuration, 25, SpringLayout.NORTH, blockTitle);

		lLaneSpan = new JLabel("Lane span: ");
		block.add(lLaneSpan);
		blockLayout.putConstraint(SpringLayout.WEST, lLaneSpan, 10, SpringLayout.WEST, block);
		blockLayout.putConstraint(SpringLayout.NORTH, lLaneSpan, 25, SpringLayout.NORTH, lblockDuration);

		Integer[] laneSpan = new Integer[Surface.getLaneCount()];
		for (int i = 0; i < laneSpan.length; i++) {
			laneSpan[i] = i+1;
		}
		cbLaneSpan = new JComboBox<Integer>(laneSpan);
		cbLaneSpan.setSelectedIndex(0);
		cbLaneSpan.setToolTipText("?");
		block.add(cbLaneSpan);
		blockLayout.putConstraint(SpringLayout.WEST, cbLaneSpan, 120, SpringLayout.WEST, block);
		blockLayout.putConstraint(SpringLayout.NORTH, cbLaneSpan, 25, SpringLayout.NORTH, lblockDuration);
	}
	
	public void setLogPanel(){
		tLog = new JTextArea();
		tLog.setEditable(false);
		logScroll = new JScrollPane(tLog);
		log.add(logScroll);
	}
	
	public void setCharts(){
		chartFrame = new JFrame();
		chartFrame.setTitle("Traffic jam simulator - Chart of the cars speed");
		chartFrame.setSize(500, 300);
		chartFrame.setDefaultCloseOperation(HIDE_ON_CLOSE);
		boolean success = false;
		switch(cbCharts.getSelectedIndex()){
			case 0:
				success = setCarSpeedChart();
				break;
			case 1:
				success = setProfileSpeedChart();
				break;
			case 2:
			default:
				success = setStatusChart();
				break;				
		}
		if(success){
			chartFrame.setVisible(true);
		}
	}
	
	private boolean setStatusChart() {
		if(!c.driverStatusChartData.isEmpty()){
			int maxSize = 0;
			DateFormat df = new SimpleDateFormat("mm:ss");
		    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
			for(Date date : c.driverStatusChartData.keySet()){			
				ArrayList<String> names = (ArrayList<String>) c.driverStatusChartData.get(date).get(0);
				ArrayList<Integer> datas = (ArrayList<Integer>) c.driverStatusChartData.get(date).get(1);
				
				for (int j = 0; j < names.size(); j++) {
					String name = names.get(j);
					Integer data = datas.get(j);
					String dateString = df.format(date);
					dataset.addValue(data, name, dateString);
						
				}	
				if(names.size() > maxSize){
					maxSize = names.size();
				}
			}    
		    
			JFreeChart chart = ChartFactory.createStackedBarChart(
					"Distribution of the drivers statuses", 
					"Time", 
					"Number of Cars", 
					dataset);
		    ChartPanel chartPanel = new ChartPanel(chart);
		    
		    JPanel content = new JPanel(new BorderLayout());
	        content.add(chartPanel);
	        chartPanel.setPreferredSize(new java.awt.Dimension(750, 520));
	        chartFrame.setContentPane(content);
	        return true;
		}
		return false;
	}

	private boolean setProfileSpeedChart() {
		if(!c.profileSpeedChartData.isEmpty()){
			series = new HashMap<>();
			dataset = new ArrayList<>();
			series.put("Random Data", new TimeSeries("Random Data"));
		    dataset.add(new TimeSeriesCollection(series.get("Random Data")));
		    JFreeChart chart = ChartFactory.createTimeSeriesChart(
		            "Average speed of the cars in driver profiles", 
		            "Time", 
		            "Speed(km/h)",
		            dataset.get(0), 
		            true, 
		            true, 
		            false
		        );
		    plot = chart.getXYPlot();
		    ValueAxis axis = plot.getDomainAxis();
		    axis.setAutoRange(true);
			Object[] array =  c.profileSpeedChartData.keySet().toArray();
			double range = ((Millisecond)array[array.length-1]).getFirstMillisecond() - ((Millisecond)array[0]).getFirstMillisecond();
		    axis.setFixedAutoRange(range);  // 60 seconds
		    axis = plot.getRangeAxis();
		    axis.setRange(0.0, 200.0); 
		    ChartPanel chartPanel = new ChartPanel(chart);
		    
		    JPanel content = new JPanel(new BorderLayout());
		    content.add(chartPanel);
		    chartPanel.setPreferredSize(new java.awt.Dimension(750, 520));
		    chartFrame.setContentPane(content);
		    setProfileSpeedChartData();
		    return true;
		}
		return false;
	}

	private void setProfileSpeedChartData() {
		int i = 0;
		series = new HashMap<>();
		dataset = new ArrayList<>();
		for(Millisecond ms : c.profileSpeedChartData.keySet()){
			
			ArrayList<String> names = (ArrayList<String>) c.profileSpeedChartData.get(ms).get(0);
			ArrayList<Float> datas = (ArrayList<Float>) c.profileSpeedChartData.get(ms).get(1);
			
			for (int j = 0; j < names.size(); j++) {
				String name = names.get(j);
				Float data = datas.get(j);
				if(series.get(name) == null){
					Color color =  HighWay.getDriverProfile(name).getColor();
					series.put(name, new TimeSeries(name));
					dataset.add(new TimeSeriesCollection(series.get(name)));
					plot.setDataset(i, dataset.get(i));
					plot.setRenderer(i, new StandardXYItemRenderer());
					plot.getRendererForDataset(plot.getDataset(i)).setSeriesPaint(0, color);
					i++;
				} 
				series.get(name).add(ms, data);
			}			
		}
	}

	private boolean setCarSpeedChart() {
		if(!c.carSpeedChartData.isEmpty()){
			series = new HashMap<>();
			dataset = new ArrayList<>();
			series.put("Random Data", new TimeSeries("Random Data"));
		    dataset.add(new TimeSeriesCollection(series.get("Random Data")));
		    JFreeChart chart = ChartFactory.createTimeSeriesChart(
		            "Speed of the cars", 
		            "Time", 
		            "Speed(m/s)",
		            dataset.get(0), 
		            false, 
		            true, 
		            false
		        );
		    plot = chart.getXYPlot();
	        ValueAxis axis = plot.getDomainAxis();
	        axis.setAutoRange(true);
			Object[] array =  c.carSpeedChartData.keySet().toArray();
			double range = ((Millisecond)array[array.length-1]).getFirstMillisecond() - ((Millisecond)array[0]).getFirstMillisecond();
	        axis.setFixedAutoRange(range);  // 60 seconds
	        axis = plot.getRangeAxis();
	        axis.setRange(0.0, 200.0); 
		    ChartPanel chartPanel = new ChartPanel(chart);
		    
		    JPanel content = new JPanel(new BorderLayout());
	        content.add(chartPanel);
	        chartPanel.setPreferredSize(new java.awt.Dimension(750, 520));
	        chartFrame.setContentPane(content);
		    setCarSpeedChartData();
		    return true;
		}
		return false;
	}

	private void setCarSpeedChartData() {
		int i = 0;
		series = new HashMap<>();
		dataset = new ArrayList<>();
		for(Millisecond ms : c.carSpeedChartData.keySet()){
			
			ArrayList<String> names = (ArrayList<String>) c.carSpeedChartData.get(ms).get(0);
			ArrayList<Float> datas = (ArrayList<Float>) c.carSpeedChartData.get(ms).get(1);
			ArrayList<Color> colors = (ArrayList<Color>) c.carSpeedChartData.get(ms).get(2);
			for (int j = 0; j < names.size(); j++) {
				String name = names.get(j);
				Float data = datas.get(j);
				Color color = colors.get(j);
				if(series.get(name) == null){
					series.put(name, new TimeSeries(name));
					dataset.add(new TimeSeriesCollection(series.get(name)));
					plot.setDataset(i, dataset.get(i));
					plot.setRenderer(i, new StandardXYItemRenderer());
					plot.getRendererForDataset(plot.getDataset(i)).setSeriesPaint(0, color);
					i++;
				} 
				series.get(name).add(ms, data);
			}			
		}
	}

//	public void newChartData(Object[] name, Object[] data, Object[] color){
//		Millisecond now = new Millisecond();
//		for (int i = 0; i < name.length; i++) {
//			if( name[i] != null){
//				if(i >= series.size()){
//					series.add(new TimeSeries((String)name[i]));
//					dataset.add(new TimeSeriesCollection(series.get(i)));
//					plot.setDataset(i, dataset.get(i));
//					plot.setRenderer(i, new StandardXYItemRenderer());
//					plot.getRenderer().setSeriesPaint(i, (Color)color[i]);
//				}
//				if(!series.get(i).getKey().equals((String)name[i])){
//					series.get(i).setKey((String)name[i]);
//					plot.getRenderer().setSeriesPaint(i, (Color)color[i]);
//				}
//				series.get(i).add(now, (Float)data[i]);
////				System.out.println("plotColor: " + plot.getRenderer().getSeriesPaint(i) + " carColor: " + color[i]);
//				plot.getRenderer().getSeriesPaint(i);
//			}
//		}
//	}
	
	
	
	public void typeSelected(String type) {
	    CardLayout cl = (CardLayout)(options.getLayout());
	    cl.show(options, type);
	}
	
	public void setRoadObjects(ArrayList<RoadObject> ro){
		surface.setRoadObjects(ro);
		lcarCount.setText("There are " + ro.size() + " cars on the highway.");
	}
	
	public void reFresh(){
		surface.reFresh();
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() instanceof JComboBox){
			JComboBox<?> cb = (JComboBox<?>)e.getSource();	        
	        if(cb == typeChooser){
	        	String type = (String)cb.getSelectedItem();	        	
	        	typeSelected(type);
	        }
		}	
		if(e.getSource() instanceof JTextField){
			JTextField tf = (JTextField)e.getSource();
	        String text = (String)tf.getText();
	        if(tf == tNewCarTimer){
		        if("".equals(text))
		        	text = new String("500");
				c.setNewCarTime(Float.parseFloat(text));
				if(!Float.toString(c.getNewCarTime()).equals(tNewCarTimer.getText()))
					tNewCarTimer.setText(Float.toString(c.getNewCarTime()));
	        } else if(tf == tTimeWarp){
		        if("".equals(text))
		        	text = new String("20");
				c.setTimeWarp(Float.parseFloat(text));
				if(!Float.toString(c.getTimeWarp()).equals(tTimeWarp.getText()))
					tTimeWarp.setText(Float.toString(c.getTimeWarp()));
	        }
		}
		if(e.getSource() instanceof JButton){
			JButton b = (JButton)e.getSource();
			String text = b.getText();

			if("Restart".equals(text)){				
				c.restart();
			} else if("Pause".equals(text)){
				bPause.setText("Start");
				c.stop();
			} else if("Start".equals(text)){
				bPause.setText("Pause");
				c.stop();
			}else if(b == bChart){
				bPause.setText("Start");
				if(c.s == Status.RUNNING)
					c.stop();
				setCharts();				
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
		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File file = fc.getSelectedFile();
		        	if(file != null)
		        		c.LoadGame(file);
		        } 
				if(c.s != s)
					c.stop();	
			} else if(b == bcAccept && selected instanceof Car){
	        	((Car)selected).setDriverProfile(HighWay.getDriverProfile((String)dpChooser.getSelectedItem()));
	        	setSelected();
				c.hw.modifyCar((Car)selected);	
			} else if(b == bbAccept){
				c.hw.modifyBlock(selected.id, Float.parseFloat(stblockDuration.getText()));
			} else if(b == bcDel || b == bbDel){
				c.hw.deleteObject(selected);
			} else if(b == bAddDriverProfile){
				String name = tcarName.getText();
				Float maxSpeed = Float.parseFloat(tcarMaxSpeed.getText());
				Float maxAcc = Float.parseFloat(tcarMaxAcc.getText());
				Color color = colorsC[colorChooser.getSelectedIndex()];
				Float prefSpeed = Float.parseFloat(tdriverPrefSpeed.getText());
				Float range = Float.parseFloat(tdriverRange.getText());
				Float safetyGap = Float.parseFloat(tdriverSafety.getText());
				if(name == null || maxSpeed == null || maxAcc == null 
						|| prefSpeed == null || range == null || safetyGap == null ){
					
				}else{
					c.hw.addDriverProfile(name, maxSpeed, maxAcc, color, prefSpeed, range, safetyGap);
				}
				setProfilesPanel();
			}
		}
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {   	
		if(e.getSource() instanceof JComboBox){
			JComboBox<?> cb = (JComboBox<?>)e.getSource();
			if(cb == cbLanes){
				Integer lane = (Integer) e.getItem();
				Surface.setLaneCount(lane);	  
				setLanes();
			} 
		}
	}
	
	public void setLanes(){
		cbLaneSpan.removeAllItems();				
    	Integer[] lanes = new Integer[Surface.getLaneCount()];
		for (int i = 0; i < lanes.length; i++) {
			cbLaneSpan.addItem(i+1);
		}
		cbLaneSpan.setSelectedIndex(0);
    	c.restart();
	}
	
	public void setLog(){
		String newRow = null;
		String type = null; 
		for (int i = 1; i <= RoadObject.count; i++) {
			type = c.hw.getType(i);
			if(type != null && "Car".equals(type)){
				if("driving".equals(c.hw.getStatus(i)))
					newRow = "Car " + Integer.toString(i) + " is " + c.hw.getStatus(i) + " with speed: " + c.hw.getSpeed(i) +".";
				else
					newRow = "Car " + Integer.toString(i) + " is " + c.hw.getStatus(i) + ".";
			}
			if(type != null && "Block".equals(type)){
				newRow = "Block " + Integer.toString(i) + " has " + c.hw.getStatus(i) + " seconds left.";
			}
			if(newRow != null){
				try {
					if(tLog.getDocument().getLength()>2000000){
						tLog.getDocument().remove(1000000, 1000000);
					}
					tLog.getDocument().insertString(0, newRow + System.lineSeparator(), null);
				} catch (BadLocationException e) {					
					e.printStackTrace();
				}   
			}
		}
	}
	public Point2D.Float reConvertCoords(Point2D.Float position){
		return new Point2D.Float(position.x - (size/2), size - (position.y + (size/2)));
	}
	
	public void setSelected(){
		if(selected != null){
			if(selected instanceof Car){
				profilesSplit.setLeftComponent(scar);
//				select.remove(sblock);
//				select.add(scar);							
				selectedCarObject.setText("ID: " + selected.id);
				lCarStatus.setText("Status: " + ((Car) selected).getDriver().getStatus().toString());
				stcarMaxSpeed.setText(Float.toString(((Car) selected).getMaxSpeed()));
				stcarMaxAcc.setText(Float.toString(((Car) selected).getMaxAcc()));
				for (int i = 0; i < colorsC.length; i++){
					if(colorsC[i].getRed() == ((Car) selected).getColor().getRed() &&
						colorsC[i].getGreen() == ((Car) selected).getColor().getGreen() &&
						colorsC[i].getBlue()== ((Car) selected).getColor().getBlue())
					{
						scolorChooser.setText(colorsS[i]);
						break;
					}
				}
				stdriverPrefSpeed.setText(Float.toString(((Car) selected).getDriver().getPrefSpeed()));
				stdriverRange.setText(Float.toString(((Car) selected).getDriver().getRangeOfView()));
				stdriverSafety.setText(Float.toString(((Car) selected).getDriver().getSafetyGap()));

				dpChooser.removeAllItems();
				for(String name : getDriverProfiles()){
					dpChooser.addItem(name);
					if(name.equals(((Car) selected).getName())){
						dpChooser.setSelectedItem(name);	
					}
				}
				
			}
			else if(selected instanceof Block){
				profilesSplit.setLeftComponent(sblock);
//				select.remove(scar);
//				select.add(sblock);
				selectedBlockObject.setText("Block " + selected.id);
				stblockDuration.setText(Float.toString(((Block) selected).duration));
			}
			
		}
		SwingUtilities.updateComponentTreeUI(this);
		selectSplit.invalidate();
		selectSplit.repaint();
		selectSplit.revalidate();
	}

	public Road getRoad() {
		return new Road(surface.ovalBorder, surface.firstBorder, 
				surface.firstOvalPartSize, surface.firstRectPartSize, 
				surface.laneSize, surface.getStartPosition(),
				surface.circleCenter, Surface.getLaneCount());
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		Point2D.Float newPoint = new Point2D.Float();
		newPoint.x = e.getX();
		newPoint.y = e.getY();
		selected = surface.getObject(newPoint);
		if(selected == null && typeChooser.getSelectedItem().equals(BLOCKPANEL)){
			if(!"".equals(tblockDuration.getText())){
				int lane = surface.getNewObjectsLane(newPoint);
				int span = (Integer) cbLaneSpan.getSelectedItem();
				Point2D.Float roadPoint = surface.getRoadPoint(newPoint, lane);
				c.hw.addBlock(roadPoint, lane, Float.parseFloat(tblockDuration.getText()));
				int count = 1;
				int i = 1;
				while(count != span){
					int actLane = lane + i;
					if(actLane > 0 && actLane <= Surface.getLaneCount()){
						roadPoint = surface.getRoadPoint(newPoint, actLane);
						c.hw.addBlock(roadPoint, actLane, Float.parseFloat(tblockDuration.getText()));
						count++;
					}
					i = i * (-1);
					if(i>0){
						i++;
					}
				}
			}
			selected = surface.getObject(newPoint);
		}
		setSelected();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}


}
