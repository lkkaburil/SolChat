package solchatclient;

import java.awt.Color;

import java.awt.Font;

import java.awt.event.ActionEvent;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.DataInputStream;

import java.io.DataOutputStream;

import java.io.IOException;

import java.io.InputStream;

import java.io.OutputStream;

import java.net.Socket;

import java.util.StringTokenizer;

import java.util.Vector;

import javax.swing.ImageIcon;

import javax.swing.JButton;

import javax.swing.JFrame;

import javax.swing.JLabel;

import javax.swing.JList;

import javax.swing.JOptionPane;

import javax.swing.JPanel;

import javax.swing.JScrollPane;

import javax.swing.JTextArea;

import javax.swing.JTextField;

import javax.swing.SwingConstants;

import javax.swing.border.EmptyBorder;

public class Client extends JFrame implements ActionListener, KeyListener {

// Login GUI 변수

private JFrame Login_GUI = new JFrame(); // 새로운 객체를 생성

private JPanel Login_Pane;

private JTextField name_tf; // 이름 받는 텍스트 필드

private JTextField port_tf; // port 받는 텍스트 필드

private JLabel lblNewLabel;

private JButton login_btn = new JButton("접 속");

// Main GUI 변수

private JPanel contentPane;

private JTextField message_tf;

private JButton send_btn = new JButton("전    송");

private JList user_list = new JList(); // 접속자 리스트

private JTextArea Chat_area = new JTextArea(); // 채팅창 변수

JScrollPane scrollPane_1 = new JScrollPane();


// Network 위한 자원 변수

private Socket socket;

private int port;

private String name;

private InputStream is;

private OutputStream os;

private DataInputStream dis;

private DataOutputStream dos;

// 그외 변수들

Vector User_list = new Vector();

StringTokenizer st;

Client() {

Login_Config(); // Login 창 화면 구성 메소드

Main_Config();

actionstart();

}

private void actionstart() {

login_btn.addActionListener(this); // 접속 버튼 리스너

send_btn.addActionListener(this); // 전송 리스너


}

private void Main_Config() {

setTitle("솔챗");

setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

setBounds(100, 100, 450, 550);

contentPane = new JPanel();

contentPane.setBackground(new Color(47, 79, 79));

contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

setContentPane(contentPane);

contentPane.setLayout(null);

JLabel label = new JLabel("접속자");

label.setForeground(Color.WHITE);

label.setBounds(351, 15, 57, 15);

contentPane.add(label);


scrollPane_1.setBounds(12, 10, 302, 461);

contentPane.add(scrollPane_1);

Chat_area.setBackground(Color.BLACK);

Chat_area.setForeground(Color.WHITE);

Chat_area.setEditable(false);

scrollPane_1.setViewportView(Chat_area);

message_tf = new JTextField();

message_tf.setBounds(12, 480, 300, 21);
message_tf.addKeyListener(this);

contentPane.add(message_tf);

message_tf.setColumns(10);

send_btn.setBackground(new Color(218, 165, 32));

send_btn.setBounds(325, 479, 97, 23);

contentPane.add(send_btn);

JScrollPane scrollPane = new JScrollPane();

scrollPane.setBounds(326, 48, 96, 286);

contentPane.add(scrollPane);

scrollPane.setViewportView(user_list);

user_list.setForeground(Color.WHITE);

user_list.setBackground(Color.BLACK);

user_list.setListData(User_list);

this.setVisible(false); // 얘는 객체생성을 해서 하는게 아니라 상속받은 애이므로 this 사용

}

private void Login_Config() {

Login_GUI.setTitle("솔챗 로그인창");

Login_GUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

Login_GUI.setBounds(100, 100, 450, 550);

Login_Pane = new JPanel();

Login_Pane.setBackground(Color.WHITE);

Login_Pane.setBorder(new EmptyBorder(5, 5, 5, 5));

Login_GUI.setContentPane(Login_Pane);

Login_Pane.setLayout(null);

JLabel label = new JLabel("이름");

label.setHorizontalAlignment(SwingConstants.CENTER);

label.setBounds(114, 351, 57, 15);

Login_Pane.add(label);

name_tf = new JTextField();

name_tf.setBounds(183, 348, 116, 21);

Login_Pane.add(name_tf);

name_tf.setColumns(10);

JLabel label_1 = new JLabel("포트번호");

label_1.setHorizontalAlignment(SwingConstants.CENTER);

label_1.setBounds(114, 307, 57, 15);

Login_Pane.add(label_1);

port_tf = new JTextField();

port_tf.setBounds(183, 304, 116, 21);

Login_Pane.add(port_tf);

port_tf.setColumns(10);

port_tf.setText("2018");

login_btn.setFont(new Font("굴림", Font.BOLD, 24));

login_btn.setBounds(42, 407, 362, 75);

Login_Pane.add(login_btn);

lblNewLabel = new JLabel("");

lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);

lblNewLabel.setIcon(new ImageIcon(Client.class.getResource("/solchatclient/soldesk.jpg")));

lblNewLabel.setBounds(0, 126, 434, 75);

Login_Pane.add(lblNewLabel);

Login_GUI.setVisible(true);

}

