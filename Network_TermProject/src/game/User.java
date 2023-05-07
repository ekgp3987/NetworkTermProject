package game;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;

public class User {
	private String defaultNick = "defaultNick";
	private static int nickCnt = 1;
	private String IP;
	private String nickName; // 사용자 닉네임
	private String id; // 사용자 아이디 - IP 주소
	private String pw; // password
	private boolean online;
	private ArrayList<Room> user_rooms; // 사용자가 입장한 방의 목록

	private DataInputStream dis; // 입력스트림
	private DataOutputStream dos; // 출력스트림

	// PROTOCOLs
	public static final String LOGIN = "EI"; // 로그인
	public static final String LOGOUT = "EO"; // 로그아웃
	public static final String MEMBERSHIP = "EM"; // 회원가입

	public static final String INVITE = "EV"; // 초대
	public static final String UPDATE_SELECTEDROOM_USERLIST = "ED"; // 대기실에서 선택한 채팅방의 유저리스트 업데이트
	public static final String UPDATE_ROOM_USERLIST = "ES"; // 채팅방의 유저리스트 업데이트
	public static final String UPDATE_USERLIST = "EU"; // 유저리스트 업데이트
	public static final String UPDATE_ROOMLIST = "ER"; // 채팅방리스트 업데이트
	public static final String CHANGE_NICK = "EN"; // 닉네임변경

	public static final String CREATE_ROOM = "RC"; // 채팅방 생성
	public static final String GETIN_ROOM = "RI"; // 채팅방 들어옴
	public static final String GETOUT_ROOM = "RO"; // 채팅방 나감
	public static final String ECHO01 = "MM"; // 대기실 채팅
	public static final String ECHO02 = "ME"; // 채팅방 채팅
	public static final String WHISPER = "MW"; // 귓속말
	
	//추가 프로토콜
	public static final String GAMEMOVE = "GM";

	User() {

	}

	User(String id, String nick) {
		this.id = id;
		this.nickName = nick;
	}

	User(DataInputStream dis, DataOutputStream dos) {
		this.dis = dis;
		this.dos = dos;
		nickCnt++;
		setNickName(defaultNick + nickCnt);
		user_rooms = new ArrayList<Room>();
	}

	public String toStringforLogin() {
		return id + "/" + pw + "/" + nickName;
	}

	public String toProtocol() {
		return id + "/" + nickName;
	}

	public String toString() {
		return nickName + "(" + id + ")";
	}

	public String getIP() {
		return IP;
	}

	public void setIP(String iP) {
		IP = iP;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPw() {
		return pw;
	}

	public void setPw(String pw) {
		this.pw = pw;
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

	public boolean isOnline() {
		return online;
	}
	
	public void setOnline(boolean online) {
		this.online = online;
	}

	public ArrayList<Room> getRoomArray() {
		return user_rooms;
	}

	public void setRooms(ArrayList<Room> rooms) {
		this.user_rooms = rooms;
	}

}
