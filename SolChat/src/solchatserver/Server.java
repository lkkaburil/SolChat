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

private JButton start_btn = new JButton("���� ����");

private JButton stop_btn = new JButton("���� ����");

// Network �ڿ�

private ServerSocket server_socket;

private Socket socket;

private Vector user_vc = new Vector();

private int port;

Server() { // ������

config();

actionstart();

}

private void actionstart() {

start_btn.addActionListener(this); // �ڱ� Ŭ�������� �������̹Ƿ� this

stop_btn.addActionListener(this);

}

// �̺�Ʈ ������

// 1. ���� ��� <-����ϰ���

// 2. �͸� Ŭ����

// 3. ���� Ŭ����, �ܺ� Ŭ����

private void config() { // ȭ�� ���� �޼ҵ�

setTitle("��ê �������α׷�");

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

JLabel label = new JLabel("��Ʈ ��ȣ");

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

this.setVisible(true);// true = ȭ�鿡 ����

}

private void Server_start() {

try {

server_socket = new ServerSocket(port);

} catch (IOException e) {

// TODO Auto-generated catch block

e.printStackTrace();

}

if (server_socket != null) {// ���������� ��Ʈ�� ������ ���

Connection();

}

}

private void Connection() {

// 1���� �����忡���� 1������ �ϸ� ó�� �� �� ����

Thread th = new Thread(new Runnable() {

@Override

public void run() { // �����忡�� ó���� ���� ����

// TODO Auto-generated method stub

while (true) {

try {

server_log.append("����� ���� �����... \n");

socket = server_socket.accept(); // ����� ���� ���� ���

server_log.append("Client ���� ����!!\n");

UserInfo user = new UserInfo(socket);

user.start();

} catch (IOException e) {

// TODO Auto-generated catch block

e.printStackTrace();

}

} // while�� ��

}

});

th.start();

}

public static void main(String[] args) {

new Server(); // ��ü ����(������ ȣ��)

}

@Override

public void actionPerformed(ActionEvent e) {

if (e.getSource() == start_btn) {

System.out.println("start_btn Ŭ��");

port = Integer.parseInt(port_tf.getText());

Server_start();

} else if (e.getSource() == stop_btn) {

System.out.println("stop_btn Ŭ��");

}

}// �׼� �̺�Ʈ ��

class UserInfo extends Thread {

private OutputStream os;

private InputStream is;

private DataOutputStream dos;

private DataInputStream dis;

private Socket user_socket;

private String name = "";

UserInfo(Socket socket) {// ������

this.user_socket = socket;

UserNetwork();

}

private void UserNetwork() {// ��Ʈ��ũ �ڿ� ����

try {

is = user_socket.getInputStream();

dis = new DataInputStream(is);

os = user_socket.getOutputStream();

dos = new DataOutputStream(os);

name = dis.readUTF();

server_log.append(name + " Client�� �����߽��ϴ�. \n");
BroadCast("*****"+name+"���� �����߽��ϴ�*****");
BroadCast("NewUser/ " + name);

// �ڽſ��� ���� ����ڸ� �˸�

for (int i = 0; i < user_vc.size(); i++) {

UserInfo u = (UserInfo) user_vc.elementAt(i);

send_Message("OldUser/" + u.name);

}

user_vc.add(this); // �ڽŰ�ü�� ���Ϳ� ����

BroadCast("user_list_update/ ");

} catch (IOException e) {

e.printStackTrace();

}

}

public void run() { // Thread���� ó���� ����

while (true) {

try {

String msg = dis.readUTF();

server_log.append(name + " : ����ڷκ��� ���� �޼��� :" + msg +" \n");

BroadCast(name + " : " + msg);

} catch (IOException e) {

// TODO Auto-generated catch block



server_log.append(name + "�� ������ ���������ϴ�. \n");

try {

is.close();

os.close();

dis.close();

dos.close();

user_socket.close();

user_vc.remove(this);
BroadCast(name+"���� ������ �����߽��ϴ٤Ф�");
BroadCast("User_out/" + name);
BroadCast("user_list_update/ ");



} catch (IOException e2) {

}

break;

}

}

}

// run �޼ҵ� ��

private void BroadCast(String str) { // ��ü ����ڿ��� �޼��� ������ �κ�

for (int i = 0; i < user_vc.size(); i++) {// ���� ���ӵ� ����ڿ��� ���ο� ����� �˸�

UserInfo u = (UserInfo) user_vc.elementAt(i);

u.send_Message(str); // �������� (��� �Ծ�)

}

}

private void send_Message(String str) {// ���ڿ� �޾Ƽ� ����

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