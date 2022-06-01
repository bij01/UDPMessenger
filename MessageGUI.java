import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;


class MessageGUI extends JFrame {
	JPanel mainPanel, topPanel, topPanel1, topPanel2, bottomPanel, textPanel;
	JLabel receiverL, senderL;
	JFrame frame;
	JTextField sendertf;
	JTextArea textarea, textarea2;
	JScrollPane sp1, sp2;
	JButton sendButton;
	JComboBox<String> combobox;
	HashMap<String, String> nameList = new HashMap<String, String>();
	String msg;
	FileWriter fw;
	FileReader fr;
	BufferedReader br;
	Container container;
	GraphicsDevice gd;
	int x_position;
	int y_position;

	UDPServer us;
	TrayIcon trayIcon;

	MessageGUI(){
		setDefault(300, 350);
		setTop();
		setBottom();
		readFile();
		makeEvent();
		setTrayIcon();
		setVisible(true);
		makeMessageFrame();
		us = new UDPServer(this);
		us.start();
	}

	void makeEvent(){ //윈도우 이벤트 추가
		addWindowListener(new WindowListener(){
			public void windowActivated(WindowEvent e) {
				//Invoked when the Window is set to be the active Window.
				//System.out.println("active");
			}
			public void windowClosed(WindowEvent e) {
				//Invoked when a window has been closed as the result of calling dispose on the window.
				//System.out.println("dispose");
			}
			public void windowClosing(WindowEvent e){
				//Invoked when the user attempts to close the window from the window's system menu.
				//System.out.println("closing");
				String[] options = {"최소화", "종료"};
				int option = JOptionPane.showOptionDialog(null, "프로그램을 종료할까요?", "프로그램 종료 안내",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
				switch (option){
				case 0: //최소화
					setVisible(false);
					break;
				case 1: //종료
					System.exit(0);
				}
			}
			public void windowDeactivated(WindowEvent e){
				//Invoked when a Window is no longer the active Window.
				//System.out.println("not active");
			}
			public void windowDeiconified(WindowEvent e){
				//Invoked when a window is changed from a minimized to a normal state.
				//System.out.println("normal from minimized");
			}
			public void windowIconified(WindowEvent e){
				//Invoked when a window is changed from a normal to a minimized state.
				//System.out.println("minimized");
			}
			public void windowOpened(WindowEvent e){
				//Invoked the first time a window is made visible.
				//System.out.println("first time visible");
			}
		});
	}
	
	void setTrayIcon() { //트레이아이콘 설정
		Image trayImage;
		
		PopupMenu popup = new PopupMenu();
		MenuItem menu1 = new MenuItem("restore");
		MenuItem menu2 = new MenuItem("exit");
		menu1.addActionListener(e -> setVisible(true));
		menu2.addActionListener(e -> System.exit(0));
		popup.add(menu1);
		popup.add(menu2);
		
		trayImage = Toolkit.getDefaultToolkit().getImage("mailbox.png");
		trayIcon = new TrayIcon(trayImage, "chatting Tray", popup);
		trayIcon.setToolTip("메신저");
		trayIcon.setImageAutoSize(true);
		if(SystemTray.isSupported()) {
			SystemTray tray = SystemTray.getSystemTray();
			try {
				tray.add(trayIcon);
			} catch (Exception e) {
				System.out.println(e + "에러");
			}
		}
	}

	void setDefault(int w_width, int w_height){
		gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		
		//윈도우 우측 끝으로 띄우기
		x_position = gd.getDisplayMode().getWidth();
		x_position = x_position - w_width + 6;
		y_position = 0;

		setBounds(x_position, y_position, w_width, w_height);
		setTitle("Message Client");
		setResizable(true);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
	}

	void setTop(){ //상단화면 설정(받는 사람, 보내는 사람)
		mainPanel = new JPanel();
		mainPanel.setBorder(new EmptyBorder(10, 10, 0, 10));
		mainPanel.setLayout(new BorderLayout());
		topPanel = new JPanel();
		topPanel.setLayout(new GridLayout(1, 2));
		combobox = new JComboBox<String>();
		combobox.setPreferredSize(new Dimension(100, 20));
		combobox.setEditable(true);
		topPanel1 = new JPanel();
		topPanel1.setLayout(new BorderLayout());
		topPanel1.add(combobox);
		topPanel1.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.RAISED), "받는사람"));
		sendertf = new JTextField();
		topPanel2 = new JPanel();
		topPanel2.setLayout(new BorderLayout());
		topPanel2.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.RAISED), "보내는사람"));
		topPanel2.add(sendertf);
		topPanel.add(topPanel1);
		topPanel.add(topPanel2);
		mainPanel.add(topPanel, BorderLayout.NORTH);
	}

	void setBottom(){ //하단화면 설정(메시지입력, 전송 버튼)
		bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		textPanel = new JPanel();
		textPanel.setLayout(new BorderLayout());
		textPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.RAISED), "보낼 메시지"));
		textarea = new JTextArea();
		sp1 = new JScrollPane(textarea);
		sendButton = new JButton("보내기");
		sendButton.addActionListener(e -> {
			us.sendMessage();
			saveIndex();
		});
		textPanel.add(sp1, BorderLayout.CENTER);
		textPanel.add(sendButton, BorderLayout.SOUTH);
		bottomPanel.add(textPanel, BorderLayout.CENTER);
		mainPanel.add(bottomPanel, BorderLayout.CENTER);
		add(mainPanel);
	}

	void readFile(){ //파일 읽어서 콤보박스에 리스트 추가 및 해쉬맵에 아이피 주소 등록
		String fileName = "ips.txt";
		String line;
		String fileName2 = "lastTarget.txt";
		try{
			fr = new FileReader(fileName);
			br = new BufferedReader(fr);
			while ((line = br.readLine()) !=null){
				line = line.trim();
				String lines[] = line.split(" ");
				nameList.put(lines[0], lines[1]);
				combobox.addItem(lines[0]);
			}
			fr = new FileReader(fileName2); //최근에 메시지 보낸 상대
			br = new BufferedReader(fr);
			if ((line = br.readLine()) != null){
				try{
					int index = Integer.parseInt(line);
					combobox.setSelectedIndex(index);
				}catch (NumberFormatException nfe){
					System.out.println(nfe);
				}
			} else {
			}
		}catch (FileNotFoundException fne){
			msg = fileName + " 파일을 찾을 수 없습니다.";
			showMessage(msg, "파일 에러");
		}catch (IOException io1){
			msg = io1 + "";
			showMessage(msg, "IOException");
		}finally{
			try{
				if (br!=null) br.close();
				if (fr!=null) fr.close();
			}catch (IOException io2){
				msg = io2 + "";
				showMessage(msg, "IOException");
			}
		}
	}

	void saveIndex(){
		String fileName2 = "lastTarget.txt";
		try{
		
			fw = new FileWriter(fileName2);
			int index = combobox.getSelectedIndex();
			String str = Integer.toString(index);
			fw.write(str);
			fw.flush();
		}catch(IOException io){
		}finally{
			try {	
				if (fw != null) fw.close();
			}catch(IOException io2){
			}
		}
	}

	void showMessage(String msg, String title){
		JOptionPane.showMessageDialog(null, msg, title, JOptionPane.INFORMATION_MESSAGE);
	}
	
	void makeMessageFrame(){
		int width, height;
		frame = new JFrame();
		frame.setTitle("메시지 전송화면");
		frame.setResizable(true);
		width = 400;
		height = 500;
		x_position = (gd.getDisplayMode().getWidth()/2);
		y_position = (gd.getDisplayMode().getHeight()/2);
		x_position = x_position - (width/2);
		y_position = y_position - (height/2);
		frame.setBounds(x_position, y_position, width, height);
		frame.setDefaultCloseOperation(HIDE_ON_CLOSE);
		textarea2 = new JTextArea();
		sp2 = new JScrollPane(textarea2);
		frame.setLayout(new BorderLayout());
		frame.add(sp2);
		frame.setVisible(false);
	}
	public static void main(String[] args) {
		new MessageGUI();
	}
}