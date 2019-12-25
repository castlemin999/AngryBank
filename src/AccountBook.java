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

public class AccountBook { // 가계부
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
             SendMessage("\n--------------------환전 및 계정관리--------------------\n                   1. 환전(한->외) \n                   2. 환전(외->한) \n                   3. 삭제 \n                   4. 나가기 \n-------------------↓메뉴 선택↓-------------------");
         String select = ReceiveMessage();
         switch (select) {
         case "1":
            inputIncome(); // 수입입력
            break;
         case "2":
            inputExpenditure(); // 지출입력
            break;
         case "3":
            deleteID(); // 삭제
            break;
         case "4":
            SendMessage("이전 메뉴로 돌아갑니다.");
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

      SendMessage("현재 환율가");
      for (int i = 0; i < exchangelist.length; i++) {
         SendMessage(nationlist[i]);
         SendMessage(exchangelist[i] + " 입니다.");
      }

      while (true) {
         SendMessage("1.미국 2.일본 3.유럽 4.중국 5.끝");
         String select = ReceiveMessage();
         switch (select) {
         case "1":
            result = ExchangeMoney(exchangelist[0]);
            if (result == 0) {
               break;
            } else {
               SendMessage(result + "$ 예약되었습니다.");
               break;
            }

         case "2":
            result = ExchangeMoney(exchangelist[1]);
            if (result == 0) {
               break;
            } else {
               SendMessage(result + "￥ 예약되었습니다.");
               break;
            }
         case "3":
            result = ExchangeMoney(exchangelist[1]);
            if (result == 0) {
               break;
            } else {
               SendMessage(result + "€ 예약되었습니다.");
               break;
            }
         case "4":
            result = ExchangeMoney(exchangelist[1]);
            if (result == 0) {
               break;
            } else {
               SendMessage(result + "元 예약되었습니다.");
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

      SendMessage("현재 환율가");
      for (int i = 0; i < exchangelist.length; i++) {
         SendMessage(nationlist[i]);
         SendMessage(exchangelist[i] + " 입니다.");
      }

      while (true) {
         SendMessage("1.미국 2.일본 3.유럽 4.중국 5.끝");
         String select = ReceiveMessage();
         switch (select) {
         case "1":
            result = MoneyExchange(exchangelist[0]);
            if (result == 0) {
               break;
            } else {
               SendMessage(result + "$ 예약되었습니다.");
               break;
            }
         case "2":
            result = MoneyExchange(exchangelist[1]);
            if (result == 0) {
               break;
            } else {
               SendMessage(result + "￥ 예약되었습니다.");
               break;
            }
         case "3":
            result = MoneyExchange(exchangelist[1]);
            if (result == 0) {
               break;
            } else {
               SendMessage(result + "€ 예약되었습니다.");
               break;
            }
         case "4":
            result = MoneyExchange(exchangelist[1]);
            if (result == 0) {
               break;
            } else {
               SendMessage(result + "元 예약되었습니다.");
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
         SendMessage("금액을 입력해주세요. 최소금액은 10000원 입니다.");
         int money = Integer.parseInt(ReceiveMessage());

         if (money >= 10000) {
            result = money / exchange;
            System.out.println(result);
            boolean che = true;
            while (che) {
               SendMessage(result + "입니다.환전 예약하시겠습니까?(y/n)");
               String ans = ReceiveMessage();

               if (ans.equals("y")) {
                  SendMessage("예약되었습니다.");
                  che = false;
                  return result;
               } else if (ans.equals("n")) {
                  SendMessage("취소되었습니다.");
                  return 0;
               } else {
                  SendMessage("y/n 만 입력해주세요.");
               }
            }
         } else {
            SendMessage("최소금액 10000원 입니다.");
         }
      }
      return result;
   }

   public int MoneyExchange(int exchange) {
      int result = 0;
      boolean check = true;
      while (check) {
         SendMessage("금액을 입력해주세요. 최소금액은 1000원 입니다.");
         int money = Integer.parseInt(ReceiveMessage());

         if (money > 1000) {
            result = money * exchange;
            System.out.println(result);
            boolean che = true;
            while (che) {
               SendMessage(result + "입니다.환전 예약하시겠습니까?(y/n)");
               String ans = ReceiveMessage();

               if (ans.equals("y")) {
                  SendMessage("예약되었습니다.");
                  che = false;
                  return result;
               } else if (ans.equals("n")) {
                  SendMessage("취소되었습니다.");
                  return 0;
               } else {
                  SendMessage("y/n 만 입력해주세요.");
               }
            }
         } else {
            SendMessage("최소금액 1000원입니다");
         }
      }
      return result;
   }

   public void deleteID() { // 계정삭제
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
            SendMessage("번호를 입력하세요.");
            ppnum = ReceiveMessage();
            int checkuser=0;
            Collection collection = userInfo.keySet(); // 계좌번호
            Iterator iter = collection.iterator();
            while (iter.hasNext()) {
               checkuser = (int) iter.next(); // 고유 계좌번호
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
            ////////////////// 본인이 본인 계정 삭제하는지 확인 단계////////////////////////

            
            if (pnum.equals(ppnum)) { // 본인 인증 성공
               while (true) {
                  SendMessage(uname + "님의 계정을 정말로 삭제하시겠습니까?" + "\n(1. YES    2. NO)");
                  YorN = ReceiveMessage();
                  if (YorN.equals("1")) { // 삭제 진행
                     SendMessage("비밀번호를 입력하세요.");
                     String uupw = ReceiveMessage();
                     if (uupw.equals(upw)) { // 비번 확인
                        // 삭제 쿼리문1 
                        sb.setLength(0);
                        sb.append("DELETE FROM BANKUSER WHERE uno ="+uno);
                        stmt.executeUpdate(sb.toString());
                        con.commit();
                        
                        // 삭제 쿼리문2
                        sb.setLength(0);
                        sb.append("DELETE FROM USERACC WHERE userno ="+uno);
                        stmt.executeUpdate(sb.toString());
                        con.commit();
                        
                        SendMessage("삭제되었습니다. 안녕히 가세요.");
                        SendMessage("Exit!!!!");
                     } else {// 비번 실패
                        SendMessage("비밀번호가 틀렸습니다.");
                     }
                  } else if (YorN.equals("2")) {
                     SendMessage("이전 메뉴로 돌아갑니다.");
                     return;
                  } else {
                     SendMessage("다시 입력하세요.");
                  }
               }

            } else { //본인 인증 실패
               SendMessage("본인 계정만 삭제 가능합니다. 상위 메뉴로 돌아갑니다.");
               break;
            }

         }

      } catch (SQLException e1) {
         e1.printStackTrace();
      }

   } // deleteID


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
   @SuppressWarnings("finally")
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