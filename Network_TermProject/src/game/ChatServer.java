package game;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

// To create UI and listen to client access
public class ChatServer extends JFrame implements Runnable {

	private static final int PORT = 5555; // 서버프로그램의 포트번호
	private Socket socket;
	private ServerSocket serverSocket; // 서버소켓
	private DataOutputStream os;
	private DataInputStream is;
	private ArrayList<User> userArray; // 서버에 접속한 사용자들
	private ArrayList<Room> roomArray; // 서버가 열어놓은 채팅방들

	private int sizeX = 600, sizeY = 600;
	private Dimension whole, part;
	private int xPos, yPos;
	private JTextArea jta;
	private JPanel jp;

	ChatServer() {
		userArray = new ArrayList<User>();
		roomArray = new ArrayList<Room>();
		setTitle("TictactoeChat Server");
		setSize(sizeX, sizeY);

		jta = new JTextArea();
		jp = new JPanel();

		jp.setLayout(new GridLayout(1, 2)); // 그리드 레이아웃
		jta.setEditable(false); // 에디팅 불가
		jta.setLineWrap(true); // 자동줄바꿈

		JScrollPane jsp = new JScrollPane(jta); // 텍스트에어리어에 스크롤 추가
		jsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		jp.add(jsp);// 패널에 스크롤 붙임
		jta.setText("Server Start...");

		add(jp); // 프레임에 패널 붙임

		// 윈도우 위치 계산
		whole = Toolkit.getDefaultToolkit().getScreenSize();
		part = this.getSize();
		xPos = (int) (whole.getWidth() / 2 - part.getWidth() / 2);
		yPos = (int) (whole.getHeight() / 2 - part.getHeight() / 2);

		setLocation(xPos, yPos);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}

	public static void main(String[] args) {
		// Create server UI
		System.out.println("Server start...");
		ChatServer server = new ChatServer();
		Thread thread = new Thread(server);
		thread.start();
	}

	@Override
	public void run() {
		// 클라이언트 대기 모드

		// 서버소켓 생성
		try {
			InetAddress addr = InetAddress.getLocalHost(); // 로컬호스트 주소
			serverSocket = new ServerSocket(PORT); // 서버소켓 생성
			jta.append("Port #" + PORT + ", socket were created successfully.\n"
					+ "The server IP address is " + addr.getHostAddress().toString()
					+ "\n");
		} catch (IOException e1) {
			e1.printStackTrace();
			jta.append("Error : Failed to generate server socket.\n");
		}

		while (true) {
			socket = null;
			is = null;
			os = null;
			try {
				// 무한반복, 입출력 에러가 나거나 프로그램이 종료될 때까지 실행
				socket = serverSocket.accept(); // 클라이언트 접속 대기
				jta.append("Client " + socket.getInetAddress().getHostAddress()
						+ " is accessed.\n");

			} catch (IOException e) {
				e.printStackTrace();
				try {
					socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				jta.append("Client connection error.\n");
			}
			try {
				// 스트림 객체 생성
				is = new DataInputStream(socket.getInputStream());
				os = new DataOutputStream(socket.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
				try {
					is.close();
					os.close();
				} catch (IOException e1) {
					e1.printStackTrace();
					jta.append("Error occurred while closing stream.\n");
				}
				try {
					socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
					jta.append("Error occurred while closing socket.\n");
				}
				jta.append("Error occurred while creating stream.\n");
			}
			User person = new User(is, os); // 가명의 사용자 객체 생성
			person.setIP(socket.getInetAddress().getHostName()); // 아이피주소 설정
			// 부여

			Thread thread = new Thread(new ServerThread(jta, person,
					userArray, roomArray));
			thread.start(); // 스레드 시작
		}
	}
}