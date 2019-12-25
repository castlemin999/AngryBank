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

//1. 입금/출금 
public class FinMgr { // 입출금 관리
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
	String today = df.format(cal.getTime());// 현재 날짜 넣기

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

	public void finMakeAcc() throws SQLException { // 통장 만들지 말지

		end: while (true) {
			SendMessage("-----------------------계좌 거래--------------------");
			SendMessage("                   1. 입금 / 출금 서비스");
			SendMessage("                   2. 예금 개설   ");
			SendMessage("                   3. 예금 입금   ");
			SendMessage("                   4. 초기 화면 돌아가기  ");
			SendMessage("--------------------↓메뉴 선택↓--------------------");

			String finselect = ReceiveMessage();
			switch (finselect) {
			case "1":
				finmgrMain(); // 단순 입출금 서비스
				break;
			case "2":
				makeDepAcc(); // 예금 만들기
				break;

			case "3":
				depositMoney(); // 예금 입금
				break;

			case "4":
				break end;

			} // switch
		} // while
	}// finMakeAcc

//   
//   적금 만들때 ->예금
//   1.돈은 기존 통장에서 빼옴(잔액 내에서만 가능)
//   2. 해지하기
//   신청하기 누르면 오늘 날짜랑 db에 해지날짜랑 같은지 비교해서 다르면 x
//   같으면 해주기
//   ->만든다면 해지하는날에 신청하기 누르면 원금에 이자 10%넣어서 
//   기존계좌로 넣어줌
	public void makeDepAcc() throws SQLException {// 예금 게썰
		int uno = 0;
		String userano = "";
		ResultSet rs1;
		PreparedStatement pstmt = null;
		Statement stmt = con.createStatement();
		String rnew_ano = "";
		String pnum = "";
		while (true) {
			try {
				SendMessage("예금 통장 개설에 동의하십니까? (y/n)");
				String ans = ReceiveMessage();
				if (ans.equals("Y") || ans.equals("y")) {
					try {
						stmt = con.createStatement();
						boolean sizecheck = true;
						while (sizecheck) {
							SendMessage("가입 시 작성한 핸드폰 번호를 입력하세요(형식-01011111234): ");
							pnum = ReceiveMessage();
							if (pnum.length() > 11 || pnum.length() < 11) {
								SendMessage("올바른 형식이 아닙니다.(숫자만 입력)");
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
					SendMessage("예금 통장 개설 하셨습니다");
					SendMessage("계좌번호:" + rnew_ano + "입니다");
					break;
				} // if
				else if (ans.equals("N") || ans.equals("n")) {
					SendMessage("뒤로 돌아가겠습니다.");
					finMakeAcc();
					break;
				} // else if
				else {
					SendMessage("다시 입력 해주세요.");
					continue;
				}

			} // try
			catch (SQLException e) {
				e.printStackTrace();
			} // catch

		} // while
	}// makeDepAcc

	public void depositMoney() throws SQLException { // 예금 입금
		SendMessage("예금 계좌를  입력 해주세요.");
		String oano1 = ReceiveMessage();
		int int_oano1 = Integer.parseInt(oano1);
		stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT userano FROM useracc WHERE userano = '" + int_oano1 + "'");

		if (rs.next()) {
			if (rs.getString(1).equals(oano1)) {
				SendMessage("입금할 금액을 입력 해주세요.");
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
						SendMessage("잔액부족입니다.\n 현재 잔액: " + mymoney);
						return;
					}
					if (mymoney == 0) {
						SendMessage("잔액부족입니다. \n 현재 잔액: " + mymoney);
						return;
					}
					if (mymoney < 0) {
						SendMessage("잔액부족입니다. \n  현재 잔액: " + mymoney);
						return;
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}

				while (true) {
					SendMessage("비밀번호를 입력 해주세요.");
					String pw = ReceiveMessage();
					rs = stmt.executeQuery("SELECT upw FROM bankuser WHERE upw = '" + pw + "'");
					SendMessage("예금 내역을 입력 해주세요.");
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
							SendMessage("예금 계좌에 입금 되었습니다.");
							sb.setLength(0);
							break;
						} else {
							SendMessage("------------------------------------");
							SendMessage("비번 틀렸습니다.");
							continue;
						}
					}
				} // while
			} else {
				SendMessage("계좌번호 다시 입력하세요");
				return;
			}
		}
	}// 끝

