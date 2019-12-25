

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

//import pro.ConnectionPool;

public class ServerMain {
   public static void main(String[] args) throws IOException {
      ServerCon server = new ServerCon(10002);
      try {
         server.open();
      } catch (SQLException e) {
         e.printStackTrace();
      }
   }
}

class ServerCon{
   private ConnectionPool cp = null;
   Connection con = null;
   PreparedStatement pstmt = null;
   ResultSet rs = null;
   StringBuffer sb = new StringBuffer();
   int portNum;
   
   
   // 생성자
   ServerCon(int portNum){
      this.portNum = portNum;
      
   }
   //con 세팅
   void ConSetting() {
      String url = "jdbc:oracle:thin:@localhost:1521:xe";
      String user = "Bank";
      String password = "angry3";
      try {
         cp = ConnectionPool.getInstance(url, user, password, 5, 10);
         con = cp.getConnection();
//         System.out.println(cp.getNumCons() + "  <<<< 여기 인원수");
      } catch (SQLException e) {
         e.printStackTrace();
      }

   }
   public void open() throws IOException, SQLException {
      ServerSocket server = new ServerSocket(10002);
      ConSetting();
      while (true) {
         try {
            Socket sock = server.accept(); // 서버 수락
            System.out.println(sock.getInetAddress()+"접속완료");
            ServiceMain serverMain = new ServiceMain(sock,con);
            new Thread(serverMain).start(); // 스레드 실행
         }catch(Exception e) {
        	 break;
         } finally {
            try {
               if (rs != null)
                  rs.close();
               if (pstmt != null)
                  pstmt.close();
               if (con != null)
                  cp.releaseConnection(con);
            } catch (Exception e) {
               e.printStackTrace();
            }

         }
      }

   }
}