package game;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.JTextArea;

public class ServerThread implements Runnable {

	private ArrayList<User> userArray; // ������ ������ ����ڵ�
	private ArrayList<Room> roomArray; // ������ ������� ä�ù��
	private User user; // ���� ������� �����(������ ������) �����
	private JTextArea jta;
	private boolean onLine = true;
	String executePath = System.getProperty("user.dir");

	private DataOutputStream thisUser;

	ServerThread(JTextArea jta, User person, ArrayList<User> userArray,
			ArrayList<Room> roomArray) {
		this.roomArray = roomArray;
		this.userArray = userArray;
		this.userArray.add(person); // �迭�� ����� �߰�
		this.user = person;
		this.jta = jta;
		this.thisUser = person.getDos();
	}

	@SuppressWarnings("finally")
	@Override
	public void run() {
		DataInputStream dis = user.getDis(); // �Է� ��Ʈ�� ���

		while (onLine) {
			try {
				String receivedMsg = dis.readUTF(); // �޽��� �ޱ�(���)
				dataParsing(receivedMsg); // �޽��� �ؼ�
				jta.append("Success : Message Read -" + receivedMsg + "\n"); //���� : �޽��� ���� -
				jta.setCaretPosition(jta.getText().length());
			} catch (IOException e) {
				try {
					user.getDis().close();
				} catch (IOException e1) {
					e1.printStackTrace();
				} finally {
					jta.append("Error : ServerThread - read failed\n"); //���� : ����������-�б� ����\n
					break;
				}
			}
		}
	}

	// �����͸� ����
	public synchronized void dataParsing(String data) {
		StringTokenizer token = new StringTokenizer(data, "/"); // ��ū ����
		String protocol = token.nextToken(); // ��ū���� �и��� ��Ʈ���� ���ڷ�
		String id, pw, rNum, nick, rName, msg;
		System.out.println("Data received by Server : " + data); //������ ���� ������

		switch (protocol) {
		case User.LOGIN: // �α���
			// ����ڰ� �Է���(������) ���̵�� �н�����
			id = token.nextToken();
			pw = token.nextToken();
			login(id, pw);
			break;
		case User.LOGOUT: // �α׾ƿ�
			logout();
			break;
		case User.MEMBERSHIP: // ȸ������
			id = token.nextToken();
			pw = token.nextToken();
			member(id, pw);
			break;
		case User.INVITE: // �ʴ��ϱ�
			id = null;
			// �Ѹ��� �ʴ�
			while (token.hasMoreTokens()) {
				// �ʴ��� ����� ���̵�� ���ȣ
				id = token.nextToken();
				rNum = token.nextToken();
				invite(id, rNum);
			}
			break;
		case User.UPDATE_USERLIST: // ���� ����� ���
			userList(thisUser);
			break;
		case User.UPDATE_ROOM_USERLIST: // ä�ù� ����� ���
			// ���ȣ�б�
			rNum = token.nextToken();
			userList(rNum, thisUser);
			break;
		case User.UPDATE_SELECTEDROOM_USERLIST: // ���ǿ��� ������ ä�ù��� ����� ���
			// ���ȣ�б�
			rNum = token.nextToken();
			selectedRoomUserList(rNum, thisUser);
			break;

		case User.UPDATE_ROOMLIST: // �� ���
			roomList(thisUser);
			break;
		case User.CHANGE_NICK: // �г��� ����(����)
			nick = token.nextToken();
			changeNick(nick);
			break;
		case User.CREATE_ROOM: // �游���
			rNum = token.nextToken();
			rName = token.nextToken();
			createRoom(rNum, rName);
			break;
		case User.GETIN_ROOM:
			rNum = token.nextToken();
			getInRoom(rNum);
			break;
		case User.GETOUT_ROOM:
			rNum = token.nextToken();
			getOutRoom(rNum);
			break;
		case User.ECHO01: // ���� ����
			msg = token.nextToken();
			echoMsg(User.ECHO01 + "/" + user.toString() + " " + msg); //msg �� " "+
			break;
		case User.ECHO02: // ä�ù� ����
			rNum = token.nextToken();
			msg = token.nextToken();
			echoMsg(rNum, msg);
			break;
		case User.WHISPER: // �ӼӸ�
			id = token.nextToken();
			msg = token.nextToken();
			whisper(id, msg);
			break;
			
		//�߰� ��������
		case User.GAMEMOVE:
			id = token.nextToken();
			msg = token.nextToken();
			
			break;
		}
	}

