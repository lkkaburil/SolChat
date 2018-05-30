package solchatserver;

import java.awt.event.ActionEvent;

import java.awt.event.ActionListener;

import java.io.DataInputStream;

import java.io.DataOutputStream;

import java.io.IOException;

import java.io.InputStream;

import java.io.OutputStream;

import java.net.ServerSocket;

import java.net.Socket;

import java.util.Vector;

import javax.swing.JButton;

import javax.swing.JFrame;

import javax.swing.JLabel;

import javax.swing.JOptionPane;

import javax.swing.JPanel;

import javax.swing.JScrollPane;

import javax.swing.JTextArea;

import javax.swing.JTextField;

import javax.swing.border.EmptyBorder;

public class Server extends JFrame implements ActionListener {

private JPanel contentPane;

private JTextField port_tf;

private JTextArea server_log = new JTextArea();

private JButton start_btn = new JButton("서버 실행");

private JButton stop_btn = new JButton("서버 중지");

// Network 자원

private ServerSocket server_socket;

private Socket socket;

private Vector user_vc = new Vector();

private int port;

Server() { // 생성자

config();

actionstart();

}

private void actionstart() {

start_btn.addActionListener(this); // 자기 클래스에서 구현중이므로 this

stop_btn.addActionListener(this);

}

// 이벤트 리스너

// 1. 직접 상속 <-사용하겠음

// 2. 익명 클래스

// 3. 내부 클래스, 외부 클래스

private void config() { // 화면 구성 메소드

setTitle("솔챗 서버프로그램");

setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

setBounds(100, 100, 450, 550);

contentPane = new JPanel();

contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

setContentPane(contentPane);

contentPane.setLayout(null);

JScrollPane scrollPane = new JScrollPane();

scrollPane.setBounds(10, 10, 412, 469);

contentPane.add(scrollPane);

server_log.setEditable(false);

scrollPane.setViewportView(server_log);

JLabel label = new JLabel("포트 번호");

label.setBounds(10, 486, 57, 15);

contentPane.add(label);

port_tf = new JTextField();

port_tf.setBounds(64, 483, 116, 21);

contentPane.add(port_tf);

port_tf.setColumns(10);

start_btn.setBounds(208, 482, 97, 23);

contentPane.add(start_btn);

stop_btn.setBounds(317, 482, 97, 23);

contentPane.add(stop_btn);

this.setVisible(true);// true = 화면에 보임

}

private void Server_start() {

try {

server_socket = new ServerSocket(port);

} catch (IOException e) {

// TODO Auto-generated catch block

e.printStackTrace();

}

if (server_socket != null) {// 정상적으로 포트가 열렸을 경우

Connection();

}

}

private void Connection() {

// 1가지 스레드에서는 1가지의 일만 처리 할 수 있음

Thread th = new Thread(new Runnable() {

@Override

public void run() { // 스레드에서 처리할 일을 기재

// TODO Auto-generated method stub

while (true) {

try {

server_log.append("사용자 접속 대기중... \n");

socket = server_socket.accept(); // 사용자 접속 무한 대기

server_log.append("Client 접속 성공!!\n");

UserInfo user = new UserInfo(socket);

user.start();

} catch (IOException e) {

// TODO Auto-generated catch block

e.printStackTrace();

}

} // while문 끝

}

});

th.start();

}

public static void main(String[] args) {

new Server(); // 객체 생성(생성자 호출)

}

@Override

public void actionPerformed(ActionEvent e) {

if (e.getSource() == start_btn) {

System.out.println("start_btn 클릭");

port = Integer.parseInt(port_tf.getText());

Server_start();

} else if (e.getSource() == stop_btn) {

System.out.println("stop_btn 클릭");

}

}// 액션 이벤트 끝

class UserInfo extends Thread {

private OutputStream os;

private InputStream is;

private DataOutputStream dos;

private DataInputStream dis;

private Socket user_socket;

private String name = "";

UserInfo(Socket socket) {// 생성자

this.user_socket = socket;

UserNetwork();

}

private void UserNetwork() {// 네트워크 자원 설정

try {

is = user_socket.getInputStream();

dis = new DataInputStream(is);

os = user_socket.getOutputStream();

dos = new DataOutputStream(os);

name = dis.readUTF();

server_log.append(name + " Client가 접속했습니다. \n");
BroadCast("*****"+name+"님이 접속했습니다*****");
BroadCast("NewUser/ " + name);

// 자신에게 기존 사용자를 알림

for (int i = 0; i < user_vc.size(); i++) {

UserInfo u = (UserInfo) user_vc.elementAt(i);

send_Message("OldUser/" + u.name);

}

user_vc.add(this); // 자신객체를 벡터에 담음

BroadCast("user_list_update/ ");

} catch (IOException e) {

e.printStackTrace();

}

}

public void run() { // Thread에서 처리할 내용

while (true) {

try {

String msg = dis.readUTF();

server_log.append(name + " : 사용자로부터 들어온 메세지 :" + msg +" \n");

BroadCast(name + " : " + msg);

} catch (IOException e) {

// TODO Auto-generated catch block



server_log.append(name + "과 연결이 끊어졌습니다. \n");

try {

is.close();

os.close();

dis.close();

dos.close();

user_socket.close();

user_vc.remove(this);
BroadCast(name+"님이 연결을 종료했습니다ㅠㅜ");
BroadCast("User_out/" + name);
BroadCast("user_list_update/ ");



} catch (IOException e2) {

}

break;

}

}

}

// run 메소드 끝

private void BroadCast(String str) { // 전체 사용자에게 메세지 보내는 부분

for (int i = 0; i < user_vc.size(); i++) {// 현재 접속된 사용자에게 새로운 사용자 알림

UserInfo u = (UserInfo) user_vc.elementAt(i);

u.send_Message(str); // 프로토콜 (통신 규약)

}

}

private void send_Message(String str) {// 문자열 받아서 전송

try {

dos.writeUTF(str);

// dos.flush();

} catch (IOException e) {

// TODO Auto-generated catch block

e.printStackTrace();

}

}

}

}