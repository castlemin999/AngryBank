import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

//1. �Ա�/��� 
public class FinMgr { // ����� ����
	static Connection con = null;
	static Statement stmt = null;
	// DealMain dm = new DealMain(Connection con, DataInputStream in,
	// DataOutputStream out, HashMap<Integer, String> userInfo , in, out, userInfo);
	ResultSet rs = null;
	DataOutputStream out;
	DataInputStream in;
	StringBuffer sb = new StringBuffer();
	HashMap<Integer, String> userInfo = new HashMap<Integer, String>();
	int ano;
	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	Calendar cal = Calendar.getInstance();
	String today = df.format(cal.getTime());// ���� ��¥ �ֱ�

	Calendar cal2 = Calendar.getInstance();
	int year2 = cal2.get(cal.YEAR) + 1;
	int month2 = cal.get(cal.MONTH) + 1;
	int date2 = cal.get(cal.DATE);
	String nextyear = String.valueOf(year2 + "-" + "0" + month2 + "-" + date2);

	FinMgr(Connection con, DataInputStream in, DataOutputStream out, HashMap<Integer, String> userInfo) {
		this.con = con;
		this.out = out;
		this.in = in;
		this.userInfo = userInfo;
		try {
			stmt = con.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Collection collection = userInfo.keySet();
		Iterator iter = collection.iterator();
		while (iter.hasNext()) {
			ano = (int) iter.next();
		}

	}

	public void finMakeAcc() throws SQLException { // ���� ������ ����

		end: while (true) {
			SendMessage("-----------------------���� �ŷ�--------------------");
			SendMessage("                   1. �Ա� / ��� ����");
			SendMessage("                   2. ���� ����   ");
			SendMessage("                   3. ���� �Ա�   ");
			SendMessage("                   4. �ʱ� ȭ�� ���ư���  ");
			SendMessage("--------------------��޴� ���á�--------------------");

			String finselect = ReceiveMessage();
			switch (finselect) {
			case "1":
				finmgrMain(); // �ܼ� ����� ����
				break;
			case "2":
				makeDepAcc(); // ���� �����
				break;

			case "3":
				depositMoney(); // ���� �Ա�
				break;

			case "4":
				break end;

			} // switch
		} // while
	}// finMakeAcc

//   
//   ���� ���鶧 ->����
//   1.���� ���� ���忡�� ����(�ܾ� �������� ����)
//   2. �����ϱ�
//   ��û�ϱ� ������ ���� ��¥�� db�� ������¥�� ������ ���ؼ� �ٸ��� x
//   ������ ���ֱ�
//   ->����ٸ� �����ϴ³��� ��û�ϱ� ������ ���ݿ� ���� 10%�־ 
//   �������·� �־���
	public void makeDepAcc() throws SQLException {// ���� �Խ�
		int uno = 0;
		String userano = "";
		ResultSet rs1;
		PreparedStatement pstmt = null;
		Statement stmt = con.createStatement();
		String rnew_ano = "";
		String pnum = "";
		while (true) {
			try {
				SendMessage("���� ���� ������ �����Ͻʴϱ�? (y/n)");
				String ans = ReceiveMessage();
				if (ans.equals("Y") || ans.equals("y")) {
					try {
						stmt = con.createStatement();
						boolean sizecheck = true;
						while (sizecheck) {
							SendMessage("���� �� �ۼ��� �ڵ��� ��ȣ�� �Է��ϼ���(����-01011111234): ");
							pnum = ReceiveMessage();
							if (pnum.length() > 11 || pnum.length() < 11) {
								SendMessage("�ùٸ� ������ �ƴմϴ�.(���ڸ� �Է�)");
							} else {
								ResultSet rs3 = stmt
										.executeQuery("SELECT uno FROM BANKUSER WHERE pnum = '" + pnum + "'");
								sizecheck = false;
							}
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}

					String sql = "SELECT MAX(uno) FROM bankuser";
					rs1 = stmt.executeQuery(sql);
					while (rs1.next()) {
						uno = rs1.getInt(1);
					}
					sb.setLength(0);
					sb.append("INSERT INTO USERACC(userno, userano, depositacc, mdate, edate,DEPOSITELIST) ");
					sb.append("VALUES (" + uno + ", seq_useracc_userano.nextval,'Y','" + today + "', '" + nextyear
							+ "', null )");
					stmt.executeUpdate(sb.toString());

					ResultSet rs2 = stmt.executeQuery("SELECT MAX(userano) FROM useracc WHERE userno = '" + uno + "'");
					while (rs2.next()) {
						int new_ano = rs2.getInt(1);
						rnew_ano = String.valueOf(new_ano);
					}
					con.commit();
					SendMessage("���� ���� ���� �ϼ̽��ϴ�");
					SendMessage("���¹�ȣ:" + rnew_ano + "�Դϴ�");
					break;
				} // if
				else if (ans.equals("N") || ans.equals("n")) {
					SendMessage("�ڷ� ���ư��ڽ��ϴ�.");
					finMakeAcc();
					break;
				} // else if
				else {
					SendMessage("�ٽ� �Է� ���ּ���.");
					continue;
				}

			} // try
			catch (SQLException e) {
				e.printStackTrace();
			} // catch

		} // while
	}// makeDepAcc

	public void depositMoney() throws SQLException { // ���� �Ա�
		SendMessage("���� ���¸�  �Է� ���ּ���.");
		String oano1 = ReceiveMessage();
		int int_oano1 = Integer.parseInt(oano1);
		stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT userano FROM useracc WHERE userano = '" + int_oano1 + "'");

		if (rs.next()) {
			if (rs.getString(1).equals(oano1)) {
				SendMessage("�Ա��� �ݾ��� �Է� ���ּ���.");
				String mlist = ReceiveMessage();
				int int_mlist = Integer.parseInt(mlist);
				int Pmoney = 0;
				int Mmoney = 0, mymoney = 0;
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
					if (mymoney < int_mlist) {
						SendMessage("�ܾ׺����Դϴ�.\n ���� �ܾ�: " + mymoney);
						return;
					}
					if (mymoney == 0) {
						SendMessage("�ܾ׺����Դϴ�. \n ���� �ܾ�: " + mymoney);
						return;
					}
					if (mymoney < 0) {
						SendMessage("�ܾ׺����Դϴ�. \n  ���� �ܾ�: " + mymoney);
						return;
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}

				while (true) {
					SendMessage("��й�ȣ�� �Է� ���ּ���.");
					String pw = ReceiveMessage();
					rs = stmt.executeQuery("SELECT upw FROM bankuser WHERE upw = '" + pw + "'");
					SendMessage("���� ������ �Է� ���ּ���.");
					String tlist = ReceiveMessage();
					if (rs.next()) {
						if (rs.getString(1).equals(pw)) {
							sb.setLength(0);
							sb.append("INSERT INTO pmlist (index_nu, pmlist, ano, tlist, etc, tdate, sano) ");
							sb.append("VALUES (seq_pmlist_index_nu.nextval, 'P', " + int_oano1 + ",  " + int_mlist
									+ ",'" + tlist + "','" + today + "', '" + ano + "')");
							stmt.executeUpdate(sb.toString());
							con.commit();

							sb.setLength(0);
							sb.append("INSERT INTO pmlist (index_nu, pmlist, ano, tlist, etc, tdate, sano) ");
							sb.append("VALUES (seq_pmlist_index_nu.nextval, 'M', " + ano + ",  " + int_mlist + ",'"
									+ tlist + "','" + today + "', '" + int_oano1 + "')");
							stmt.executeUpdate(sb.toString());
							con.commit();

							sb.setLength(0);
							sb.append(
									"UPDATE useracc SET DEPOSITELIST = " + int_mlist + " WHERE userano =" + int_oano1);
							stmt.executeUpdate(sb.toString());
							con.commit();
							SendMessage("------------------------------------");
							SendMessage("���� ���¿� �Ա� �Ǿ����ϴ�.");
							sb.setLength(0);
							break;
						} else {
							SendMessage("------------------------------------");
							SendMessage("��� Ʋ�Ƚ��ϴ�.");
							continue;
						}
					}
				} // while
			} else {
				SendMessage("���¹�ȣ �ٽ� �Է��ϼ���");
				return;
			}
		}
	}// ��

	public void finmgrMain() throws SQLException {

		end: while (true) {
			SendMessage(
					"\n--------------------���� �ŷ�--------------------\n                   1. �Ա� \n                   2. ��� \n                   3. �۱� \n                   4. ������ \n-------------------��޴� ���á�-------------------");
			String finselect = ReceiveMessage();
			switch (finselect) {
			case "1":
				selfDeposit(); // �� -> �� (�Ա�)
				break;
			case "2":
				selfWithdraw(); // �� -> �� (���)
				break;
			case "3":
				otherWithdraw(); // �� -> Ÿ�� (����)
				break;
			case "4":
				SendMessage("�ʱ� �޴��� �����ϴ�.");
				break end;
			} // switch
		} // while
	} // finmgrMain

	public void selfDeposit() throws SQLException { // �� -> �� (�Ա�)
		SendMessage("\n�Ա��� �ݾ��� �Է� ���ּ���.");
		String plist = ReceiveMessage();
		int int_plist = Integer.parseInt(plist);
		SendMessage("�ŷ� ������ �Է� ���ּ���.");
		String tlist = ReceiveMessage();
		while (true) {
			SendMessage("��й�ȣ�� �Է� ���ּ���.");
			String pw = ReceiveMessage();
			ResultSet rs = stmt.executeQuery("SELECT upw FROM bankuser WHERE upw = '" + pw + "'");
			if (rs.next()) {
				if (rs.getString(1).equals(pw)) {
					sb.setLength(0);
					sb.append("INSERT INTO pmlist (index_nu,pmlist,ano,tlist,etc,tdate,sano) ");
					sb.append("VALUES (seq_pmlist_index_nu.nextval,'P'," + ano + "," + int_plist + ",'" + tlist + "','"
							+ today + "', '" + ano + "')");
					System.out.println(sb.toString());
					try {
						stmt.executeUpdate(sb.toString());
					} catch (SQLException e) {
						e.printStackTrace();
					}
					System.out.println(sb.toString());
					con.commit();
					SendMessage("\n�Ա� �Ǿ����ϴ�.");
					break;
				}
			}
		}

	} // selfDeposit

	public void selfWithdraw() throws SQLException { // �� -> �� (���)
		SendMessage("����� �ݾ��� �Է� ���ּ���.");
		String mlist = ReceiveMessage();
		int int_mlist = Integer.parseInt(mlist);
		int Pmoney = 0;
		int Mmoney = 0, mymoney = 0;
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
			if (mymoney < int_mlist) {
				System.out.println("���� �ݾ�" + mymoney);
				System.out.println("���� ���� �ݾ�" + int_mlist);
				System.out.println(" mymoney < int_mlist" + mymoney);
				SendMessage("�ܾ׺����Դϴ�.\n ���� �ܾ�: " + mymoney);
				System.out.println("�ܾ׺����Դϴ�");
				return;
			}
			if (mymoney == 0) {
				SendMessage("�ܾ׺����Դϴ�. \n ���� �ܾ�: " + mymoney);
				return;
			}
			if (mymoney < 0) {
				SendMessage("�ܾ׺����Դϴ�. \n  ���� �ܾ�: " + mymoney);
				return;
			}
			if (mymoney > int_mlist) {
				System.out.println(" mymoney>int_mlist" + mymoney);
			}

			System.out.println("���� ����" + mymoney);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		SendMessage("�ŷ� ������ �Է� ���ּ���.");
		String tlist = ReceiveMessage();
		while (true) {
			SendMessage("��й�ȣ�� �Է� ���ּ���.");
			String pw = ReceiveMessage();
			ResultSet rs = stmt.executeQuery("SELECT upw FROM bankuser WHERE upw = '" + pw + "'");
			if (rs.next()) {
				if (rs.getString(1).equals(pw)) {
					sb.setLength(0);
					sb.append("INSERT INTO pmlist (index_nu,pmlist,ano,tlist,etc,tdate,sano) ");
					sb.append("VALUES (seq_pmlist_index_nu.nextval,'P'," + ano + "," + int_mlist + ",'" + tlist + "','"
							+ today + "', '" + ano + "')");
					System.out.println(sb.toString());
					try {
						stmt.executeUpdate(sb.toString());
					} catch (SQLException e) {
						e.printStackTrace();
					}
					System.out.println(sb.toString());
					con.commit();
					SendMessage("\n��� �Ǿ����ϴ�.");
					break;
				}
			}
		}

	} // selfWithdraw

	public void otherWithdraw() throws SQLException { // Ÿ�� -> �� (�Ա�)
		SendMessage("���� ���¸�  �Է� ���ּ���.");
		String oano = ReceiveMessage();
		stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT ano FROM bankuser WHERE ano = '" + oano + "'");

		if (rs.next()) {
			if (rs.getString(1).equals(oano)) {
				SendMessage("�Ա��� �ݾ��� �Է� ���ּ���.");
				String plist = ReceiveMessage();
				int int_plist = Integer.parseInt(plist);
				int Pmoney = 0;
				int Mmoney = 0, mymoney = 0;
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
					if (mymoney < int_plist) {
						System.out.println("���� �ݾ�" + mymoney);
						System.out.println("���� ���� �ݾ�" + int_plist);
						System.out.println(" mymoney < int_mlist" + mymoney);
						SendMessage("�ܾ׺����Դϴ�.\n ���� �ܾ�: " + mymoney);
						System.out.println("�ܾ׺����Դϴ�");
						return;
					}
					if (mymoney == 0) {
						SendMessage("�ܾ׺����Դϴ�. \n ���� �ܾ�: " + mymoney);
						return;
					}
					if (mymoney < 0) {
						SendMessage("�ܾ׺����Դϴ�. \n  ���� �ܾ�: " + mymoney);
						return;
					}
					if (mymoney > int_plist) {
						System.out.println(" mymoney>int_mlist" + mymoney);
					}

					System.out.println("���� ����" + mymoney);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				SendMessage("�ŷ� ������ �Է� ���ּ���.");
				String tlist = ReceiveMessage();
				while (true) {
					SendMessage("��й�ȣ�� �Է� ���ּ���.");
					String pw = ReceiveMessage();
					rs = stmt.executeQuery("SELECT upw FROM bankuser WHERE upw = '" + pw + "'");
					if (rs.next()) {
						if (rs.getString(1).equals(pw)) {
							sb.setLength(0);
							sb.append("INSERT INTO pmlist (index_nu,pmlist,ano,tlist,etc,tdate,sano) ");
							sb.append("VALUES (seq_pmlist_index_nu.nextval,'P', " + oano + ",  " + int_plist + ", '"
									+ tlist + "', '" + today + "', '" + ano + "')");
							stmt.executeUpdate(sb.toString());
							sb.setLength(0);
							sb.append("INSERT INTO pmlist (index_nu,pmlist,ano,tlist,etc,tdate,sano) ");
							sb.append("VALUES (seq_pmlist_index_nu.nextval,'M', " + ano + ",  " + int_plist + ", '"
									+ tlist + "', '" + today + "', '" + oano + "')");
							stmt.executeUpdate(sb.toString());

							System.out.println(sb.toString());
							con.commit();
							SendMessage("------------------------------------");
							SendMessage("���� ���¿� �Ա� �Ǿ����ϴ�.");
							sb.setLength(0);
							break;
						} else {
							SendMessage("------------------------------------");
							SendMessage("��� Ʋ�Ⱦ��ϴ�.");
							continue;
						}

					}

				} // while
			} else {
				SendMessage("���¹�ȣ �ٽ� �Է��ϼ���");
				return;
			}
		}
	}

	// } // otherWithdraw

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
} // FinMgr