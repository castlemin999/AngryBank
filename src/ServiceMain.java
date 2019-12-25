import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Pattern;

public class ServiceMain implements Service {
	Socket sock = null;
	Connection con = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	DataOutputStream out;
	DataInputStream in;
	StringBuffer sb = new StringBuffer();
	HashMap<Integer, String> userInfo = new HashMap<Integer, String>();

	// ������
	ServiceMain(Socket sock, Connection con) throws IOException {
		this.sock = sock;
		this.con = con;
		this.out = new DataOutputStream(new BufferedOutputStream(sock.getOutputStream()));
		this.in = new DataInputStream(sock.getInputStream());
	}

	@Override
	public void run() {
		// usermgr, dealmain ��ü ����
		UserMgr usermgr = new UserMgr(con, in, out);
		userInfo = null;
		// �α��� ���� Ȯ��
		do {
			// �α��� �ϴ� �޼���
			userInfo = usermgr.UserMgrMenu();
			System.out.println("�α��� ��");
		} while (userInfo.isEmpty());
		// �α��� �Ǹ� dealmain ����'
		System.out.println(userInfo);
		DealMain deal = new DealMain(con, in, out, userInfo);
		deal.DealMainMenu();

		// ���� ������ ���� ����
		try {
			sock.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// �޽��� ������
	public void SendMessage(String message) {
		try {
			out.writeUTF(message);
			out.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// �޽��� �ޱ�
	// @SuppressWarnings("finally")
	public String ReceiveMessage() {
		String receive = "";

		try {
			receive = in.readUTF();
		} catch (Exception e) {
			// System.out.println("����");
			e.printStackTrace();
		} finally {
			return receive;
		}
	}
} // ServiceMain

class UserMgr {
	Connection con = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	DataOutputStream out;
	DataInputStream in;
	HashMap<Integer, String> userInfo = new HashMap<Integer, String>();

	// ������
	UserMgr(Connection con, DataInputStream in, DataOutputStream out) {
		this.con = con;
		this.out = out;
		this.in = in;
	}

	// �޴������ֱ�
	public HashMap<Integer, String> UserMgrMenu() { // ȸ������
		while (true) {
			SendMessage(
					"\n------------��WELLCOME ANGRY3 BANK��----------\n                1. ȸ������ \n                2. �α��� \n                3. ���¹�ȣ ã�� \n                4. ��й�ȣ ã�� \n                5. ���α׷� ���� \n-------------------��޴� ���á�-------------------");
			String select = ReceiveMessage();
			switch (select) {
			case "1":
				try {
					signUp();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				break;
			case "2":
				userInfo = logIn();
				System.out.println(userInfo);
				return userInfo;
			case "3":
				searchID();
				break;
			case "4":
				searchPW();
				break;
			case "5":
				SendMessage("���α׷��� �����մϴ�.");
				SendMessage("Exit!!!!");
			}
		}
	}

	// 1.�α��� -> hm���� ����,��� ����
	public HashMap<Integer, String> logIn() { // �α���
		String uname = "", upw = "";
		String pnum = "";
		boolean check = true;
		try {
			while (check) {
				Statement stmt;
				ResultSet rs;
				SendMessage("�ڵ��� ��ȣ�� �Է��ϼ���: ");
				pnum = ReceiveMessage();
				SendMessage("��й�ȣ�� �Է��ϼ���: ");
				upw = ReceiveMessage();

				stmt = con.createStatement();
				rs = stmt.executeQuery("SELECT uname,upw,pnum,ano FROM BANKUSER WHERE pnum = '" + pnum + "'");

				if (rs.next()) {
					if (rs.getString(3).equals(pnum)) {
						System.out.println(rs.getString(3));
						if (rs.getString(2).equals(upw)) {
							userInfo.put(rs.getInt(4), rs.getString(2));
							SendMessage(rs.getString(1) + "�� �ȳ��ϼ���.");
							int mymoney = getBalance(rs.getInt(4));
							SendMessage("�ܾ�: " + mymoney + "��");
							check = false;
						} else {
							SendMessage("��й�ȣ�� Ʋ�Ƚ��ϴ�.");
						}
					}
				} else {
					SendMessage("���Ե��� ���� ��ȣ�Դϴ�.");
				}
			} // ��
//			} // while ��

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			return userInfo;
		}
	} // logIn

	// 2.ȸ������
	public void signUp() throws SQLException { // ȸ������
		int uno=0; int ano =0;
		String uname = "", upw = "", pnum = "";
		ResultSet rs1;
		Statement stmt = con.createStatement();
		 SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		 Calendar cal = Calendar.getInstance();
		 String today = df.format(cal.getTime());//���� ��¥ �ֱ� 
		
		String signupQuery1 = "INSERT INTO BANKUSER(uno,uname,upw,pnum,ano) VALUES(seq_bankuser_uno.nextval,?,?,?,seq_acclist_ano_real.nextval)";
		try {

			boolean a = Pattern.matches("^[��-����-�R]*$", uname);

			while (true) {
				boolean check = true;
				SendMessage("�̸��� �Է��ϼ���: ");
				uname = ReceiveMessage();

				if (Pattern.matches("^[��-����-�R]*$", uname) == false) {
					SendMessage("�ѱ۸� �����մϴ�.");
				} else {
					SendMessage("��й�ȣ�� �Է��ϼ���: ");
					upw = ReceiveMessage();
					while (true) {
						SendMessage("�ڵ��� ��ȣ�� �Է��ϼ���(����-01011111234): ");
						pnum = ReceiveMessage();
						if (pnum.length() != 11) {
							SendMessage("�ùٸ� ������ �ƴմϴ�. (���ڸ� �Է�)");
						} else {
							rs1 = stmt.executeQuery("SELECT pnum FROM BANKUSER WHERE pnum = '" + pnum + "'");
							if (rs1.next() != true) {
								break;
							} else {
								SendMessage(pnum + "�� �̹� ���Ե� ��ȭ��ȣ�Դϴ�.");
								SendMessage("ID ã�⸦ �̿����ּ���.");
							}
						}
					} // while

					pstmt = con.prepareStatement(signupQuery1);
					pstmt.setString(1, uname);
					pstmt.setString(2, upw);
					pstmt.setString(3, pnum);
					pstmt.executeQuery();
					con.commit();
					
					rs = stmt.executeQuery("SELECT uno,ano FROM bankuser WHERE pnum = " + pnum);
					while (rs.next()) {
						uno = rs.getInt(1);
						ano = rs.getInt(2);
					}
					
					stmt.executeQuery("INSERT INTO USERACC(USERNO,USERANO,DEPOSITACC,MDATE,EDATE) VALUES("+uno+","+ano+",'N','"+today+"',"+null+")");
					con.commit();
					SendMessage("ȸ������ ����");
					SendMessage(uname+"���� ���¹�ȣ�� "+ano+" �Դϴ�.");
					break;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 3.���� ã��
	public void searchID() {
		String pnum = "";
		Statement stmt;
		try {
			stmt = con.createStatement();
			boolean sizecheck = true;
			while (sizecheck) {
				SendMessage("�ڵ��� ��ȣ�� �Է��ϼ���(����-01011111234): ");
				pnum = ReceiveMessage();
				if (pnum.length() > 11 || pnum.length() < 11) {
					SendMessage("�ùٸ� ������ �ƴմϴ�.(���ڸ� �Է�)");
				} else {
					ResultSet rs1 = stmt.executeQuery("SELECT ano FROM BANKUSER WHERE pnum = '" + pnum + "'");
					if(rs1.next()) {
						if (rs1.getInt(1)!=1) {
							SendMessage(pnum + "���� �˻� ���");
							SendMessage("\n���´� " + rs1.getInt(1) + " �Դϴ�.");
							sizecheck = false;
						}
					}else{
						SendMessage("���Ե��� ���� ��ȭ��ȣ�Դϴ�.\n ȸ�����Ժ��� �������ּ���.");
						return;
					}
				}

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}// searchID()

	// 4.��й�ȣ ã��

	public void searchPW() {
		String pnum = "";
		Statement stmt;
		try {
			stmt = con.createStatement();
			boolean sizecheck = true;
			while (sizecheck) {
				SendMessage("�ڵ��� ��ȣ�� �Է��ϼ���(����-01011111234): ");
				pnum = ReceiveMessage();
				if (pnum.length() > 11 || pnum.length() < 11) {
					SendMessage("�ùٸ� ������ �ƴմϴ�.(���ڸ� �Է�)");
				} else{
					ResultSet rs1 = stmt.executeQuery("SELECT uname,pnum,upw FROM BANKUSER WHERE pnum =" + pnum);
					if (rs1.next()) {
						if (rs1.getString(2) != null) {
							SendMessage("���̵� �Է��ϼ���.");
							String uname = ReceiveMessage();
							if (rs1.getString(1).equals(uname)) {
								SendMessage("��й�ȣ��" + rs1.getString(3) + " �Դϴ�.");
								sizecheck = false;
							}
						}
					}else{
						SendMessage("���Ե��� ���� ��ȭ��ȣ�Դϴ�.\n ȸ�����Ժ��� �������ּ���.");
						return;
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// �ܾ� ��
	public int getBalance(int ano) {
		int Pmoney = 0, Mmoney = 0, mymoney = 0;
		Statement stmt;
		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery("SELECT SUM(tlist) FROM pmlist WHERE pmlist IN 'P' AND ano = " + ano);
			while (rs.next()) {
				Pmoney = rs.getInt(1);
			}

			rs = stmt.executeQuery("SELECT SUM(tlist) FROM pmlist WHERE pmlist IN 'M' AND ano = " + ano);
			while (rs.next()) {
				Mmoney = rs.getInt(1);
			}
			mymoney = Pmoney - Mmoney;
			System.out.println(mymoney);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return mymoney;
	}

	// �޽��� ������
	public void SendMessage(String message) {
		try {
			out.writeUTF(message);
			out.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// �޽��� �ޱ�
	// @SuppressWarnings("finally")
	public String ReceiveMessage() {
		String receive = "";

		try {
			receive = in.readUTF();
		} catch (Exception e) {
			// System.out.println("����");
			e.printStackTrace();
		} finally {
			return receive;
		}
	}
}