private void Network() {

try {

socket = new Socket("127.0.0.1", port);

if (socket != null) {// 정상적으로 소켓 연결이 된 경우

Login_GUI.setVisible(false);

this.setVisible(true);

Connection();

}

} catch (IOException e) {

// TODO Auto-generated catch block

e.printStackTrace();

JOptionPane.showMessageDialog(null, "연결에 실패했습니다");

}

}

private void Connection() { // 실제적인 메소드 연결부분

try {

is = socket.getInputStream();

dis = new DataInputStream(is);

os = socket.getOutputStream();

dos = new DataOutputStream(os);

} catch (IOException e) {

e.printStackTrace();

} // Stream 설정 끝

// 처음 접속시 이름 전송

send_message(name);

User_list.add(name);

Thread th = new Thread(new Runnable() {

@Override

public void run() {

// TODO Auto-generated method stub

while (true) {

String msg;

try {

msg = dis.readUTF();

System.out.println("서버로부터 수신된 메세지:" + msg);

if (msg.matches(".*NewUser/.*") || msg.matches(".*OldUser/.*")
|| msg.matches(".*user_list_update/.*")||msg.matches(".*User_out/.*")) {

inmessage(msg);

}

else {
Chat_area.append(msg + " \n");
scrollPane_1.getVerticalScrollBar().setValue(scrollPane_1.getVerticalScrollBar().getMaximum());





}

} catch (IOException e) {

// TODO Auto-generated catch block

e.printStackTrace();

try {

is.close();

os.close();

dis.close();

dos.close();

socket.close();

JOptionPane.showMessageDialog(null, "연결이 끊어졌습니다.");
break;

} catch (Exception e2) {

break;

}

}

}

}

});

th.start();

}

private void inmessage(String str) {// 서버로부터 들어오는 모든 메세지

st = new StringTokenizer(str, "/");

String protocol = st.nextToken().trim();

String Message = st.nextToken().trim();

System.out.println("프로토콜 : " + protocol);

System.out.println("내용 : " + Message);

if (protocol.equals("NewUser")) {

User_list.add(Message);

} else if (protocol.equals("OldUser")) {

User_list.add(Message);

} 
else if (protocol.equals("User_out")) {
System.out.println(Message);
Thread th=new Thread(new Runnable() {

@Override
public void run() {
// TODO Auto-generated method stub
int i=1;
while(true) {
boolean a=User_list.removeElement(Message);
if (a==true) {

System.out.println(i+"번째만에 성공적으로 삭제했습니다");
break;
}
i++;
}

}
});
th.start();	
}
//user_list.setListData(User_list);

else if (protocol.equals("user_list_update")) {

user_list.setListData(User_list);

} 

}

private void send_message(String str) {// 서버에게 메세지를 보내는 부분

try {

dos.writeUTF(str);

dos.flush();

} catch (IOException e) {

// TODO Auto-generated catch block

e.printStackTrace();

}

}

public static void main(String[] args) {

new Client();

}

@Override

public void actionPerformed(ActionEvent e) {

if (e.getSource() == login_btn) {

System.out.println("login_btn 클릭");

port = Integer.parseInt(port_tf.getText().trim()); // port를 받아오는 부분

name = name_tf.getText().trim();

Network();

} else if (e.getSource() == send_btn) {

send_message(message_tf.getText());

message_tf.setText("");

System.out.println("send_btn 클릭");

}

}

@Override
public void keyTyped(KeyEvent e) {
// TODO Auto-generated method stub

}

@Override
public void keyPressed(KeyEvent e) {
if (e.getKeyCode() == KeyEvent.VK_ENTER) {

send_message(message_tf.getText());

message_tf.setText("");

System.out.println("엔터입력");

}

}

@Override
public void keyReleased(KeyEvent e) {
// TODO Auto-generated method stub

}


}
