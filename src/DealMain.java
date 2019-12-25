import java.io.*;
import java.net.Socket;
import java.sql.*;
import java.util.HashMap;

public class DealMain{
//   Socket sock = null;
   static Connection con = null;
   static PreparedStatement pstmt = null;
   ResultSet rs = null;
   DataOutputStream out;
   DataInputStream in;
   StringBuffer sb = new StringBuffer();
   HashMap<Integer, String> userInfo = new HashMap<Integer, String>();
   
   // 생성자
   DealMain(Connection con, DataInputStream in, DataOutputStream out, HashMap<Integer, String> userInfo ) {
      this.con = con;
      this.out = out;
      this.in = in;
      this.userInfo = userInfo;
   }
   
   void DealMainMenu(){
      //1. 입금/출금 
            FinMgr finmgr = new FinMgr(con, in, out, userInfo);
            AccountList accountlist = new AccountList(con, in, out, userInfo);
            AccountBook accountbook = new AccountBook(con, in, out, userInfo);
            end: while (true) {
               SendMessage("\n--------------------메뉴 선택--------------------\n                   1. 입출금 \n                   2. 내역 보기 \n                   3. 기타 \n                   4. 나가기 \n-------------------↓메뉴 선택↓-------------------");
               String select = ReceiveMessage();
               switch (select) {
               case "1":
                  try {
                     finmgr.finMakeAcc();
                  } catch (SQLException e) {
                     e.printStackTrace();
                  }
                  break;
               case "2":
                  accountlist.accountlistMain();
                  break;
               case "3":
                  accountbook.accountbookMain();
                  break;
               case "4":
                  SendMessage("종료합니다.");
                  SendMessage("Exit!!!!");
                  break end;
               }
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


}