	public void alarm() {

	}

	private void GameInputSend() {
		
	}
	
	private void getOutRoom(String rNum) {
		for (int i = 0; i < roomArray.size(); i++) {
			if (Integer.parseInt(rNum) == roomArray.get(i).getRoomNum()) {
				// �濡�� ������
				// ä�ù��� ��������Ʈ���� ����� ����
				for (int j = 0; j < roomArray.get(i).getUserArray().size(); j++) {
					if (user.getId().equals(
							roomArray.get(i).getUserArray().get(j).getId())) {
						roomArray.get(i).getUserArray().remove(j);
					}
				}

				// ������� �渮��Ʈ���� ���� ����
				for (int j = 0; j < user.getRoomArray().size(); j++) {
					if (Integer.parseInt(rNum) == user.getRoomArray().get(j)
							.getRoomNum()) {
						user.getRoomArray().remove(j);
					}
				}
				echoMsg(roomArray.get(i), user.toString() + " is Out."); //���� �����ϼ̽��ϴ�.
				userList(rNum);

				if (roomArray.get(i).getUserArray().size() <= 0) {
					roomArray.remove(i);
					roomList();
				}
			}
		}
	}

	private void getInRoom(String rNum) {
		for (int i = 0; i < roomArray.size(); i++) {
			if (Integer.parseInt(rNum) == roomArray.get(i).getRoomNum()) {
				// �� ��ü�� �ִ� ���, �濡 ������߰�
				roomArray.get(i).getUserArray().add(user);
				// ����� ��ü�� �� �߰�
				user.getRoomArray().add(roomArray.get(i));
				echoMsg(roomArray.get(i), user.toString() + " is Join."); //���� �����ϼ̽��ϴ�.
				userList(rNum);
			}
		}
	}

	private void createRoom(String rNum, String rName) {
		Room rm = new Room(rName); // ������ �������� ä�ù� ����
		rm.setMaker(user); // ���� ����
		rm.setRoomNum(Integer.parseInt(rNum)); // ���ȣ ����

		rm.getUserArray().add(user); // ä�ù濡 ����(����) �߰�
		roomArray.add(rm); // �븮��Ʈ�� ���� ä�ù� �߰�
		user.getRoomArray().add(rm); // ����� ��ü�� ������ ä�ù��� ����

		echoMsg(User.ECHO01 + "/" + user.toString() + " has opened Chatroom #" + rm.getRoomNum()
				+ "!");
		echoMsg(rm, user.toString() + " is Join."); //���� �����ϼ̽��ϴ�.
		roomList();
		userList(rNum, thisUser);
		jta.append("Success : " + userArray.toString() + " making Chatroom\n");
	}

