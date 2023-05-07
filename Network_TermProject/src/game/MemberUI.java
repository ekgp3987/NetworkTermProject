package game;

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

public class MemberUI extends JFrame {

	public boolean confirm = false;
	public JTextField idText;
	public JPasswordField pwText;
	public JButton signUpBtn, cancelBtn;
	private ChatClient client;
	
	String pw = ""; 
	
	public MemberUI(ChatClient client) {
		setTitle("Register"); //ȸ������

		this.client = client;
		initialize();
	}

	private void initialize() {
		setBounds(100, 100, 403, 281); // 100 100 335 197
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(null);

		JPanel panel = new JPanel();
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel.setBounds(12, 12, 368, 210); // 12 10 295 138
		getContentPane().add(panel);
		panel.setLayout(null);

		JLabel lblNewLabel = new JLabel("ID"); //���̵�
		lblNewLabel.setFont(new Font("Arial Unicode MS", Font.PLAIN, 17));
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setBounds(41, 46, 57, 15);
		panel.add(lblNewLabel);

		JLabel lblNewLabel_1 = new JLabel("PW"); //��й�ȣ
		lblNewLabel_1.setFont(new Font("Arial Unicode MS", Font.PLAIN, 17));
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1.setBounds(41, 98, 57, 15);
		panel.add(lblNewLabel_1);		
		
		idText = new JTextField();
		idText.setFont(new Font("Arial Unicode MS", Font.PLAIN, 16));
		idText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					setVisible(false);
				}
			}
		});
		idText.setBounds(114, 41, 180, 24);
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
				} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					setVisible(false);
				}
			}
		});
		pwText.setBounds(112, 93, 182, 24);
		panel.add(pwText);
		pwText.setColumns(10);

		signUpBtn = new JButton("Join"); //����
		signUpBtn.setFont(new Font("Arial Unicode MS", Font.PLAIN, 20));
		signUpBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						msgSummit();
					}
				}).start();
				dispose();
			}
		});
		signUpBtn.setBounds(27, 154, 116, 35); //50 88 97 23
		panel.add(signUpBtn);

		cancelBtn = new JButton("Cancel"); //���
		cancelBtn.setFont(new Font("Arial Unicode MS", Font.PLAIN, 20));
		cancelBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				setVisible(false);
			}
		});
		cancelBtn.setBounds(208, 154, 123, 35); //148 88 97 23
		panel.add(cancelBtn);
		setVisible(true);
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
	
	   private void msgSummit() {// ���ϻ���
		   
		      char[] char_pw = pwText.getPassword();
		      pw = "";
		      //char_pw �迭�� ����� ��ȣ�� �ڸ��� ��ŭ for�� �����鼭 cha �� �� ���ھ� ���� 
		      for(char cha : char_pw)
		      {
		         
		         String temp = Character.toString(cha); //cha �� ����� �� string���� ��ȯ 
		         //pw �� ����, pw �� ���� ��������� ����, ���� ������ �̾ ����
		         //pw += (pw.equals("")) ? ""+cha+"" : ""+cha+"";
		         
		         pw += temp;
		      }
		         
		         
		      if (client.serverAccess()) {
		         try {
		            // ȸ����������(���̵�+�н�����) ����
		            client.getDos().writeUTF(
		                  User.MEMBERSHIP + "/" + idText.getText() + "/"
		                        + encrypt(pw));
		            
		            setVisible(false);
		         } catch (IOException e1) {
		            e1.printStackTrace();
		         }
		      }
		   }
}
