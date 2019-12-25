import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;

public class AccountBook { // �����
   static Connection con = null;
   static PreparedStatement pstmt = null;
   ResultSet rs = null;
   DataOutputStream out;
   DataInputStream in;
   StringBuffer sb = new StringBuffer();
   HashMap<Integer, String> userInfo = new HashMap<Integer, String>();

   AccountBook(Connection con, DataInputStream in, DataOutputStream out, HashMap<Integer, String> userInfo) {
      this.con = con;
      this.out = out;
      this.in = in;
      this.userInfo = userInfo;
   }

   public void accountbookMain() {
      end: while (true) {
             SendMessage("\n--------------------ȯ�� �� ��������--------------------\n                   1. ȯ��(��->��) \n                   2. ȯ��(��->��) \n                   3. ���� \n                   4. ������ \n-------------------��޴� ���á�-------------------");
         String select = ReceiveMessage();
         switch (select) {
         case "1":
            inputIncome(); // �����Է�
            break;
         case "2":
            inputExpenditure(); // �����Է�
            break;
         case "3":
            deleteID(); // ����
            break;
         case "4":
            SendMessage("���� �޴��� ���ư��ϴ�.");
            break end;
         }
      }
   }

   public void inputIncome() {
      naver nav = new naver();
      int money = 0;
      int result = 0;
      int[] exchangelist = new int[4];
      exchangelist = nav.exchangeMoney();
      String[] nationlist = { "USD", "JPY", "EUR", "CNY" };

      SendMessage("���� ȯ����");
      for (int i = 0; i < exchangelist.length; i++) {
         SendMessage(nationlist[i]);
         SendMessage(exchangelist[i] + " �Դϴ�.");
      }

      while (true) {
         SendMessage("1.�̱� 2.�Ϻ� 3.���� 4.�߱� 5.��");
         String select = ReceiveMessage();
         switch (select) {
         case "1":
            result = ExchangeMoney(exchangelist[0]);
            if (result == 0) {
               break;
            } else {
               SendMessage(result + "$ ����Ǿ����ϴ�.");
               break;
            }

         case "2":
            result = ExchangeMoney(exchangelist[1]);
            if (result == 0) {
               break;
            } else {
               SendMessage(result + "�� ����Ǿ����ϴ�.");
               break;
            }
         case "3":
            result = ExchangeMoney(exchangelist[1]);
            if (result == 0) {
               break;
            } else {
               SendMessage(result + "�� ����Ǿ����ϴ�.");
               break;
            }
         case "4":
            result = ExchangeMoney(exchangelist[1]);
            if (result == 0) {
               break;
            } else {
               SendMessage(result + "� ����Ǿ����ϴ�.");
               break;
            }
         case "5":
            return;
         }
      }
   }

   public void inputExpenditure() {
      naver nav = new naver();
      int money = 0;
      int result = 0;
      int[] exchangelist = new int[4];
      exchangelist = nav.exchangeMoney();
      String[] nationlist = { "USD", "JPY", "EUR", "CNY" };

      SendMessage("���� ȯ����");
      for (int i = 0; i < exchangelist.length; i++) {
         SendMessage(nationlist[i]);
         SendMessage(exchangelist[i] + " �Դϴ�.");
      }

      while (true) {
         SendMessage("1.�̱� 2.�Ϻ� 3.���� 4.�߱� 5.��");
         String select = ReceiveMessage();
         switch (select) {
         case "1":
            result = MoneyExchange(exchangelist[0]);
            if (result == 0) {
               break;
            } else {
               SendMessage(result + "$ ����Ǿ����ϴ�.");
               break;
            }
         case "2":
            result = MoneyExchange(exchangelist[1]);
            if (result == 0) {
               break;
            } else {
               SendMessage(result + "�� ����Ǿ����ϴ�.");
               break;
            }
         case "3":
            result = MoneyExchange(exchangelist[1]);
            if (result == 0) {
               break;
            } else {
               SendMessage(result + "�� ����Ǿ����ϴ�.");
               break;
            }
         case "4":
            result = MoneyExchange(exchangelist[1]);
            if (result == 0) {
               break;
            } else {
               SendMessage(result + "� ����Ǿ����ϴ�.");
               break;
            }
         case "5":
            return;
         }
      }
   }

