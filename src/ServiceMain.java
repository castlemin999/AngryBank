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

	// 생성자
	ServiceMain(Socket sock, Connection con) throws IOException {
		this.sock = sock;
		this.con = con;
		this.out = new DataOutputStream(new BufferedOutputStream(sock.getOutputStream()));
		this.in = new DataInputStream(sock.getInputStream());
	}

	@Override
	public void run() {
		// usermgr, dealmain 객체 생성
		UserMgr usermgr = new UserMgr(con, in, out);
		userInfo = null;
		// 로그인 여부 확인
		do {
			// 로그인 하는 메서드
			userInfo = usermgr.UserMgrMenu();
			System.out.println("로그인 끝");
		} while (userInfo.isEmpty());
		// 로그인 되면 dealmain 실행'
		System.out.println(userInfo);
		DealMain deal = new DealMain(con, in, out, userInfo);
		deal.DealMainMenu();

		// 모든게 끝나면 소켓 정리
		try {
			sock.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

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
} // ServiceMain

class UserMgr {
	Connection con = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	DataOutputStream out;
	DataInputStream in;
	HashMap<Integer, String> userInfo = new HashMap<Integer, String>();

	// 생성자
	UserMgr(Connection con, DataInputStream in, DataOutputStream out) {
		this.con = con;
		this.out = out;
		this.in = in;
	}

	// 메뉴보여주기
	public HashMap<Integer, String> UserMgrMenu() { // 회원관리
		while (true) {
			SendMessage(
					"\n------------★WELLCOME ANGRY3 BANK★----------\n                1. 회원가입 \n                2. 로그인 \n                3. 계좌번호 찾기 \n                4. 비밀번호 찾기 \n                5. 프로그램 종료 \n-------------------↓메뉴 선택↓-------------------");
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
				SendMessage("프로그램을 종료합니다.");
				SendMessage("Exit!!!!");
			}
		}
	}

	// 1.로그인 -> hm으로 계좌,비번 리턴
	public HashMap<Integer, String> logIn() { // 로그인
		String uname = "", upw = "";
		String pnum = "";
		boolean check = true;
		try {
			while (check) {
				Statement stmt;
				ResultSet rs;
				SendMessage("핸드폰 번호를 입력하세요: ");
				pnum = ReceiveMessage();
				SendMessage("비밀번호를 입력하세요: ");
				upw = ReceiveMessage();

				stmt = con.createStatement();
				rs = stmt.executeQuery("SELECT uname,upw,pnum,ano FROM BANKUSER WHERE pnum = '" + pnum + "'");

				if (rs.next()) {
					if (rs.getString(3).equals(pnum)) {
						System.out.println(rs.getString(3));
						if (rs.getString(2).equals(upw)) {
							userInfo.put(rs.getInt(4), rs.getString(2));
							SendMessage(rs.getString(1) + "님 안녕하세요.");
							int mymoney = getBalance(rs.getInt(4));
							SendMessage("잔액: " + mymoney + "원");
							check = false;
						} else {
							SendMessage("비밀번호가 틀렸습니다.");
						}
					}
				} else {
					SendMessage("가입되지 않은 번호입니다.");
				}
			} // 끝
//			} // while 끝

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			return userInfo;
		}
	} // logIn

	// 2.회원가입
	public void signUp() throws SQLException { // 회원가입
		int uno=0; int ano =0;
		String uname = "", upw = "", pnum = "";
		ResultSet rs1;
		Statement stmt = con.createStatement();
		 SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		 Calendar cal = Calendar.getInstance();
		 String today = df.format(cal.getTime());//현재 날짜 넣기 
		
		String signupQuery1 = "INSERT INTO BANKUSER(uno,uname,upw,pnum,ano) VALUES(seq_bankuser_uno.nextval,?,?,?,seq_acclist_ano_real.nextval)";
		try {

			boolean a = Pattern.matches("^[ㄱ-ㅎ가-힣]*$", uname);

			while (true) {
				boolean check = true;
				SendMessage("이름을 입력하세요: ");
				uname = ReceiveMessage();

				if (Pattern.matches("^[ㄱ-ㅎ가-힣]*$", uname) == false) {
					SendMessage("한글만 가능합니다.");
				} else {
					SendMessage("비밀번호를 입력하세요: ");
					upw = ReceiveMessage();
					while (true) {
						SendMessage("핸드폰 번호를 입력하세요(형식-01011111234): ");
						pnum = ReceiveMessage();
						if (pnum.length() != 11) {
							SendMessage("올바른 형식이 아닙니다. (숫자만 입력)");
						} else {
							rs1 = stmt.executeQuery("SELECT pnum FROM BANKUSER WHERE pnum = '" + pnum + "'");
							if (rs1.next() != true) {
								break;
							} else {
								SendMessage(pnum + "은 이미 가입된 전화번호입니다.");
								SendMessage("ID 찾기를 이용해주세요.");
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
					SendMessage("회원가입 성공");
					SendMessage(uname+"님의 계좌번호는 "+ano+" 입니다.");
					break;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 3.계좌 찾기
	public void searchID() {
		String pnum = "";
		Statement stmt;
		try {
			stmt = con.createStatement();
			boolean sizecheck = true;
			while (sizecheck) {
				SendMessage("핸드폰 번호를 입력하세요(형식-01011111234): ");
				pnum = ReceiveMessage();
				if (pnum.length() > 11 || pnum.length() < 11) {
					SendMessage("올바른 형식이 아닙니다.(숫자만 입력)");
				} else {
					ResultSet rs1 = stmt.executeQuery("SELECT ano FROM BANKUSER WHERE pnum = '" + pnum + "'");
					if(rs1.next()) {
						if (rs1.getInt(1)!=1) {
							SendMessage(pnum + "으로 검색 결과");
							SendMessage("\n계좌는 " + rs1.getInt(1) + " 입니다.");
							sizecheck = false;
						}
					}else{
						SendMessage("가입되지 않은 전화번호입니다.\n 회원가입부터 진행해주세요.");
						return;
					}
				}

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}// searchID()

	// 4.비밀번호 찾기

	public void searchPW() {
		String pnum = "";
		Statement stmt;
		try {
			stmt = con.createStatement();
			boolean sizecheck = true;
			while (sizecheck) {
				SendMessage("핸드폰 번호를 입력하세요(형식-01011111234): ");
				pnum = ReceiveMessage();
				if (pnum.length() > 11 || pnum.length() < 11) {
					SendMessage("올바른 형식이 아닙니다.(숫자만 입력)");
				} else{
					ResultSet rs1 = stmt.executeQuery("SELECT uname,pnum,upw FROM BANKUSER WHERE pnum =" + pnum);
					if (rs1.next()) {
						if (rs1.getString(2) != null) {
							SendMessage("아이디를 입력하세요.");
							String uname = ReceiveMessage();
							if (rs1.getString(1).equals(uname)) {
								SendMessage("비밀번호는" + rs1.getString(3) + " 입니다.");
								sizecheck = false;
							}
						}
					}else{
						SendMessage("가입되지 않은 전화번호입니다.\n 회원가입부터 진행해주세요.");
						return;
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 잔액 겟
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
}