	public void finmgrMain() throws SQLException {

		end: while (true) {
			SendMessage(
					"\n--------------------계좌 거래--------------------\n                   1. 입금 \n                   2. 출금 \n                   3. 송금 \n                   4. 나가기 \n-------------------↓메뉴 선택↓-------------------");
			String finselect = ReceiveMessage();
			switch (finselect) {
			case "1":
				selfDeposit(); // 나 -> 나 (입금)
				break;
			case "2":
				selfWithdraw(); // 나 -> 나 (출금)
				break;
			case "3":
				otherWithdraw(); // 나 -> 타인 (수금)
				break;
			case "4":
				SendMessage("초기 메뉴로 나갑니다.");
				break end;
			} // switch
		} // while
	} // finmgrMain

	public void selfDeposit() throws SQLException { // 나 -> 나 (입금)
		SendMessage("\n입금할 금액을 입력 해주세요.");
		String plist = ReceiveMessage();
		int int_plist = Integer.parseInt(plist);
		SendMessage("거래 내역을 입력 해주세요.");
		String tlist = ReceiveMessage();
		while (true) {
			SendMessage("비밀번호를 입력 해주세요.");
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
					SendMessage("\n입금 되었습니다.");
					break;
				}
			}
		}

	} // selfDeposit

	public void selfWithdraw() throws SQLException { // 나 -> 나 (출금)
		SendMessage("출금할 금액을 입력 해주세요.");
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
				System.out.println("남은 금액" + mymoney);
				System.out.println("내가 적은 금액" + int_mlist);
				System.out.println(" mymoney < int_mlist" + mymoney);
				SendMessage("잔액부족입니다.\n 현재 잔액: " + mymoney);
				System.out.println("잔액부족입니다");
				return;
			}
			if (mymoney == 0) {
				SendMessage("잔액부족입니다. \n 현재 잔액: " + mymoney);
				return;
			}
			if (mymoney < 0) {
				SendMessage("잔액부족입니다. \n  현재 잔액: " + mymoney);
				return;
			}
			if (mymoney > int_mlist) {
				System.out.println(" mymoney>int_mlist" + mymoney);
			}

			System.out.println("최종 내돈" + mymoney);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		SendMessage("거래 내역을 입력 해주세요.");
		String tlist = ReceiveMessage();
		while (true) {
			SendMessage("비밀번호를 입력 해주세요.");
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
					SendMessage("\n출금 되었습니다.");
					break;
				}
			}
		}

	} // selfWithdraw

	public void otherWithdraw() throws SQLException { // 타인 -> 나 (입금)
		SendMessage("상대방 계좌를  입력 해주세요.");
		String oano = ReceiveMessage();
		stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT ano FROM bankuser WHERE ano = '" + oano + "'");

		if (rs.next()) {
			if (rs.getString(1).equals(oano)) {
				SendMessage("입금할 금액을 입력 해주세요.");
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
						System.out.println("남은 금액" + mymoney);
						System.out.println("내가 적은 금액" + int_plist);
						System.out.println(" mymoney < int_mlist" + mymoney);
						SendMessage("잔액부족입니다.\n 현재 잔액: " + mymoney);
						System.out.println("잔액부족입니다");
						return;
					}
					if (mymoney == 0) {
						SendMessage("잔액부족입니다. \n 현재 잔액: " + mymoney);
						return;
					}
					if (mymoney < 0) {
						SendMessage("잔액부족입니다. \n  현재 잔액: " + mymoney);
						return;
					}
					if (mymoney > int_plist) {
						System.out.println(" mymoney>int_mlist" + mymoney);
					}

					System.out.println("최종 내돈" + mymoney);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				SendMessage("거래 내역을 입력 해주세요.");
				String tlist = ReceiveMessage();
				while (true) {
					SendMessage("비밀번호를 입력 해주세요.");
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
							SendMessage("상대방 계좌에 입금 되었습니다.");
							sb.setLength(0);
							break;
						} else {
							SendMessage("------------------------------------");
							SendMessage("비번 틀렸씁니다.");
							continue;
						}

					}

				} // while
			} else {
				SendMessage("계좌번호 다시 입력하세요");
				return;
			}
		}
	}

	// } // otherWithdraw

	// 메시지 보내기
	public void SendMessage(String message) {
		try {
			out.writeUTF(message);
			out.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 메시지 받기
	// @SuppressWarnings("finally")
	public String ReceiveMessage() {
		String receive = "";

		try {
			receive = in.readUTF();
		} catch (Exception e) {
			// System.out.println("에러");
			e.printStackTrace();
		} finally {
			return receive;
		}
	}
} // FinMgr