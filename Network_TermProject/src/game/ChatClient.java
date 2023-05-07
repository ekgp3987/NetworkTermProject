package game;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

public class ChatClient implements Runnable {

	private static int PORT = 5555; // 서버포트번호
	private static String IP = "192.168.136.1"; // 서버아이피주소
	private Socket socket; // 소켓
	private User user; // 사용자

	public LoginUI login;
	public RestRoomUI restRoom;
	private DataInputStream dis = null;
	private DataOutputStream dos = null;
	public boolean ready = false;
	
	ChatClient() {
		login = new LoginUI(this);
		// 스레드 시작
		Thread thread = new Thread(this);
		thread.start();
	}

	public static void main(String[] args) {
		System.out.println("Client start...");
		new ChatClient();
	}

	@Override
	public void run() {
		//
		// 소켓 통신 시작
		//
		while (!ready) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// 사용자가 객체 생성 및 아이피설정
		user = new User(dis, dos);
		user.setIP(socket.getInetAddress().getHostAddress());

		// 메시지 리딩
		while (true) {
			try {
				String receivedMsg = dis.readUTF(); // 메시지 받기(대기)
				dataParsing(receivedMsg); // 메시지 해석
			} catch (IOException e) {
				e.printStackTrace();
				try {
					user.getDis().close();
					user.getDos().close();
					socket.close();
					break;
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}

		errorMsg("Server program is already closed.");
		// 채팅프로그램 종료
		restRoom.dispose();
	}

	public boolean serverAccess() {
		if (!ready) {
			// 소켓이 연결이 이루어지지 않은 경우에만 실행
			// 즉, 처음 연결시에만 실행
			socket = null;
			IP = login.ipBtn.getText();
			try {
				// 서버접속
				InetSocketAddress inetSockAddr = new InetSocketAddress(
						InetAddress.getByName(IP), PORT);
				socket = new Socket();

				// 지정된 주소로 접속 시도 (3초동안)
				socket.connect(inetSockAddr, 3000);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// 접속이 되면 실행
			if (socket.isBound()) {
				// 입력, 출력 스트림 생성
				try {
					dis = new DataInputStream(socket.getInputStream());
					dos = new DataOutputStream(socket.getOutputStream());
				} catch (IOException e) {
					e.printStackTrace();
				}
				ready = true;
			}
		}
		return ready;
	}
	
	
	// 데이터를 구분
	public synchronized void dataParsing(String data) {
		StringTokenizer token = new StringTokenizer(data, "/"); // 토큰 생성
		String protocol = token.nextToken(); // 토큰으로 분리된 스트링
		String id, pw, rNum, nick, rName, msg, result;
		System.out.println("Received data : " + data);

		switch (protocol) {
		case User.LOGIN: // 로그인
			// 사용자가 입력한(전송한) 아이디와 패스워드
			result = token.nextToken();
			if (result.equals("OK")) {
				nick = token.nextToken();
				login(nick);
			} else {
				msg = token.nextToken();
				errorMsg(msg);
			}
			break;
		case User.LOGOUT:
			logout();
			break;
		case User.MEMBERSHIP: // 회원가입 승인
			result = token.nextToken();
			if (result.equals("OK")) {
				errorMsg("Successfully registered!");
			} else {
				errorMsg("This ID is already registered.");
			}
			break;
		case User.INVITE: // 초대받기
			id = null;
			// 한명씩 초대
			while (token.hasMoreTokens()) {
				// 초대할 사람의 아이디와 방번호
				id = token.nextToken();
				rNum = token.nextToken();
				invite(id, rNum);
			}
			break;
		case User.UPDATE_USERLIST: // 대기실 사용자 목록
			userList(token);
			break;
		case User.UPDATE_ROOM_USERLIST: // 채팅방 사용자 목록
			// 방번호읽기
			rNum = token.nextToken();
			userList(rNum, token);
			break;
		case User.UPDATE_SELECTEDROOM_USERLIST: // 대기실에서 선택한 채팅방의 사용자 목록
			selectedRoomUserList(token);
			break;

		case User.UPDATE_ROOMLIST: // 방 목록
			roomList(token);
			break;
		case User.CHANGE_NICK: // 닉네임 변경(대기실)
			nick = token.nextToken();
			changeNick(nick);
			break;
		case User.ECHO01: // 대기실 에코
			msg = token.nextToken();
			echoMsg(msg);
			break;
		case User.ECHO02: // 채팅방 에코
			rNum = token.nextToken();
			msg = token.nextToken();
			echoMsgToRoom(rNum, msg);
			break;
		case User.WHISPER: // 귓속말
			id = token.nextToken();
			nick = token.nextToken();
			msg = token.nextToken();
			whisper(id, nick, msg);
			break;
			
			
			
		//추가 프로토콜
		case User.GAMEMOVE:
			id = token.nextToken();
		}
	}

	private void logout() {
		try {
			restRoom.dispose();
			user.getDis().close();
			user.getDos().close();
			socket.close();
			restRoom = null;
			user = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 채팅방 내부 사용자 리스트
	private void userList(String rNum, StringTokenizer token) {
		for (int i = 0; i < user.getRoomArray().size(); i++) {
			if (Integer.parseInt(rNum) == user.getRoomArray().get(i)
					.getRoomNum()) {

				// 기존에 리스트가 있을 경우 지워줌
				if (user.getRoomArray().get(i).getrUI().model != null)
					user.getRoomArray().get(i).getrUI().model
							.removeAllElements();

				while (token.hasMoreTokens()) {
					// 아이디와 닉네임을 읽어서 유저 객체 하나를 생성
					String id = token.nextToken();
					String nick = token.nextToken();
					User tempUser = new User(id, nick);

					user.getRoomArray().get(i).getrUI().model.addElement(tempUser.toString());
				}
			}
		}
	}

	// 선택한 채팅방의 사용자 리스트
	private void selectedRoomUserList(StringTokenizer token) {
		// 서버로부터 유저리스트(채팅방)를 업데이트하라는 명령을 받음

		if (!restRoom.level_2_1.isLeaf()) {
			// 리프노드가 아니고, 차일드가 있다면 모두 지움
			restRoom.level_2_1.removeAllChildren();
		}
		while (token.hasMoreTokens()) {
			// 아이디와 닉네임을 읽어서 유저 객체 하나를 생성
			String id = token.nextToken();
			String nick = token.nextToken();
			User tempUser = new User(id, nick);

			// 채팅방 사용자노드에 추가
			restRoom.level_2_1.add(new DefaultMutableTreeNode(tempUser
					.toString()));
		}
		restRoom.userTree.updateUI();
	}

	// 대기실 사용자 리스트
	private void userList(StringTokenizer token) {
		// 서버로부터 유저리스트(대기실)를 업데이트하라는 명령을 받음

		if (restRoom == null) {
			return;
		}

		if (!restRoom.level_2_2.isLeaf()) {
			// 리프노드가 아니고, 차일드가 있다면 모두 지움
			restRoom.level_2_2.removeAllChildren();
		}
		while (token.hasMoreTokens()) {
			// 아이디와 닉네임을 읽어서 유저 객체 하나를 생성
			String id = token.nextToken();
			String nick = token.nextToken();
			User tempUser = new User(id, nick);

			for (int i = 0; i < restRoom.userArray.size(); i++) {
				if (tempUser.getId().equals(restRoom.userArray.get(i))) {
				}
				if (i == restRoom.userArray.size()) {
					// 배열에 유저가 없으면 추가해줌
					restRoom.userArray.add(tempUser);
				}
			}
			// 대기실 사용자노드에 추가
			restRoom.level_2_2.add(new DefaultMutableTreeNode(tempUser
					.toString()));
		}
		restRoom.userTree.updateUI();
	}

	// 서버로부터 방리스트를 업데이트하라는 명령을 받음
	private void roomList(StringTokenizer token) {
		String rNum, rName;
		Room room = new Room();

		// 기존에 리스트가 있을 경우 지워줌
		if (restRoom.model != null) {
			restRoom.model.removeAllElements();
		}

		while (token.hasMoreTokens()) {
			rNum = token.nextToken();
			rName = token.nextToken();
			int num = Integer.parseInt(rNum);

			// 라스트룸넘버를 업데이트 (최대값+1)
			if (num >= restRoom.lastRoomNum) {
				restRoom.lastRoomNum = num + 1;
			}
			room.setRoomNum(num);
			room.setRoomName(rName);

			restRoom.model.addElement(room.toProtocol());
		}
	}

	private void errorMsg(String string) {
		int i = JOptionPane.showConfirmDialog(null, string, "Event occurred.",
				JOptionPane.CLOSED_OPTION, JOptionPane.INFORMATION_MESSAGE);
		// 확인 누르면 종료
		if (i == 0) {

		}
	}

	private void login(String nick) {
		// 로그인정보 가져옴
		user.setId(login.idText.getText());
		user.setNickName(nick);

		// 로그인창 닫고 대기실창 열기
		login.dispose();
		restRoom = new RestRoomUI(ChatClient.this);
		restRoom.lb_id.setText(user.getId());
		restRoom.lb_ip.setText(user.getIP());
		restRoom.lb_nick.setText(user.getNickName());
	}

	
	
	//whispers + gameRequest
	private void whisper(String id, String nick, String msg) {
		if(msg.compareTo("TICTACTOEGAMEREQUESTTRANSFER")==0) {			
			int select = JOptionPane.showConfirmDialog(null, id+" send a match request.\nAccept?","Game Request has arrived",
					JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
			if(select==JOptionPane.YES_OPTION) {
				restRoom.chatField.setText("/"+id+" YESIWANTTOPLAYTICTACTOE");
				TicTacToe.startGame(user.getIP(),22222);
			}
			else if(select==JOptionPane.NO_OPTION) {
				restRoom.chatField.setText("/"+id+" NOIDONTWANTTOPLAYTICTACTOE");
			}
			else {
				//pass.
			}
		}
		
		if(msg.compareTo("YESIWANTTOPLAYTICTACTOE")==0) {
			TicTacToe.startGame(user.getIP(),22222);
//			TicTacToe ticTacToe = new TicTacToe(user.getIP(),7778);
			System.out.println("Start tictactoe game...");
			System.out.println(user.getIP());
			restRoom.restRoomArea.append(nick+"("+id+")'s whispering: "+msg+"\n");
		}
		
		else {
			restRoom.restRoomArea.append(nick+"("+id+")'s whispering: "+msg+"\n");
		}
	}

	
	private void invite(String id, String rNum) {
	}

	private void changeNick(String nick) {
		user.setNickName(nick);
		restRoom.lb_nick.setText(nick);
	}

	private void echoMsg(String msg) {
		// 커서 위치 조정
		if (restRoom != null) {
			restRoom.restRoomArea.setCaretPosition(restRoom.restRoomArea
					.getText().length());
			restRoom.restRoomArea.append(msg + "\n");
		}
	}

	private void echoMsgToRoom(String rNum, String msg) {
		for (int i = 0; i < user.getRoomArray().size(); i++) {
			if (Integer.parseInt(rNum) == user.getRoomArray().get(i)
					.getRoomNum()) {

				// 사용자 -> 방배열 -> 유아이 -> 텍스트에어리어
				// 커서 위치 조정
				user.getRoomArray().get(i).getrUI().chatArea
						.setCaretPosition(user.getRoomArray().get(i).getrUI().chatArea
								.getText().length());
				// 에코
				user.getRoomArray().get(i).getrUI().chatArea.append(" "+ msg + "\n");
			}
		}
	}

	// getter, setter
	public static int getPORT() {
		return PORT;
	}

	public static void setPORT(int pORT) {
		PORT = pORT;
	}

	public static String getIP() {
		return IP;
	}

	public static void setIP(String iP) {
		IP = iP;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public LoginUI getLogin() {
		return login;
	}

	public void setLogin(LoginUI login) {
		this.login = login;
	}

	public RestRoomUI getRestRoom() {
		return restRoom;
	}

	public void setRestRoom(RestRoomUI restRoom) {
		this.restRoom = restRoom;
	}

	public DataInputStream getDis() {
		return dis;
	}

	public void setDis(DataInputStream dis) {
		this.dis = dis;
	}

	public DataOutputStream getDos() {
		return dos;
	}

	public void setDos(DataOutputStream dos) {
		this.dos = dos;
	}

	public boolean isReady() {
		return ready;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}
}
