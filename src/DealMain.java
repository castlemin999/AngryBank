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
   
   // ������
   DealMain(Connection con, DataInputStream in, DataOutputStream out, HashMap<Integer, String> userInfo ) {
      this.con = con;
      this.out = out;
      this.in = in;
      this.userInfo = userInfo;
   }
   
   void DealMainMenu(){
      //1. �Ա�/��� 
            FinMgr finmgr = new FinMgr(con, in, out, userInfo);
            AccountList accountlist = new AccountList(con, in, out, userInfo);
            AccountBook accountbook = new AccountBook(con, in, out, userInfo);
            end: while (true) {
               SendMessage("\n--------------------�޴� ����--------------------\n                   1. ����� \n                   2. ���� ���� \n                   3. ��Ÿ \n                   4. ������ \n-------------------��޴� ���á�-------------------");
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
                  SendMessage("�����մϴ�.");
                  SendMessage("Exit!!!!");
                  break end;
               }
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


}
