

import java.io.DataInputStream;
import java.io.DataOutputStream;
//import java.io.File;
//import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
//import java.util.Properties;
import java.util.Scanner;

public class Client implements Receiver, Sender, SignTable {
   String ip1;
   int port1;
   boolean sw = true;
   private DataInputStream dis;
   private DataOutputStream dos;
   private Scanner sc = new Scanner(System.in);
   private Receiver r; // 클래스
   private Sender s; // 클래스
   private Thread listener = null;

   
   /** 호스트 명 */
   private String host_name;

   public Client(String ip1, int port1) {
      this.ip1 = ip1;
      this.port1 = port1;
      r = this;
      s = this;
   }

   // 메인
   public static void main(String[] args) {
      Client client = new Client("127.0.0.1", 10002);
      client.open();
   }

   public void lisener(boolean sw) {
      listener = null;

      if (sw) {

         // 출력 스트림 오픈 (receive)
         (listener = new Thread(new Runnable() {

            @Override
            public void run() {
               try {
                  while (r != null) {
                     String line = r.receive();
                     // System.out.println(line);
                     if (line.equals("Exit!!!!")) {
                        System.exit(0);
                        return;
                     } else {
                        System.out.println(line);
                     }
                  }
               } catch (IOException e) {
                  e.printStackTrace();
               }
            }
         })).start();
      }
      
   }

   // open 메서드
   public void open() {

      try {
         Socket socket = new Socket("127.0.0.1", 10002);

         host_name = socket.getInetAddress().getHostName(); // 클라이언트 ip겟

         System.out.println(host_name + "님이 입장");

         // 스트림 초기화
         dis = new DataInputStream(socket.getInputStream());
         dos = new DataOutputStream(socket.getOutputStream());

         // 리스너 실행
         lisener(on);

         // send
         String command;
         while (!(command = readConsole()).equals(end)) {
            send(command);
         }

      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   /* 콘솔에서 한줄을 입력 받는다. */
   private String readConsole() {
      return sc.nextLine();
   }

   // send
   @Override
   public void send(String msg) throws IOException {

      dos.writeUTF(msg);
      dos.flush();

   }

   // receive
   @Override
   public String receive() throws IOException {

      return dis.readUTF();
   }

}