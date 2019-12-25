import java.io.*;
import java.sql.*;
import java.util.*;

class AccountList { // ��������
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
   } // ������

   public void accountlistMain() {
      end: while (true) {
         SendMessage("\n--------------------���� ����--------------------\n                1. ��ü ���� \n                2. �Ա� ���� \n                3. ��� ���� \n                4. ������ \n-------------------��޴� ���á�-------------------");
         String acselect = ReceiveMessage();
         SendMessage("\n");
         switch (acselect) {
         case "1":
            totalList(); // ��ü����
            break;

         case "2":
            depositList(); // �Աݳ���
            break;

         case "3":
            withdrawList(); // ��ݳ���
            break;

         case "4":
            SendMessage("���� �޴��� ���ư��ϴ�.");
            break end;
         }
      }
   } // accountlistMain

   public void totalList() { // ��ü����
      try {
         Statement stmt = con.createStatement();
         sb.setLength(0);
         sb.append("SELECT * FROM pmlist WHERE ano = '" + ano + "'");
         rs = stmt.executeQuery(sb.toString());
         SendMessage("===============��ü����===============");
         while (rs.next()) {            
            SendMessage(
                             "\n��(P)/��(M):    " + rs.getString(2)+
                             "\n���¹�ȣ:    " + rs.getString(3)+ 
                             "\n�ݾ�:    " + rs.getString(4)+
                             "\n���⳻��:    " + rs.getString(5)+
                             "\n�ŷ���¥:    " + rs.getString(6) 
                             );
         } // while
         int result = getBalance(ano);
         SendMessage("\n�ܾ�: " + result);
      } catch (SQLException e) {
         e.printStackTrace();
      }
   } // TotalList

   public void depositList() { // �Աݳ���
      try {
         Statement stmt = con.createStatement();         
         sb.setLength(0);
         sb.append("SELECT * FROM pmlist WHERE pmlist = 'P' AND ano = '" + ano + "'");
         rs = stmt.executeQuery(sb.toString());
         SendMessage("==========�Աݳ���==========");
         while (rs.next()) {
            SendMessage(
                             "\n�ŷ���ȣ: " + rs.getString(1)+
                               "\n�Ա�(P)/���(M): " + rs.getString(2)+
                               "\n���¹�ȣ: " + rs.getString(3)+
                               "\n�ݾ�: " + rs.getString(4)+
                               "\n���⳻��: " + rs.getString(5)+
                               "\n�ŷ���¥: " + rs.getString(6) + "\n"
                               );
         }
      } catch (SQLException e) {
         e.printStackTrace();
      }
   } // depositList

   public void withdrawList() { // ��ݳ���
      try {
         Statement stmt = con.createStatement();
         sb.setLength(0);
         sb.append("SELECT * FROM pmlist WHERE pmlist = 'M' AND ano = '" + ano + "'");
         rs = stmt.executeQuery(sb.toString());
         SendMessage("==========��ݳ���==========");
         while (rs.next()) {
            SendMessage(
                             "\n�ŷ���ȣ: " + rs.getString(1)+
                               "\n�Ա�(P)/���(M): " + rs.getString(2)+
                               "\n���¹�ȣ: " + rs.getString(3)+
                               "\n�ݾ�: " + rs.getString(4)+
                               "\n���⳻��: " + rs.getString(5)+
                               "\n�ŷ���¥: " + rs.getString(6) + "\n"
                               );
         }
      } catch (SQLException e) {
         e.printStackTrace();
      }
   } // withdrawList
   
	//�ܾ� ��
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
   
   
   
   // �޽��� ������
   public void SendMessage(String message) {
      try {
         out.writeUTF(message);
         out.flush();

      } catch (Exception e) {
         e.printStackTrace();
      }
   } // SendMessage

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
   } // ReceiveMessage
} // AccountList