	private void whisper(String id, String msg) {
		for (int i = 0; i < userArray.size(); i++) {
			if (id.equals(userArray.get(i).getId())) {
				// �ӼӸ� ��븦 ã����
				try {
					userArray
							.get(i)
							.getDos()
							.writeUTF(
									User.WHISPER + "/" + user.toProtocol()
											+ "/" + msg);
					jta.append("Success : Whisper Sent : " + user.toString() + " whispered to "
							+ userArray.get(i).toString() + "\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// ���� ����
	private void echoMsg(String msg) {
		for (int i = 0; i < userArray.size(); i++) {
			try {
				userArray.get(i).getDos().writeUTF(msg);
				jta.append(user.toString() + " - " + msg + "\n");
			} catch (IOException e) {
				e.printStackTrace();
				jta.append("Error : Echo failed\n");
			}
		}
	}

	// �� ���� (�� ��ȣ�� �ƴ� ���)
	private void echoMsg(String rNum, String msg) {
		for (int i = 0; i < roomArray.size(); i++) {
			if (Integer.parseInt(rNum) == roomArray.get(i).getRoomNum()) {
				echoMsg(roomArray.get(i), msg);
			}
		}
	}

	// �� ���� (�水ü�� �ִ� ���)
	private void echoMsg(Room room, String msg) {
		for (int i = 0; i < room.getUserArray().size(); i++) {
			try {
				// �濡 ������ �����鿡�� ���� �޽��� ����
				room.getUserArray()
						.get(i)
						.getDos()
						.writeUTF(
								User.ECHO02 + "/" + room.getRoomNum() + "/"
										+ msg);
				jta.append("Success : Message Sent : " + msg + "\n");
			} catch (IOException e) {
				e.printStackTrace();
				jta.append("Error : Echo failed\n");
			}
		}
	}

	// ���Ǵг��� ����
	private void changeNick(String nick) {
		File file = new File(executePath + "\\" + user.getId() + ".txt");
		FileWriter f;
		try {
			f = new FileWriter(file);
			// ���Ͽ� ȸ���������� (���̵�+�н�����+�г���)
			f.write(user.getId() + "/" + user.getPw() + "/" + nick);
			f.close();
			thisUser.writeUTF(User.MEMBERSHIP + "/OK");
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// �г����� �ߺ������
		jta.append("Success : Change Nickname : " + user.getId() + "'s Nickname => "
				+ user.getNickName() + " changed to " + nick + "!");
		user.setNickName(nick);

		try {
			user.getDos().writeUTF(User.CHANGE_NICK + "/" + nick);
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < userArray.size(); i++) {
			userList(userArray.get(i).getDos());
		}
		// �� ����� ������ ��� ���� ����ڸ���Ʈ�� ������Ʈ
		for (int i = 0; i < user.getRoomArray().size(); i++) {
			userList(String.valueOf(user.getRoomArray().get(i).getRoomNum()));
		}
	}

	private void invite(String id, String rNum) {
		for (int i = 0; i < userArray.size(); i++) {
			// �ʴ��һ���� ã�Ƽ� �ʴ�޽��� ����
			if (id.equals(userArray.get(i).getId())) {
				try {
					// �ʴ��� ����� ���̵�� ���ȣ�� ����
					userArray
							.get(i)
							.getDos()
							.writeUTF(
									User.INVITE + "/" + user.getId() + "/"
											+ rNum);
				} catch (IOException e) {
					e.printStackTrace();
					jta.append("Error : Invitation failed-" + userArray.toString() + "\n");
				}
			}
		}
	}

	private void member(String id, String pw) {
		User newUser = new User();
		newUser.setId(id);
		newUser.setPw(pw);
		
		try {
			File file = new File(executePath + "\\" + id + ".txt");
			if (!file.isFile()) {
				FileWriter f = new FileWriter(file);

				// ���Ͽ� ȸ���������� (���̵�+�н�����+�г���)
				f.write(newUser.toStringforLogin());
				f.close();
				thisUser.writeUTF(User.MEMBERSHIP + "/OK");
				jta.append("Success : Create user registration file\n");
			} else {
				// ������ �����ϴ� ���
				thisUser.writeUTF(User.MEMBERSHIP + "/fail");
				jta.append("Error : Member who already exists.\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
			try {
				thisUser.writeUTF(User.MEMBERSHIP + "/fail");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			jta.append("Error : Create user registration file\n");
		}
	}

	private void login(String id, String pw) {

		FileReader reader = null;
		int inputValue = 0;
		StringBuffer str = new StringBuffer();
		
		try {
			// ���� ����
			reader = new FileReader(executePath + "\\" + id + ".txt");
			while ((inputValue = reader.read()) != -1) {
				// ���� ����
				str.append((char) inputValue);
			}
			jta.append("Success : Read file : "+ executePath + "\\" + id + ".txt\n");
			reader.close();
			StringTokenizer token = new StringTokenizer(str.toString(), "/"); // ��ū
			// ����

			try {
				if (id.equals(token.nextToken())) {
					if (pw.equals(token.nextToken())) {
						for (int i = 0; i < userArray.size(); i++) {
							if (id.equals(userArray.get(i).getId())) {
								try {
									System.out.println("Connecting");
									thisUser.writeUTF(User.LOGIN
											+ "/fail/already Connected.");
								} catch (IOException e) {
									e.printStackTrace();
								}
								return;
							}
						}

						// �α��� OK
						user.setId(id);
						user.setPw(pw);
						user.setNickName(token.nextToken());
						thisUser.writeUTF(User.LOGIN + "/OK/"
								+ user.getNickName());
						this.user.setOnline(true);

						// ���ǿ� ����
						echoMsg(User.ECHO01 + "/" + user.toString()
								+ " is Join.");
						jta.append(id + " : is Join.\n");

						roomList(thisUser);
						for (int i = 0; i < userArray.size(); i++) {
							userList(userArray.get(i).getDos());
						}
					} else {
						thisUser.writeUTF(User.LOGIN + "/fail/Invalid password.");
						jta.append("Error : login-Invalid password. : " + pw + "\n");
					}
				} else {
					thisUser.writeUTF(User.LOGIN + "/fail/Invalid ID.");
					jta.append("Error : login-Invalid ID. : " + id + "\n");
				}
			} catch (IOException e) {
				e.printStackTrace();
				thisUser.writeUTF(User.LOGIN + "/fail/Login failed.");
				jta.append("Error : Login failed." + pw + "\n");
			}
		} catch (Exception e) {
			try {
				thisUser.writeUTF(User.LOGIN + "/fail/Invalid ID.");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			jta.append("Error : File read failed.\n");
			return;
		}

	}

	private void logout() {
		System.out.println("Logout");

		// ������������ �ٲ�
		user.setOnline(false);
		// ����ڹ迭���� ����
		for (int i = 0; i < userArray.size(); i++) {
			if (user.getId().equals(userArray.get(i).getId())) {
				System.out.println(userArray.get(i).getId() + " deleted.");
				userArray.remove(i);
			}
		}
		// room Ŭ������ ��������� ����ڹ迭���� ����
		for (int i = 0; i < roomArray.size(); i++) {
			for (int j = 0; j < roomArray.get(i).getUserArray().size(); j++) {
				if (user.getId().equals(
						roomArray.get(i).getUserArray().get(j).getId())) {
					roomArray.get(i).getUserArray().remove(j);
				}
			}
		}
		echoMsg(User.ECHO01 + "/" + user.toString() + " is Out.");

		for (int i = 0; i < userArray.size(); i++) {
			userList(userArray.get(i).getDos());
		}

		jta.append(user.getId() + " : is Out.\n");

		try {
			user.getDos().writeUTF(User.LOGOUT);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			user.getDis().close();
			user.getDos().close();
			user = null;
			jta.append("Success : Stream closed.\n");
		} catch (IOException e) {
			e.printStackTrace();
			jta.append("Failed : Stream closed.\n");
		}
	}

	// ����� ����Ʈ (������ ä�ù�)
	public void selectedRoomUserList(String rNum, DataOutputStream target) {
		String ul = "";

		for (int i = 0; i < roomArray.size(); i++) {
			if (Integer.parseInt(rNum) == roomArray.get(i).getRoomNum()) {
				for (int j = 0; j < roomArray.get(i).getUserArray().size(); j++) {
					// ä�ù濡 ���ӵǾ� �ִ� �������� ���̵�+�г���
					ul += "/"
							+ roomArray.get(i).getUserArray().get(j)
									.toProtocol();
				}
			}
		}
		try {
			// ������ ����
			target.writeUTF(User.UPDATE_SELECTEDROOM_USERLIST + ul);
			jta.append("Success : List(user)-" + ul + "\n");
		} catch (IOException e) {
			jta.append("Error : List(user) Transfer Failed.\n");
		}
	}

	// ����� ����Ʈ (����)
	public String userList(DataOutputStream target) {
		String ul = "";

		for (int i = 0; i < userArray.size(); i++) {
			// ���ӵǾ� �ִ� �������� ���̵�+�г���
			ul += "/" + userArray.get(i).toProtocol();
		}

		try {
			// ������ ����
			target.writeUTF(User.UPDATE_USERLIST + ul);
			jta.append("Success : List(user)-" + ul + "\n");
		} catch (IOException e) {
			jta.append("Error : List(user) Transfer Failed.\n");
		}
		return ul;
	}

	// ����� ����Ʈ (ä�ù� ����)
	public void userList(String rNum, DataOutputStream target) {
		String ul = "/" + rNum;

		for (int i = 0; i < roomArray.size(); i++) {
			if (Integer.parseInt(rNum) == roomArray.get(i).getRoomNum()) {
				for (int j = 0; j < roomArray.get(i).getUserArray().size(); j++) {
					// ä�ù濡 ���ӵǾ� �ִ� �������� ���̵�+�г���
					ul += "/"
							+ roomArray.get(i).getUserArray().get(j)
									.toProtocol();
				}
			}
		}
		try {
			// ������ ����
			target.writeUTF(User.UPDATE_ROOM_USERLIST + ul);
			jta.append("Success : List(user)-" + ul + "\n");
		} catch (IOException e) {
			jta.append("Error : List(user) Transfer Failed.\n");
		}
	}

	// ����� ����Ʈ (ä�ù� ���� ��� ����ڵ鿡�� ����)
	public void userList(String rNum) {
		String ul = "/" + rNum;
		Room temp = null;
		for (int i = 0; i < roomArray.size(); i++) {
			if (Integer.parseInt(rNum) == roomArray.get(i).getRoomNum()) {
				temp = roomArray.get(i);
				for (int j = 0; j < roomArray.get(i).getUserArray().size(); j++) {
					// ä�ù濡 ���ӵǾ� �ִ� �������� ���̵�+�г���
					ul += "/"
							+ roomArray.get(i).getUserArray().get(j)
									.toProtocol();
				}
			}
		}
		for (int i = 0; i < temp.getUserArray().size(); i++) {
			try {
				// ������ ����
				temp.getUserArray().get(i).getDos()
						.writeUTF(User.UPDATE_ROOM_USERLIST + ul);
				jta.append("Success : List(user)-" + ul + "\n");
			} catch (IOException e) {
				jta.append("Error : List(user) Transfer Failed.\n");
			}
		}
	}

	// ä�� �渮��Ʈ
	public void roomList(DataOutputStream target) {
		String rl = "";

		for (int i = 0; i < roomArray.size(); i++) {
			// ������� ä�ù���� ����
			rl += "/" + roomArray.get(i).toProtocol();
		}

		jta.append("test\n");

		try {
			// ������ ����
			target.writeUTF(User.UPDATE_ROOMLIST + rl);
			jta.append("Success : List(room)-" + rl + "\n");
		} catch (IOException e) {
			jta.append("Error : List(room) Transfer Failed.\n");
		}
	}

	// ä�� �渮��Ʈ
	public void roomList() {
		String rl = "";

		for (int i = 0; i < roomArray.size(); i++) {
			// ������� ä�ù���� ����
			rl += "/" + roomArray.get(i).toProtocol();
		}

		jta.append("test\n");

		for (int i = 0; i < userArray.size(); i++) {

			try {
				// ������ ����
				userArray.get(i).getDos().writeUTF(User.UPDATE_ROOMLIST + rl);
				jta.append("Success : List(room)-" + rl + "\n");
			} catch (IOException e) {
				jta.append("Error : List(room) Transfer Failed.\n");
			}
		}
	}
}