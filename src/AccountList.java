import java.io.*;
import java.sql.*;
import java.util.*;

class AccountList { // 내역관리
   ResultSet rs = null;
   StringBuffer sb = new StringBuffer();
   Connection con = null;
   DataOutputStream out;
   DataInputStream in;
   HashMap<Integer, String> userInfo = new HashMap<Integer, String>();
   int ano;
   int Pmoney;
   int Mmoney;

   AccountList(Connection con, DataInputStream in, DataOutputStream out, HashMap<Integer, String> userInfo) {
      this.con = con;
      this.in = in;
      this.out = out;
      this.userInfo = userInfo;
      Collection collection = userInfo.keySet();
      Iterator iter = collection.iterator();
      while (iter.hasNext()) {
         this.ano = (int) iter.next();
      }
   } // 생성자

   public void accountlistMain() {
      end: while (true) {
         SendMessage("\n--------------------계좌 내역--------------------\n                1. 전체 내역 \n                2. 입금 내역 \n                3. 출금 내역 \n                4. 나가기 \n-------------------↓메뉴 선택↓-------------------");
         String acselect = ReceiveMessage();
         SendMessage("\n");
         switch (acselect) {
         case "1":
            totalList(); // 전체내역
            break;

         case "2":
            depositList(); // 입금내역
            break;

         case "3":
            withdrawList(); // 출금내역
            break;

         case "4":
            SendMessage("이전 메뉴로 돌아갑니다.");
            break end;
         }
      }
   } // accountlistMain

   public void totalList() { // 전체내역
      try {
         Statement stmt = con.createStatement();
         sb.setLength(0);
         sb.append("SELECT * FROM pmlist WHERE ano = '" + ano + "'");
         rs = stmt.executeQuery(sb.toString());
         SendMessage("===============전체내역===============");
         while (rs.next()) {            
            SendMessage(
                             "\n입(P)/출(M):    " + rs.getString(2)+
                             "\n계좌번호:    " + rs.getString(3)+ 
                             "\n금액:    " + rs.getString(4)+
                             "\n지출내역:    " + rs.getString(5)+
                             "\n거래날짜:    " + rs.getString(6) 
                             );
         } // while
         int result = getBalance(ano);
         SendMessage("\n잔액: " + result);
      } catch (SQLException e) {
         e.printStackTrace();
      }
   } // TotalList

   public void depositList() { // 입금내역
      try {
         Statement stmt = con.createStatement();         
         sb.setLength(0);
         sb.append("SELECT * FROM pmlist WHERE pmlist = 'P' AND ano = '" + ano + "'");
         rs = stmt.executeQuery(sb.toString());
         SendMessage("==========입금내역==========");
         while (rs.next()) {
            SendMessage(
                             "\n거래번호: " + rs.getString(1)+
                               "\n입금(P)/출금(M): " + rs.getString(2)+
                               "\n계좌번호: " + rs.getString(3)+
                               "\n금액: " + rs.getString(4)+
                               "\n지출내역: " + rs.getString(5)+
                               "\n거래날짜: " + rs.getString(6) + "\n"
                               );
         }
      } catch (SQLException e) {
         e.printStackTrace();
      }
   } // depositList

   public void withdrawList() { // 출금내역
      try {
         Statement stmt = con.createStatement();
         sb.setLength(0);
         sb.append("SELECT * FROM pmlist WHERE pmlist = 'M' AND ano = '" + ano + "'");
         rs = stmt.executeQuery(sb.toString());
         SendMessage("==========출금내역==========");
         while (rs.next()) {
            SendMessage(
                             "\n거래번호: " + rs.getString(1)+
                               "\n입금(P)/출금(M): " + rs.getString(2)+
                               "\n계좌번호: " + rs.getString(3)+
                               "\n금액: " + rs.getString(4)+
                               "\n지출내역: " + rs.getString(5)+
                               "\n거래날짜: " + rs.getString(6) + "\n"
                               );
         }
      } catch (SQLException e) {
         e.printStackTrace();
      }
   } // withdrawList
   
	//잔액 겟
	public int getBalance(int ano) {
	 	int Pmoney =0, Mmoney = 0, mymoney =0;
		Statement stmt;
		try {
			  stmt = con.createStatement();
			  rs = stmt.executeQuery("SELECT SUM(tlist) FROM pmlist WHERE pmlist IN 'P' AND ano = "+ano);
		      while (rs.next()) {
		    	  Pmoney = rs.getInt(1);
		      }
		      
		      rs = stmt.executeQuery("SELECT SUM(tlist) FROM pmlist WHERE pmlist IN 'M' AND ano = "+ano);
		      while (rs.next()) {
		      Mmoney = rs.getInt(1);
		      }
		      mymoney = Pmoney-Mmoney;
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
   } // SendMessage

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
   } // ReceiveMessage
} // AccountList