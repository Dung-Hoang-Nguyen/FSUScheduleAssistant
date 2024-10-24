package various;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicButtonUI;

public class ScheduleAssistant extends JFrame{
/**
 * 
 */
private static final long serialVersionUID = 1L;
Color backColor,textColor;
Scanner blockScan;
JTextArea text;
JButton generate;
JPanel classesPanel, schedulesPanel, filterPanel, panel;
JTabbedPane tabs;
JScrollPane scroll;
String infoTypes[]= {"[A-Z]{3}[0-9]{4}L?","[0-9]{4}","[A-Z]{3}","([1-9]|[1-9][0-9]|[1-9][0-9][0-9])"
		,"[A-Za-z/-]+","(M|Th|W|T|F)+",/*"[A-Z]{3}","[0-9]{4}",*/"([A-Za-z]+,)|Online","[A-Za-z]+","[0-6]"};
ArrayList<Class> classes,allClasses;
ArrayList<String> filters;
public ArrayList<Schedule> schedules;
public static void main(String[] args) {
	SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
            new ScheduleAssistant(Color.WHITE,new File("ScheduleBlocks.txt")).setVisible(true);
	        }
	    });
	}
public ScheduleAssistant(Color c, File f){
	setTitle("Schedule Assistant");
	//setIcon();
	setLayout(new BorderLayout());
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	setBounds(25, 100, 1200, 1000);
	backColor=c;
	setBackground(backColor);
    getContentPane().setBackground(backColor);
    //inverse of screen color
    textColor = new Color(255-backColor.getRed(),255-backColor.getGreen(),255-backColor.getBlue());
    try {allClasses=toList(f);} catch (FileNotFoundException e) {
//		e.printStackTrace();
		System.out.print("File not found");
	}
    classes=new ArrayList<Class>(allClasses);
    tabs=new JTabbedPane();
    classesPanel = new JPanel(new GridLayout(0, 1));
    classesPanel.setOpaque(false);
    panel=new JPanel(new BorderLayout());
    scroll = new JScrollPane(panel);
    Border bord = BorderFactory.createLineBorder(backColor, 20);
    panel.setBackground(backColor);
    panel.add(classesPanel,BorderLayout.LINE_START);
    scroll.setPreferredSize(new Dimension(450, 110));
    scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    tabs.addTab("Schedule Assistant",scroll);
    normalize(scroll);
    tabs.setOpaque(false);
    add(tabs, BorderLayout.CENTER);
    for(int i=0;i<allClasses.size();i++) {
    	final int index=i;
    	JCheckBox box = new JCheckBox("",true);
    	box.addItemListener(new ItemListener() {    
            public void itemStateChanged(ItemEvent e) {
	        	if(e.getStateChange()==1){
	        		classes.add(allClasses.get(index));
	        	}
	        	else {
	        		classes.remove(allClasses.get(index));
	        	}
            }    
         });
    	JButton view=new JButton(allClasses.get(i).info());
    	view.addActionListener( new ActionListener(){
    		@Override
    		public void actionPerformed(ActionEvent e) {
    			if(tabs.indexOfTab(allClasses.get(index).info())>-1) {
    				tabs.setSelectedIndex(tabs.indexOfTab(allClasses.get(index).info()));
    				return;
    			}
    			JPanel pane=new JPanel(new GridLayout(0,1));
    			for(Block bl:allClasses.get(index)) {
    				JCheckBox box = new JCheckBox("",bl.active?true:false);
    		    	box.addItemListener(new ItemListener() {    
    		            public void itemStateChanged(ItemEvent e) {
    			        	if(e.getStateChange()==1){
    			        		bl.active=true;
    			        	}
    			        	else {
    			        		bl.active=false;
    			        	}
    		            }    
    		         });
    		    	box.setOpaque(false);
    		    	JTextArea info=new JTextArea(bl.toString());
    		    	JPanel row=new JPanel(new FlowLayout(FlowLayout.LEFT));
    		    	row.setOpaque(false);
    		    	row.add(box);
    		    	row.add(info);
    		    	pane.add(row);
    			}
    			pane.setBackground(backColor);
    			pane.setBorder(bord);
    			JScrollPane tabScroll=new JScrollPane(pane);
    			tabScroll.setBackground(backColor);
    			tabScroll.setPreferredSize(new Dimension(450, 110));
    		    tabScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    		    tabScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    		    
    		    
    		    JPanel title=tabTitle(allClasses.get(index).info());
    			tabs.insertTab(allClasses.get(index).info(),new ImageIcon(), tabScroll, "Configure blocks",1);
    			tabs.setTabComponentAt(tabs.indexOfComponent(tabScroll), title);
    			fixScrolling(tabScroll);
    			tabs.setSelectedIndex(tabs.indexOfTab(allClasses.get(index).info()));
    		}
    	});
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        view.setOpaque(false);
        view.setContentAreaFilled(false);
        box.setOpaque(false);
        box.setContentAreaFilled(false);
        row.add(box);
        row.add(view);
        row.setOpaque(false);
        classesPanel.add(row);
    }
    classesPanel.setBorder(bord);
    generate=new JButton("Generate Schedules");
    generate.setContentAreaFilled(false);
    generate.setPreferredSize(new Dimension(200,25));
    generate.setFocusable(false);
    generate.addActionListener(new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent e) {
			if(classes.isEmpty()) {System.out.println("Error: Please select a class");return;}
			if(schedulesPanel!=null &&schedulesPanel.getParent()!=null) {panel.remove(schedulesPanel);}
			schedules=new ArrayList<Schedule>();
		    possibleSchedules(new Schedule(),0);
		    schedulesPanel=new JPanel(new GridLayout(0,1));
		    schedulesPanel.setOpaque(false);
		    schedulesPanel.add(new JLabel("Found "+schedules.size()+" schedules"));
		    for(int i=0;i<schedules.size();i++) {
		    	final int index=i;
		    	JButton but = new JButton("peek");
		    	but.addActionListener( new ActionListener(){
		    		@Override
		    		public void actionPerformed(ActionEvent e) {
		    			JPanel sche = new JPanel(new BorderLayout());
		    			JScrollPane tabScroll=new JScrollPane(sche);
		    		    tabScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		    		    tabScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		    		    tabScroll.setFocusable(false);
		    		    tabScroll.setBorder(BorderFactory.createEtchedBorder());
		    			sche.setBackground(backColor);
		    			JPanel top=new JPanel(new GridLayout(0, 1));
		    			Schedule s=schedules.get(index);
		    		    for(Class c:s.keySet()) { //add info to top pane
		    		        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
		    		        JLabel title=new JLabel(c.info());
		    		        JTextArea info=new JTextArea(c.get(s.get(c)).toString());
		    		        info.setOpaque(false);
		    	            info.setFocusable(false);
		    		        info.setOpaque(false);
		    		        row.setOpaque(false);
		    		        row.setBorder(BorderFactory.createEmptyBorder());
		    		        row.add(title);
		    		        row.add(info);
		    		        top.add(row);
		    		    }
		    			JPanel blocks=new JPanel();
		    		    JPanel times=new JPanel(new GridLayout(0,1));
		    		    JPanel daysPane=new JPanel();
		    		    
	    		        top.setOpaque(false);
	    		        top.setBorder(bord);
	    		        int botLength=25*50;
		    			String days[]= {"Monday","Tuesday","Wednessday","Thursday","Friday"};
		    			JPanel bot=new JPanel(){
		    				private static final long serialVersionUID = 1L;
		    				@Override
		    				public void paintComponent(Graphics g) {
		    				    super.paintComponent(g);
		    					Graphics2D g2 = (Graphics2D) g;
		    					int width=getBounds().width;
		    					int center=(width)/2;
		    					int textWidth = (int) Math.floor((double)(width-200) / 5)-10;
		    			        for (Component comp : daysPane.getComponents()) {
		    			            if (comp instanceof JTextField) {
		    			                comp.setPreferredSize(new Dimension(textWidth, 25));
		    			            }
		    			        }
		    			        blocks.setBounds(center-(int)(2.5*textWidth+15),25,width,botLength);
				    			daysPane.setBounds(0,0,width,25);
		    			        revalidate();
		    					g2.setColor(textColor);
		    					g2.setStroke(new BasicStroke(2));
		    					g2.drawRect(center-(int)(2.5*textWidth+15),0,5*textWidth+30,botLength);
		    					g2.drawRect(center-(int)(1.5*textWidth+7.5),0,3*textWidth+15,botLength);
		    					g2.drawRect(center-(int)(0.5*textWidth+2.5),0,textWidth+5,botLength);
		    					g2.setStroke(new BasicStroke(1));
		    					for(double i=25;i<botLength;i+=botLength/51) {
			    					g2.drawLine(center-(int)(2.5*textWidth+15),(int)i,center+(int)(2.5*textWidth+15),(int)i);
		    					}
		    					g2.setStroke(new BasicStroke(2));
		    					for (Class c:s.keySet()) {
		    						for(TimeBlock t:c.get(s.get(c))) {
		    							switch(t.day) {
		    							case "M": t.text.setBounds(0,(int)(25+botLength/51*t.getStart()),(int)(textWidth+8),(int)(25+botLength/51*(t.getStart()-t.getStart())));break;
		    							case "T": t.text.setBounds((int)(textWidth+6),(int)(25+botLength/51*t.getStart()),(int)(textWidth+6),(int)(25+botLength/51*(t.getStart()-t.getStart())));break;
		    							case "W": t.text.setBounds((int)(textWidth+6)*2,(int)(25+botLength/51*t.getStart()),(int)(textWidth+6),(int)(25+botLength/51*(t.getStart()-t.getStart())));break;
		    							case "Th": t.text.setBounds((int)(textWidth+6)*3,(int)(25+botLength/51*t.getStart()),(int)(textWidth+7.5),(int)(25+botLength/51*(t.getStart()-t.getStart())));break;
		    							case "F": t.text.setBounds((int)(textWidth+6)*4,(int)(25+botLength/51*t.getStart()),(int)(textWidth+7.5),(int)(25+botLength/51*(t.getStart()-t.getStart())));break;
		    							}
		    						}
		    					}
		    				}
		    			};
		    			bot.setLayout(null);
		    			blocks.setLayout(null);
		    			blocks.setOpaque(false);
		    			blocks.setBounds(100,25,1000,botLength);
    					for (Class c:s.keySet()) {
    						Color randCol= new Color((int)(255*Math.random()),(int)(255*Math.random()),(int)(255*Math.random()),(int)(255*Math.random()));
    						for(TimeBlock t:c.get(s.get(c))) {
    							t.text=new JTextField(c.info()+"-"+String.format("%04d",c.get(s.get(c)).section));
    							t.text.setBackground(randCol);
    							t.text.setEditable(false);
    							t.text.setForeground(textColor);
    							t.text.setHorizontalAlignment(JTextField.CENTER);
    							t.text.setFocusable(false);
    							t.text.setOpaque(true);
    							t.text.setHighlighter(null);
    							blocks.add(t.text);
    						}
    					}
		    			times.setBounds(75,5,100,botLength+20);
		    			for(int i=8;i<=20;i++) {
    						for(int j=0;j<=3;j++) {
    							JLabel temp=new JLabel(String.format("%02d",i)+":"+String.format("%02d", (j*15)));
    							if(j==0) {
    								temp.setFont(new Font(Font.MONOSPACED,Font.PLAIN,10));
    							}
    							else {
    								temp.setText(" "+temp.getText());
    								temp.setFont(new Font(Font.MONOSPACED,Font.PLAIN,8));
    							}
    							times.add(temp);
    							
    						}
    					}
		    			times.setOpaque(false);
		    			for(String day:days) {
		    				JTextField temp=new JTextField();
		    				temp.setText(day);
		    				temp.setPreferredSize(new Dimension(200,25));
		    				temp.setHorizontalAlignment(JTextField.CENTER);
		    				temp.setOpaque(false);
		    				temp.setBorder(BorderFactory.createEmptyBorder());
			                temp.setFocusable(false);
			                temp.setFont(new Font(Font.MONOSPACED,Font.PLAIN,10));
		    				daysPane.add(temp);
		    			}
		    			daysPane.setOpaque(false);
		    			JButton randomColor=new JButton("newColor");
		    			randomColor.addActionListener( 
			    	            new ActionListener() { 
			    	                @Override
			    	                public void actionPerformed(ActionEvent e) {
			        					for (Class c:s.keySet()) {
			        						Color randCol= new Color((int)(255*Math.random()),(int)(255*Math.random()),(int)(255*Math.random()),(int)(255*Math.random()));
			        						for(TimeBlock t:c.get(s.get(c))) {
			        							t.text.setBackground(randCol);
			        							repaint();
			        						}
			        					}
			    	                }
			    	    });
		    			randomColor.setBounds(0,0,100,25);
		    			randomColor.setBorder(BorderFactory.createEtchedBorder());
		    			randomColor.setContentAreaFilled(false);
		    			randomColor.addMouseListener(paintBorder());

		    			bot.add(randomColor);
    					bot.add(times);
		    			bot.add(daysPane);
    					bot.add(blocks);
		    			bot.setPreferredSize(new Dimension(1000,botLength+100));
	    		        bot.setOpaque(false);

		    		    
	    		        sche.add(top,BorderLayout.PAGE_START);
		    		    sche.add(bot,BorderLayout.PAGE_END);
		    		    
		    			JPanel title=tabTitle("Schedule "+(index+1));
		    			tabs.addTab("Schedule"+(index+1),new ImageIcon(), tabScroll, schedules.get(index).info1());
		    			tabs.setTabComponentAt(tabs.indexOfComponent(tabScroll), title);
		    			fixScrolling(tabScroll);
		    			tabs.setSelectedIndex(tabs.indexOfComponent(tabScroll));
		    		}
		    	});
		    	
		    	but.setContentAreaFilled(false);
		        JLabel schedule = new JLabel(index+1+". "+schedules.get(index).info1());
		        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
		        row.add(but);
		        row.add(schedule);
		        //row.setAlignmentX(Component.LEFT_ALIGNMENT);
		        row.setOpaque(false);
		        schedulesPanel.add(row);
		    }
		    schedulesPanel.setBorder(bord);
		    panel.add(schedulesPanel,BorderLayout.PAGE_END);
		    revalidate();
		    repaint();
		}
	});
    filters=new ArrayList<String>();
    filterPanel=new JPanel(new GridLayout(0,1));
    JButton newFilter=new JButton("New filter");
    newFilter.addActionListener(new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent e) {
			JPanel row=new JPanel(new FlowLayout(FlowLayout.LEFT));
			String choices[]= {"Within Time","Not Within Time","Containing Note","Not Containing Note"};
    		JComboBox<String> filt=new JComboBox<>(choices);
    		JCheckBox active=new JCheckBox("",true);
    		active.setOpaque(false);
    		active.addItemListener(new ItemListener() {    
                public void itemStateChanged(ItemEvent e) {
                	if(e.getStateChange()==1){
    	        		
    	        	}
    	        	else {
    	        		
    	        	}
                }    
             });
    		JButton remove=new JButton("x");
    		remove.setFont(new Font(Font.MONOSPACED,Font.PLAIN,11));
    		remove.setPreferredSize(new Dimension(25,25));
    		remove.setToolTipText("remove filter");
            remove.setContentAreaFilled(false);
            remove.setFocusable(false);
            remove.setBorder(BorderFactory.createEtchedBorder());
            remove.setBorderPainted(false);
            remove.addMouseListener(paintBorder());
    		active.setPreferredSize(new Dimension(25,25));
    		filt.setPreferredSize(new Dimension(150,25));
    		row.add(remove);
    		row.add(active);
    		row.add(filt);
    		row.setOpaque(false);
    		row.setPreferredSize(new Dimension(100,25));
    		filterPanel.add(row);
    		remove.addActionListener(new ActionListener(){
    			@Override
    			public void actionPerformed(ActionEvent e) {
    				filterPanel.remove(row);
    				repaint();
    			}
    		});
    		revalidate();
    		repaint();
    	}
    });
    newFilter.setPreferredSize(new Dimension(175,25));
    JButton search=new JButton("Search");
    
    search.setPreferredSize(new Dimension(175,25));
    JPanel filterButs=new JPanel();
    filterButs.setOpaque(false);
    filterButs.add(newFilter);
    filterButs.add(search);
    filterPanel.setOpaque(false);
    
    filterPanel.add(filterButs);
    panel.add(filterPanel,BorderLayout.LINE_END);
    fixScrolling(scroll);
    classesPanel.add(generate);
    revalidate();
    repaint();
}
public void normalize(JComponent comp) {
	comp.setOpaque(false);
	comp.setFocusable(false);
	comp.setBorder(BorderFactory.createEmptyBorder());
	comp.setForeground(textColor);
}
public JPanel tabTitle(String name) {
JPanel out= new JPanel();
JLabel la = new JLabel(name);
JButton remove = new JButton("x"); 
remove.setUI(new BasicButtonUI());
remove.setToolTipText("close tab");
remove.setContentAreaFilled(false);
remove.setFocusable(false);
remove.setBorder(BorderFactory.createEtchedBorder());
remove.setBorderPainted(false);
remove.addMouseListener(paintBorder());
remove.setRolloverEnabled(true);
remove.addActionListener( 
    new ActionListener() { 
        @Override
        public void actionPerformed(ActionEvent e) {
            int i = tabs.indexOfTabComponent(out);
            if (i != -1) {
            tabs.remove(i);
            }
        }
});
out.add(la);
out.add(remove);
out.setOpaque(false);
return out;
}
public MouseAdapter paintBorder() {
return new MouseAdapter() {
    public void mouseEntered(MouseEvent e) {
        Component component = e.getComponent();
        if (component instanceof AbstractButton) {
            AbstractButton button = (AbstractButton) component;
            button.setBorderPainted(true);
        }
    }

    public void mouseExited(MouseEvent e) {
        Component component = e.getComponent();
        if (component instanceof AbstractButton) {
            AbstractButton button = (AbstractButton) component;
            button.setBorderPainted(false);
        }
    }
};
}
public static void fixScrolling(JScrollPane scrollpane) {
    JLabel systemLabel = new JLabel();
    FontMetrics metrics = systemLabel.getFontMetrics(systemLabel.getFont());
    int lineHeight = metrics.getHeight();
    int charWidth = metrics.getMaxAdvance();
            
    JScrollBar systemVBar = new JScrollBar(JScrollBar.VERTICAL);
    JScrollBar systemHBar = new JScrollBar(JScrollBar.HORIZONTAL);
    int verticalIncrement = systemVBar.getUnitIncrement();
    int horizontalIncrement = systemHBar.getUnitIncrement();
            
    scrollpane.getVerticalScrollBar().setUnitIncrement(lineHeight * verticalIncrement);
    scrollpane.getHorizontalScrollBar().setUnitIncrement(charWidth * horizontalIncrement);
}
public String getPossibleSchedules(){
	String out="";
	schedules=new ArrayList<Schedule>();
    possibleSchedules(new Schedule(),0);
    out+=("FOUND: "+schedules.size()+" schedules\n");
    for(int i=0;i<schedules.size();i++) {
    	out+=(i+1+"\n"+schedules.get(i).info());
    }
    return out;
}
class Filter{
	boolean active;
	String type;
	String mod;
	public Filter(String t,String m) {
		active=true;
		type=t;
		mod=m;
	}
	public boolean filter() {
		switch(type) {
		case "Within Time":
			if()
			break;
		case "Not Within Time":
			
			break;
		case "Containing Note":
			
			break;
		case "Not Containing Note":
			
			break;
		}
		return true;
	}
}
class TimeBlock{
	String day;
	int startMin, startHour;
	int endMin, endHour;
	String room;
	JTextField text;
	public TimeBlock(){}
	public TimeBlock(String d, String s, String e, String r) {
		day=d;
		//12:00am
		startHour=Integer.valueOf(s.substring(0,s.length()-5));
		if (startHour==12) {
			startHour=0;
		}
		startMin=Integer.valueOf(s.substring(s.length()-4,s.length()-2));
		if (s.substring(s.length()-2).equals("pm")) {
			startHour+=12;
		}
		endHour=Integer.valueOf(e.substring(0,e.length()-5));
		if (endHour==12) {
			endHour=0;
		}
		endMin=Integer.valueOf(e.substring(e.length()-4,e.length()-2));
		if (e.substring(e.length()-2).equals("pm")) {
			endHour+=12;
		}
		room=r;
	}
	public TimeBlock(String d, String s, String e) {
		this(d,s,e,"");
	}
	public String toString() {
		return day+" "+String.format("%02d",startHour)+":"+String.format("%02d",startMin)+"-"+String.format("%02d",endHour)+":"+String.format("%02d",endMin)+" "+room;
	}
	public double getStart() {
		return startHour+(double)startMin/60;
	}
	public double getEnd() {
		return endHour+(double)endMin/60;
	}
	public boolean overlaps(TimeBlock other) {
		double thisStart=this.startHour+(double)this.startMin/60;
		double otherStart=other.startHour+(double)other.startMin/60;
		double thisEnd=this.endHour+(double)this.endMin/60;
		double otherEnd=other.endHour+(double)other.endMin/60;
		//System.out.println(thisStart+"-"+thisEnd+" and "+otherStart+"-"+otherEnd);
		if(this.day.equals(other.day)&&((thisStart<=otherEnd&&thisStart>=otherStart)||(thisEnd<=otherEnd&&thisEnd>=otherStart)||
				(otherStart<=thisEnd&&otherStart>=thisStart)||(otherEnd<=thisEnd&&otherEnd>=thisStart)||
				thisStart==otherStart||thisEnd==otherEnd)) {
			return true;
		}
		return false;
	}
}
class Block extends ArrayList<TimeBlock>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	boolean active;
	int section;
	String type;
	int seats;
	String intructor="";
	String note="";
	public Block() {active=true;}
	public Block(int s, String t, int a, String i) {
		section=s;
		type=t;
		seats=a;
		intructor=i;
		active=true;
	}
	public void section(int s) {
		section=s;
	}
	public void type(String t) {
		type=t;
	}
	public void seats(int s) {
		seats=s;
	}
	public void intructor(String i) {
		intructor+=i;
	}
	public void note(String n) {
		note=n;
	}
	public String toString() {
		String out=section+" "+type+" "+seats+" "+intructor;
		for(TimeBlock t:this) {
			out+="\n    "+t;
		}
		return out+ ((note.equals(""))? "":"\n"+note);
	}
	public void filter() {
		
	}
	public boolean overlaps(Block other) {
		for(TimeBlock i:this) {
			for(TimeBlock j:other) {
				if(i.overlaps(j)) {
					return true;
				}
			}
		}
		return false;
	}
}
class Class extends ArrayList<Block>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String group;
	String number;
	String name;
	public Class(String n) {
		group=n.substring(0,3);
		number=n.substring(3);
	}
	public String toString() {
		String out=group+number+"\n";
		for(Block b:this) {
			out+=b+"\n\n";
		}
		return out;
	}
	public String info() {
		return group+number;
	}
	public boolean blockOverlaps(Class other, int thisBlock,int otherBlock) {
		return this.get(thisBlock).overlaps(other.get(otherBlock));
	}
}
class Schedule extends HashMap<Class,Integer>{
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	public boolean isValid() {
		for(Class c:this.keySet()) {
			if(!c.get(this.get(c)).active) {
				return false;
			}
			for (Class cl:this.keySet()) {
				if(c.equals(cl)) {continue;}
				if(c.blockOverlaps(cl,this.get(c),this.get(cl))) {
					return false;
				}
			}
		}
		return true;
	}
	public String info() {
		String out="";
		for(Class c:this.keySet()) {
			out+=c.info()+" - "+c.get(this.get(c))+"\n";
		}
		return out;
	}
	public String info1() {
		String out="";
		for(Class c:this.keySet()) {
			out+=c.info()+"-"+String.format("%04d",c.get(this.get(c)).section)+", ";
		}
		return out.substring(0,out.length()-2);
	}
	public String toString() {
		String out="";
		for(Class c:this.keySet()) {
			out+=c+"\n"+c.get(this.get(c));
		}
		return out;
	}
}
public void possibleSchedules(Schedule s, int i){
	if(s.isValid()){
		if(i==classes.size()) {
			Schedule newSchedule = new Schedule();
		    newSchedule.putAll(s);
		    schedules.add(newSchedule);
		    //System.out.println("I WORK!"+schedules.size()+"\n"+schedules.get(schedules.size()-1).info());
		}
		else{
			for(int j=0;j<classes.get(i).size();j++){
				s.put(classes.get(i),j);
				possibleSchedules(s,i+1);
				s.remove(classes.get(i));
			}
		}
	}
}
private ArrayList<Class> toList(File f) throws FileNotFoundException {
Scanner scan=new Scanner(f);
ArrayList<Class> out= new ArrayList<Class>();
int sequence = 0;
String temp=scan.next();
while(scan.hasNext()) {
	//System.out.println(sequence+". "+temp);
	if(temp.equals("Special")) {
		out.get(out.size()-1).get(out.get(out.size()-1).size()-1).note(temp+" "+scan.nextLine().trim());
		if(scan.hasNext()) {temp=scan.next();}
		//System.out.println("FoundNote: "+out.get(out.size()-1).get(out.get(out.size()-1).size()-1).note);
	}
	if(temp.equals("Reserve")) {
		out.get(out.size()-1).get(out.get(out.size()-1).size()-1).note(temp+" "+scan.nextLine().trim()+" "+scan.nextLine().trim());
		if(scan.hasNext()) {temp=scan.next();}
		//System.out.println("FoundNote: "+out.get(out.size()-1).get(out.get(out.size()-1).size()-1).note);
	}
	else if (temp.matches(infoTypes[sequence])) {
		//System.out.println("BeforeSwitch"+sequence);
		switch (sequence) {
		case 0: out.add(new Class(temp)); temp=scan.next();sequence++;break;
		case 1: 
			out.get(out.size()-1).add(new Block());
			out.get(out.size()-1).get(out.get(out.size()-1).size()-1).section(Integer.valueOf(temp));
			temp=scan.next();sequence++; break;
		case 2: 
			out.get(out.size()-1).get(out.get(out.size()-1).size()-1).type(temp);
			temp=scan.next(); sequence++;break;
		case 3: 
			out.get(out.size()-1).get(out.get(out.size()-1).size()-1).seats(Integer.valueOf(temp));
			temp=scan.next(); sequence++;break;
		case 4: 
			if (temp.matches("(Th|M|T|W|F)+|Online")) {sequence++;continue;}
			out.get(out.size()-1).get(out.get(out.size()-1).size()-1).intructor(temp);
			temp=scan.next(); break;
		case 5: 
			String day=temp;
			Pattern pattern = Pattern.compile("Th|M|T|W|F");
			Matcher matcher = pattern.matcher(day);
			String start=scan.next();
			scan.next();
			String end=scan.next();
			temp=scan.next();
			if (temp.equals("-")) {
				String building=scan.next();
				String roomNum=scan.next();
				while (matcher.find())
				{
					int e=matcher.start();
					//System.out.println(e+day.substring(e,e+1)+start+end+building+roomNum);
					if (e+2<=day.length()&&day.substring(e,e+2).equals("Th")) {
						out.get(out.size()-1).get(out.get(out.size()-1).size()-1).add(new TimeBlock("Th",start,end,building+roomNum));
					}
					else {
						out.get(out.size()-1).get(out.get(out.size()-1).size()-1).add(new TimeBlock(day.substring(e,e+1),start,end,building+roomNum));
					}
				}
				temp=scan.next();
			}
			else {
				while (matcher.find())
				{
					int e=matcher.start();
					//System.out.println(e+day.substring(e,e+1)+start+end);
					if (e+2<=day.length()&&day.substring(e,e+2).equals("Th")) {
						out.get(out.size()-1).get(out.get(out.size()-1).size()-1).add(new TimeBlock("Th",start,end));
					}
					else {
						out.get(out.size()-1).get(out.get(out.size()-1).size()-1).add(new TimeBlock(day.substring(e,e+1),start,end));
					}
				}
			}
			break;
		case 6: 
			temp=scan.next(); break;
		case 7: 
			temp=scan.next(); break;
		case 8: 
			temp=scan.next(); sequence=0;break;
		}
	}
	else if(temp.matches(infoTypes[sequence+1])) {
		sequence++;
	}
	else if(temp.matches(infoTypes[sequence-1])) {
		sequence--;
	}
	else{
		System.out.println("ERROR: No match in range, invalid formatting");
		System.exit(0);
	}
}
return out;
}

}