   public int ExchangeMoney(int exchange) {
      int result = 0;
      boolean check = true;
      while (check) {
         SendMessage("�ݾ��� �Է����ּ���. �ּұݾ��� 10000�� �Դϴ�.");
         int money = Integer.parseInt(ReceiveMessage());

         if (money >= 10000) {
            result = money / exchange;
            System.out.println(result);
            boolean che = true;
            while (che) {
               SendMessage(result + "�Դϴ�.ȯ�� �����Ͻðڽ��ϱ�?(y/n)");
               String ans = ReceiveMessage();

               if (ans.equals("y")) {
                  SendMessage("����Ǿ����ϴ�.");
                  che = false;
                  return result;
               } else if (ans.equals("n")) {
                  SendMessage("��ҵǾ����ϴ�.");
                  return 0;
               } else {
                  SendMessage("y/n �� �Է����ּ���.");
               }
            }
         } else {
            SendMessage("�ּұݾ� 10000�� �Դϴ�.");
         }
      }
      return result;
   }

   public int MoneyExchange(int exchange) {
      int result = 0;
      boolean check = true;
      while (check) {
         SendMessage("�ݾ��� �Է����ּ���. �ּұݾ��� 1000�� �Դϴ�.");
         int money = Integer.parseInt(ReceiveMessage());

         if (money > 1000) {
            result = money * exchange;
            System.out.println(result);
            boolean che = true;
            while (che) {
               SendMessage(result + "�Դϴ�.ȯ�� �����Ͻðڽ��ϱ�?(y/n)");
               String ans = ReceiveMessage();

               if (ans.equals("y")) {
                  SendMessage("����Ǿ����ϴ�.");
                  che = false;
                  return result;
               } else if (ans.equals("n")) {
                  SendMessage("��ҵǾ����ϴ�.");
                  return 0;
               } else {
                  SendMessage("y/n �� �Է����ּ���.");
               }
            }
         } else {
            SendMessage("�ּұݾ� 1000���Դϴ�");
         }
      }
      return result;
   }

   public void deleteID() { // ��������
      String uname = "";String upw = "";String pnum = "";String ppnum = "";
      int uno = 0; int ano = 0;
      boolean check = true;
      String YorN = "";
      ResultSet rs;
      DataOutputStream out;
      DataInputStream in;
      StringBuffer sb = new StringBuffer();
      PreparedStatement pstmt = null;

      try {
         Statement stmt = con.createStatement();
         while (check) {
            SendMessage("��ȣ�� �Է��ϼ���.");
            ppnum = ReceiveMessage();
            int checkuser=0;
            Collection collection = userInfo.keySet(); // ���¹�ȣ
            Iterator iter = collection.iterator();
            while (iter.hasNext()) {
               checkuser = (int) iter.next(); // ���� ���¹�ȣ
            }
            sb.setLength(0);
            sb.append("SELECT uno,uname,upw,pnum,ano FROM bankuser WHERE ano = " + checkuser + "");
            rs = stmt.executeQuery(sb.toString());
            while (rs.next()) {
               uno = rs.getInt(1);
               uname = rs.getString(2);
               upw = rs.getString(3);
               pnum = rs.getString(4);
               ano = rs.getInt(5);
            }
            ////////////////// ������ ���� ���� �����ϴ��� Ȯ�� �ܰ�////////////////////////

            
            if (pnum.equals(ppnum)) { // ���� ���� ����
               while (true) {
                  SendMessage(uname + "���� ������ ������ �����Ͻðڽ��ϱ�?" + "\n(1. YES    2. NO)");
                  YorN = ReceiveMessage();
                  if (YorN.equals("1")) { // ���� ����
                     SendMessage("��й�ȣ�� �Է��ϼ���.");
                     String uupw = ReceiveMessage();
                     if (uupw.equals(upw)) { // ��� Ȯ��
                        // ���� ������1 
                        sb.setLength(0);
                        sb.append("DELETE FROM BANKUSER WHERE uno ="+uno);
                        stmt.executeUpdate(sb.toString());
                        con.commit();
                        
                        // ���� ������2
                        sb.setLength(0);
                        sb.append("DELETE FROM USERACC WHERE userno ="+uno);
                        stmt.executeUpdate(sb.toString());
                        con.commit();
                        
                        SendMessage("�����Ǿ����ϴ�. �ȳ��� ������.");
                        SendMessage("Exit!!!!");
                     } else {// ��� ����
                        SendMessage("��й�ȣ�� Ʋ�Ƚ��ϴ�.");
                     }
                  } else if (YorN.equals("2")) {
                     SendMessage("���� �޴��� ���ư��ϴ�.");
                     return;
                  } else {
                     SendMessage("�ٽ� �Է��ϼ���.");
                  }
               }

            } else { //���� ���� ����
               SendMessage("���� ������ ���� �����մϴ�. ���� �޴��� ���ư��ϴ�.");
               break;
            }

         }

      } catch (SQLException e1) {
         e1.printStackTrace();
      }

   } // deleteID


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
   @SuppressWarnings("finally")
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