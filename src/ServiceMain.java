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

	// »ý¼ºÀÚ
	ServiceMain(Socket sock, Connection con) throws IOException {
		this.sock = sock;
		this.con = con;
		this.out = new DataOutputStream(new BufferedOutputStream(sock.getOutputStream()));
		this.in = new DataInputStream(sock.getInputStream());
	}

	@Override
	public void run() {
		// usermgr, dealmain °´Ã¼ »ý¼º
		UserMgr usermgr = new UserMgr(con, in, out);
		userInfo = null;
		// ·Î±×ÀÎ ¿©ºÎ È®ÀÎ
		do {
			// ·Î±×ÀÎ ÇÏ´Â ¸Þ¼­µå
			userInfo = usermgr.UserMgrMenu();
			System.out.println("·Î±×ÀÎ ³¡");
		} while (userInfo.isEmpty());
		// ·Î±×ÀÎ µÇ¸é dealmain ½ÇÇà'
		System.out.println(userInfo);
		DealMain deal = new DealMain(con, in, out, userInfo);
		deal.DealMainMenu();

		// ¸ðµç°Ô ³¡³ª¸é ¼ÒÄÏ Á¤¸®
		try {
			sock.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// ¸Þ½ÃÁö º¸³»±â
	public void SendMessage(String message) {
		try {
			out.writeUTF(message);
			out.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ¸Þ½ÃÁö ¹Þ±â
	// @SuppressWarnings("finally")
	public String ReceiveMessage() {
		String receive = "";

		try {
			receive = in.readUTF();
		} catch (Exception e) {
			// System.out.println("¿¡·¯");
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

	// »ý¼ºÀÚ
	UserMgr(Connection con, DataInputStream in, DataOutputStream out) {
		this.con = con;
		this.out = out;
		this.in = in;
	}

	// ¸Þ´ºº¸¿©ÁÖ±â
	public HashMap<Integer, String> UserMgrMenu() { // È¸¿ø°ü¸®
		while (true) {
			SendMessage(
					"\n------------¡ÚWELLCOME ANGRY3 BANK¡Ú----------\n                1. È¸¿ø°¡ÀÔ \n                2. ·Î±×ÀÎ \n                3. °èÁÂ¹øÈ£ Ã£±â \n                4. ºñ¹Ð¹øÈ£ Ã£±â \n                5. ÇÁ·Î±×·¥ Á¾·á \n-------------------¡é¸Þ´º ¼±ÅÃ¡é-------------------");
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
				SendMessage("ÇÁ·Î±×·¥À» Á¾·áÇÕ´Ï´Ù.");
				SendMessage("Exit!!!!");
			}
		}
	}

	// 1.·Î±×ÀÎ -> hmÀ¸·Î °èÁÂ,ºñ¹ø ¸®ÅÏ
	public HashMap<Integer, String> logIn() { // ·Î±×ÀÎ
		String uname = "", upw = "";
		String pnum = "";
		boolean check = true;
		try {
			while (check) {
				Statement stmt;
				ResultSet rs;
				SendMessage("ÇÚµåÆù ¹øÈ£¸¦ ÀÔ·ÂÇÏ¼¼¿ä: ");
				pnum = ReceiveMessage();
				SendMessage("ºñ¹Ð¹øÈ£¸¦ ÀÔ·ÂÇÏ¼¼¿ä: ");
				upw = ReceiveMessage();

				stmt = con.createStatement();
				rs = stmt.executeQuery("SELECT uname,upw,pnum,ano FROM BANKUSER WHERE pnum = '" + pnum + "'");

				if (rs.next()) {
					if (rs.getString(3).equals(pnum)) {
						System.out.println(rs.getString(3));
						if (rs.getString(2).equals(upw)) {
							userInfo.put(rs.getInt(4), rs.getString(2));
							SendMessage(rs.getString(1) + "´Ô ¾È³çÇÏ¼¼¿ä.");
							int mymoney = getBalance(rs.getInt(4));
							SendMessage("ÀÜ¾×: " + mymoney + "¿ø");
							check = false;
						} else {
							SendMessage("ºñ¹Ð¹øÈ£°¡ Æ²·È½À´Ï´Ù.");
						}
					}
				} else {
					SendMessage("°¡ÀÔµÇÁö ¾ÊÀº ¹øÈ£ÀÔ´Ï´Ù.");
				}
			} // ³¡
//			} // while ³¡

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			return userInfo;
		}
	} // logIn

	// 2.È¸¿ø°¡ÀÔ
	public void signUp() throws SQLException { // È¸¿ø°¡ÀÔ
		int uno=0; int ano =0;
		String uname = "", upw = "", pnum = "";
		ResultSet rs1;
		Statement stmt = con.createStatement();
		 SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		 Calendar cal = Calendar.getInstance();
		 String today = df.format(cal.getTime());//ÇöÀç ³¯Â¥ ³Ö±â 
		
		String signupQuery1 = "INSERT INTO BANKUSER(uno,uname,upw,pnum,ano) VALUES(seq_bankuser_uno.nextval,?,?,?,seq_acclist_ano_real.nextval)";
		try {

			boolean a = Pattern.matches("^[¤¡-¤¾°¡-ÆR]*$", uname);

			while (true) {
				boolean check = true;
				SendMessage("ÀÌ¸§À» ÀÔ·ÂÇÏ¼¼¿ä: ");
				uname = ReceiveMessage();

				if (Pattern.matches("^[¤¡-¤¾°¡-ÆR]*$", uname) == false) {
					SendMessage("ÇÑ±Û¸¸ °¡´ÉÇÕ´Ï´Ù.");
				} else {
					SendMessage("ºñ¹Ð¹øÈ£¸¦ ÀÔ·ÂÇÏ¼¼¿ä: ");
					upw = ReceiveMessage();
					while (true) {
						SendMessage("ÇÚµåÆù ¹øÈ£¸¦ ÀÔ·ÂÇÏ¼¼¿ä(Çü½Ä-01011111234): ");
						pnum = ReceiveMessage();
						if (pnum.length() != 11) {
							SendMessage("¿Ã¹Ù¸¥ Çü½ÄÀÌ ¾Æ´Õ´Ï´Ù. (¼ýÀÚ¸¸ ÀÔ·Â)");
						} else {
							rs1 = stmt.executeQuery("SELECT pnum FROM BANKUSER WHERE pnum = '" + pnum + "'");
							if (rs1.next() != true) {
								break;
							} else {
								SendMessage(pnum + "Àº ÀÌ¹Ì °¡ÀÔµÈ ÀüÈ­¹øÈ£ÀÔ´Ï´Ù.");
								SendMessage("ID Ã£±â¸¦ ÀÌ¿ëÇØÁÖ¼¼¿ä.");
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
					SendMessage("È¸¿ø°¡ÀÔ ¼º°ø");
					SendMessage(uname+"´ÔÀÇ °èÁÂ¹øÈ£´Â "+ano+" ÀÔ´Ï´Ù.");
					break;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 3.°èÁÂ Ã£±â
	public void searchID() {
		String pnum = "";
		Statement stmt;
		try {
			stmt = con.createStatement();
			boolean sizecheck = true;
			while (sizecheck) {
				SendMessage("ÇÚµåÆù ¹øÈ£¸¦ ÀÔ·ÂÇÏ¼¼¿ä(Çü½Ä-01011111234): ");
				pnum = ReceiveMessage();
				if (pnum.length() > 11 || pnum.length() < 11) {
					SendMessage("¿Ã¹Ù¸¥ Çü½ÄÀÌ ¾Æ´Õ´Ï´Ù.(¼ýÀÚ¸¸ ÀÔ·Â)");
				} else {
					ResultSet rs1 = stmt.executeQuery("SELECT ano FROM BANKUSER WHERE pnum = '" + pnum + "'");
					if(rs1.next()) {
						if (rs1.getInt(1)!=1) {
							SendMessage(pnum + "À¸·Î °Ë»ö °á°ú");
							SendMessage("\n°èÁÂ´Â " + rs1.getInt(1) + " ÀÔ´Ï´Ù.");
							sizecheck = false;
						}
					}else{
						SendMessage("°¡ÀÔµÇÁö ¾ÊÀº ÀüÈ­¹øÈ£ÀÔ´Ï´Ù.\n È¸¿ø°¡ÀÔºÎÅÍ ÁøÇàÇØÁÖ¼¼¿ä.");
						return;
					}
				}

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}// searchID()

	// 4.ºñ¹Ð¹øÈ£ Ã£±â

	public void searchPW() {
		String pnum = "";
		Statement stmt;
		try {
			stmt = con.createStatement();
			boolean sizecheck = true;
			while (sizecheck) {
				SendMessage("ÇÚµåÆù ¹øÈ£¸¦ ÀÔ·ÂÇÏ¼¼¿ä(Çü½Ä-01011111234): ");
				pnum = ReceiveMessage();
				if (pnum.length() > 11 || pnum.length() < 11) {
					SendMessage("¿Ã¹Ù¸¥ Çü½ÄÀÌ ¾Æ´Õ´Ï´Ù.(¼ýÀÚ¸¸ ÀÔ·Â)");
				} else{
					ResultSet rs1 = stmt.executeQuery("SELECT uname,pnum,upw FROM BANKUSER WHERE pnum =" + pnum);
					if (rs1.next()) {
						if (rs1.getString(2) != null) {
							SendMessage("¾ÆÀÌµð¸¦ ÀÔ·ÂÇÏ¼¼¿ä.");
							String uname = ReceiveMessage();
							if (rs1.getString(1).equals(uname)) {
								SendMessage("ºñ¹Ð¹øÈ£´Â" + rs1.getString(3) + " ÀÔ´Ï´Ù.");
								sizecheck = false;
							}
						}
					}else{
						SendMessage("°¡ÀÔµÇÁö ¾ÊÀº ÀüÈ­¹øÈ£ÀÔ´Ï´Ù.\n È¸¿ø°¡ÀÔºÎÅÍ ÁøÇàÇØÁÖ¼¼¿ä.");
						return;
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// ÀÜ¾× °Ù
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

	// ¸Þ½ÃÁö º¸³»±â
	public void SendMessage(String message) {
		try {
			out.writeUTF(message);
			out.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ¸Þ½ÃÁö ¹Þ±â
	// @SuppressWarnings("finally")
	public String ReceiveMessage() {
		String receive = "";

		try {
			receive = in.readUTF();
		} catch (Exception e) {
			// System.out.println("¿¡·¯");
			e.printStackTrace();
		} finally {
			return receive;
		}
	}
}