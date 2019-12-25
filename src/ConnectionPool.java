

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

public class ConnectionPool {
	static {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	ArrayList<Connection> free; // 사용중이지 않은 Connection 객체
	ArrayList<Connection> used; // 사용중인 Connection 객체
	String url;
	String user;
	String password;
	int initialCons = 0; // 초기 커넥션 수
	int maxCons = 0; // 최대 커넥션 수
	int numCons = 0; // 현재 커넥션 수
	static ConnectionPool cp = null; // Singleton 패턴(관리자 하나만 주는 방식)을 위한 static 변수

	/*
	 * Singleton 패턴 
	 * 데이터베이스 접근처럼 일원적으로 관리되어야 할 업무는 여러군데서 접근 객체를 만들도록 허용하지 않고 단 1개의
	 * 객체만 만들도록 강요하고 이 1개의 객체로만 사용할 것을 강요하는 프로그램 방식이다.
	 * 
	 * 1) static으로 클래스 변수를 선언한다 
	 * 2) 생성자를 private으로 선언한다 
	 * 3) 일반적으로 클래스 객체를 리턴하는 getInstance()메서드를 만든다. 
	 * 4) getInstance()메서드에서는 최초로 1번만 객체를 생성하도록 한다. 
	 * 5) 외부클래스에서 이 클래스 객체를 요청할 때는 반드시 getInstance 메서드만을 사용해서 객체를 얻을 수 있다. 
	 * 6) 아무리 많은 스레드에서 요청을 해도 반환하는 객체는 유일하고 동일한 객체이다. (외부와의 연결통로가 단 하나)
	 */

	public static ConnectionPool getInstance(String url, String user, String password, int initCons, int maxCons) {
		// getInstance 를 호출해야만 객체 생성가능

		try {
			if (cp == null) {
				// static 메서드에서 동기화 하는 방법
				synchronized (ConnectionPool.class) { // 클래스 객체(클래스를 동시에 접근하지 못한다)
					cp = new ConnectionPool(url, user, password, initCons, maxCons); // 내가 내 클래스 안에서 만들어서 return
				}
			} // 두번째 들어왔을 땐(다른 스레드들이 계속 요구해도) if문 안돌고 나와서 그냥 원래 것을 return = 딱 하나의 객체만 만들 수 있다
				// => db접속 관리 객체를 단 하나만 줘서 오류를 방지한다.
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return cp;
	}
	/*
	 * 해당 객체를 동기화하는 또다른 방법 public static synchronized ConnectionPool
	 * getInstance(String url, String password,int initCons, int maxCons) { }
	 */

	// 생성자 private (나만 만들 수 있다)
	// 내 안에서 모든 객체를 생성해서 만들겠다는 의지를 가진 애 =>Singleton 패턴
	// 내가 생성해서 줄게(관리자 하나만 생성하기 위해서)
	private ConnectionPool(String url, String user, String password, int initCons, int maxCons) throws SQLException {
		this.url = url; //오라클 연결에 필요
		this.user = user;
		this.password = password;
		this.initialCons = initCons; // 최초에 Connection을 몇 개 만들지
		this.maxCons = maxCons; // 최대로 몇개까지 만들건지

		if (initialCons < 0) // -1을 주면 알아서 결정해라
			this.initialCons = 5; // 그래서 나는 5개로 결정
		if (maxCons < 0)
			this.maxCons = 100; // 알아서 10개로 결정

		// 초기 커넥션 개수를 각각의 ArrayList에 저장할 수 있도록
		// 초기 커넥션 개수만큼 ArrayList를 생성한다.
		free = new ArrayList<Connection>(initialCons);
		used = new ArrayList<Connection>(initialCons);

		// 초기 커넥션 개수만큼 Connection 객체를 생성하자
		while (this.numCons < this.initialCons) {
			addConnection(); // 1개씩 객체를 추가하는 메서드 ->이걸 소환하면 getNewConnection() 되서 5개를 생성
		}
	}

	private void addConnection() throws SQLException {
		free.add(getNewConnection()); // 5개가 등록되면 멈춘다

	}

	private Connection getNewConnection() throws SQLException {
		Connection con = null;
		con = DriverManager.getConnection(this.url, this.user, this.password); //Connection 객체 내부에 정보저장
		System.out.println("About to connect to " + con);
		this.numCons++;
		return con;
	}

	// 외부에서 오라클 연결 객체를 요구하는 메서드
	public synchronized Connection getConnection() throws SQLException { // 동시에 요청못하도록 동기화(동시에 들어오면 대기)
		if (free.isEmpty()) {
			while (this.numCons < maxCons) {
				addConnection(); //5개를 다 썼으면 추가적으로 5개 더 생성 
			}
		}
		Connection _con = free.get(free.size() - 1); //마지막 커넥션 개체를 준다
		free.remove(_con);
		used.add(_con); //free에 있던건 제거하고 used에 추가 후에 return으로 사용자에게 제공 
		
		return _con;
	}
	
	// 사용중인 오라클 연결 객체를 반납하는 메서드
	// used -> free 로 arraylist 위치만 변경해준다.
	public synchronized void releaseConnection(Connection _con) {
		try {
			boolean flag = false;
			if(used.contains(_con)) { //사용중인지 확인
				used.remove(_con); //사용중이면 제거하고
				free.add(_con); //free에 추가
			}else {
				throw new SQLException("ConnectionPool: "+"에 있지 않습니다"); //없으면 예외처리
			}
		}catch(SQLException e) { //에러 자체 처리
			System.out.println(e.getMessage());
		}

	}
	
	// 모든 Connection을 종료한다
	// 서비스 종료시 ConnectionPool을 그만 사용한다
	public void closeAll() {
		// used에 있는 커넥션을 모두 삭제하고 닫는다
		for(int i=0; i<used.size();i++) {
			Connection _con = (Connection)used.get(i);
			try {
				used.remove(i--);
				_con.close();
			}catch(SQLException e) {
				e.printStackTrace();
			}
		}
		
		// free에 있는 커넥션을 모두 삭제하고 닫는다
		for(int i=0; i<free.size();i++) {
			Connection _con = (Connection)free.get(i);
			try {
				free.remove(i--);
				_con.close();
			}catch(SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	// 최대 연결 개수
	public int getMaxCons() {
		return this.maxCons;
	}
	
	// 현재 할당 연결된 개수
	public int getNumsCons() {
		return this.numCons;
	}
}




