package game;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.Font;

public class LoginUI extends JFrame {

	public boolean confirm = false;
	public JTextField idText;
	public JPasswordField pwText;
	public JButton loginBtn, signUpBtn;
	public MemberUI mem;
	public JButton ipBtn;
	private ChatClient client;
	private JLabel lblNewLabel_2;
	
	String pw = "";
	
	public LoginUI(ChatClient ChatClient) {
		setTitle("Login");
		ServerAddress sd = new ServerAddress(this);
		this.client = ChatClient;
		loginUIInitialize();
	}

	private void loginUIInitialize() {
		setBounds(100, 100, 465, 363); //100,100,335,220
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(null);

		JPanel panel = new JPanel();
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel.setBounds(33, 93, 374, 199); //12,10,295,160
		getContentPane().add(panel);
		panel.setLayout(null);

		JLabel lblNewLabel = new JLabel("ID"); //아이디
		lblNewLabel.setFont(new Font("Arial Unicode MS", Font.PLAIN, 17));
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setBounds(60, 57, 57, 15); //60 55 57 15
		panel.add(lblNewLabel);

		JLabel lblNewLabel_1 = new JLabel("PW"); //비밀번호
		lblNewLabel_1.setFont(new Font("Arial Unicode MS", Font.PLAIN, 17));
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1.setBounds(60, 100, 57, 15);
		panel.add(lblNewLabel_1);
				
		
		idText = new JTextField();
		idText.setFont(new Font("Arial Unicode MS", Font.PLAIN, 16));
		idText.setBounds(129, 52, 187, 23);
		panel.add(idText);
		idText.setColumns(10);

		pwText = new JPasswordField();
		pwText.setEchoChar('*');
		
		pwText.setFont(new Font("Arial Unicode MS", Font.PLAIN, 16));
		pwText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					msgSummit();
				}
			}
		});
		
		pwText.setBounds(129, 95, 187, 23);
		panel.add(pwText);
		pwText.setColumns(10);
		
		loginBtn = new JButton("Log in");  //로그인
		loginBtn.setFont(new Font("Arial Unicode MS", Font.PLAIN, 20));
		loginBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		loginBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				msgSummit();
			}
		});
		loginBtn.setBounds(28, 141, 130, 35); // 50 111 97 23
		panel.add(loginBtn);

		signUpBtn = new JButton("Register"); //회원가입
		signUpBtn.setFont(new Font("Arial Unicode MS", Font.PLAIN, 20));
		signUpBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		signUpBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// 회원가입
				mem = new MemberUI(client);
			}
		});
		signUpBtn.setBounds(215, 141, 130, 35); //149 111 97 23
		panel.add(signUpBtn);

		JLabel lblNewLabel_3 = new JLabel("Server IP address"); //서버아이피
		lblNewLabel_3.setFont(new Font("Arial Unicode MS", Font.PLAIN, 15));
		lblNewLabel_3.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_3.setBounds(12, 10, 130, 15); // 12 10 78 15
		panel.add(lblNewLabel_3);

		ipBtn = new JButton("IP Setting"); //아이피 입력
		ipBtn.setFont(new Font("Arial Unicode MS", Font.PLAIN, 15));
		ipBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ServerAddress sd = new ServerAddress(LoginUI.this);
				setVisible(false);
			}
		});
		ipBtn.setBounds(142, 6, 125, 23); // 93 6 97 23
		panel.add(ipBtn);
		
		lblNewLabel_2 = new JLabel("TicTacToe");
		lblNewLabel_2.setFont(new Font("Calibri", Font.BOLD, 44));
		lblNewLabel_2.setBounds(131, 20, 191, 61);
		getContentPane().add(lblNewLabel_2);
	}

	
	
	
	private String encrypt(String str) { //CaesarCipher
		
		int shift = 5; 
		
		StringBuffer result = new StringBuffer();
		for(int i=0; i<str.length(); i++)
		{
			if(Character.isUpperCase(str.charAt(i)))
			{
				char ch = (char)(((int)str.charAt(i) +
						shift - 65) % 26 + 65);
				result.append(ch);
			}
			else
			{
				char ch = (char)(((int)str.charAt(i)+
						shift - 97)%26 + 97);
				result.append(ch);
			}
		}
		return result.toString();
	}
	
	
	
	   private void msgSummit() {
		      
		      char[] char_pw = {};
		      char_pw = pwText.getPassword();  //char[] 배열에 저장 
		      pw = "";
		      //char_pw 배열에 저장된 암호의 자릿수 만큼 for문 돌리면서 cha 에 한 글자씩 저장 
		      for(char cha : char_pw)
		      {
		         String temp = Character.toString(cha); //cha 에 저장된 값 string으로 변환
		         
		         //pw 에 저장, pw 에 값이 비어있으면 저장, 값이 있으면 이어서 저장
//		         pw += (pw.equals("")) ? ""+temp+"" : ""+temp+"";
		         
		         pw += temp;
		      }
//		      pw = char_pw.toString();
		      System.out.println(pw);
		      new Thread(new Runnable() {
		         public void run() {

		            // 소켓생성
		            if (client.serverAccess()) {
		               try {
		                  // 로그인정보(아이디+패스워드) 전송
		                  client.getDos().writeUTF(
		                        User.LOGIN + "/" + idText.getText() +
		                        "/" + encrypt(pw));
		               } catch (IOException e1) {
		                  e1.printStackTrace();
		               }
		            }
		         }
		      }).start();
		   }
}
