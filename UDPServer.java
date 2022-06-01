import java.io.*;
import java.net.*;

class UDPServer extends Thread {
	int port = 7000;
	DatagramSocket ds1, ds2;
	DatagramPacket dp1, dp2;
	InetAddress ia1, ia2;
	FileReader fr;
	BufferedReader br;
	String fileName = "ips.txt";
	String msg;
	String clientIp, ip;

	MessageGUI gui;

	UDPServer(MessageGUI gui){
		this.gui = gui;
	}
	public void run(){
		init();
	}
	
	void init(){
		try{
			String NameFrom;
			ds1 = new DatagramSocket(port);
			//System.out.println(port+"번 포트에서 UDP서버 대기중...");
			while(true){
				try{	
					byte bs[] = new byte[8192];
					dp1 = new DatagramPacket(bs, bs.length);
					ds1.receive(dp1);
					InetAddress ia1 = dp1.getAddress();
					clientIp = ia1.getHostAddress();
					msg = new String(bs);
					msg = msg.trim();
					int end = msg.indexOf(":");
					NameFrom = msg.substring(0, end);
					NameFrom = "From " + NameFrom;
					msg = msg.substring(end+1);
					if (msg.length() > 100){
						// asdkfjasldkfjaskldfj
						gui.textarea2.append(msg);
						gui.frame.setTitle(NameFrom);
						gui.frame.setVisible(true);
					} else {
						gui.showMessage(msg, NameFrom);
					}
				}catch(IOException io){
					gui.showMessage(io + "", "IO 에러");
				}
			}
		}catch(SocketException se){
			gui.showMessage(se + "", "Socket 에러");
		}finally{
			if(ds1 != null) ds1.close();
		}
	}

	void sendMessage(){
		Object obj;
		String targetName;
		String senderName;
		try{
			ds2 = new DatagramSocket();
			obj = gui.combobox.getSelectedItem();
			senderName = gui.sendertf.getText().trim();
			if (senderName.length() == 0) {
				gui.showMessage("보내는 사람을 입력해주세요", "안내메세지");
			} else {
				if (gui.textarea.getText().length() == 0) {
					gui.showMessage("보낼 메시지를 입력해주세요", "안내메세지");
				} else {
					targetName = obj.toString();
					if (!gui.nameList.containsKey(targetName)){
						gui.showMessage(targetName + "님은 등록되지 않은 사용자입니다. 다시 한번 확인해주세요.", "안내메시지");
					} else {
						ip = gui.nameList.get(targetName);
						ip = "192.168.219.101";
						ia2 = InetAddress.getByName(ip);
						msg = gui.textarea.getText();
						msg = msg.trim();
						msg = senderName + ":" + msg;
						byte bs[] = msg.getBytes();
						dp2 = new DatagramPacket(bs, bs.length, ia2, port);
						ds2.send(dp2);
						gui.showMessage(targetName + "님에게 메시지를 전송하였습니다.", "전송 완료");
						gui.textarea.setText("");
					}
				}
			}
		}catch(SocketException se){
			gui.showMessage("메시지를 전송할 수 없습니다. 아이피를 확인해주세요", "Socket 에러");
		}catch(UnknownHostException ue){
			gui.showMessage(ue + "", "UnknownHost 에러");
		}catch(IOException io){
			gui.showMessage(io + "클라이언트", "IOException 에러");
		}finally{
			if(ds2 != null) ds2.close();
		}
	}

	public static void main(String[] args) {
		//new UServer();
	}
}