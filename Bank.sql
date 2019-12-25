
--은행 유저
CREATE TABLE bankuser (
uno NUMBER
CONSTRAINT bankusser_uno PRIMARY KEY,
uname VARCHAR2 (50) NOT NULL,
upw VARCHAR2(50) NOT NULL,
pnum VARCHAR2(50) NOT NULL, --폰번
ano NUMBER --계좌번호
CONSTRAINT bankuser_ano UNIQUE
);

--계좌리스트
CREATE TABLE acclist (
ano NUMBER NOT NULL
CONSTRAINT acclist_ano_fk REFERENCES bankuser (ano)
);

--계좌 유저 테이블
CREATE TABLE bankacc (
uno NUMBER,
ano NUMBER NOT NULL
);
--거래내역
CREATE TABLE pmlist (
index_nu NUMBER,
pmlist VARCHAR2 (1) CONSTRAINT pmlist_ck CHECK (pmlist IN('P','M')) ,
ano NUMBER NOT NULL,
tlist NUMBER NOT NULL,
etc VARCHAR2(200) NOT NULL,
tdate DATE DEFAULT  NULL,
sano NUMBER,
CONSTRAINT pmlist_index_nu PRIMARY KEY (index_nu)
);


--유저 계좌관리
CREATE TABLE USERACC (
userno NUMBER
CONSTRAINT USERACC_userno PRIMARY KEY,
userano NUMBER, --계좌번호
depositacc VARCHAR2 (1) CONSTRAINT useracc_ck CHECK (depositacc IN('Y','N')),--적금 여부
mdate VARCHAR2(60), --만든 날짜
edate VARCHAR2(60), -- 해지날짜
depositelist NUMBER
);
--제약조건
--alter table bankacc  add foreign key(ano) references bankuser (ano);
--alter table bankacc  add foreign key(uno) references bankuser (uno);
--alter table pmlist  add foreign key(ano) references bankuser (ano);



--거래내역 인덱스 시퀀스
CREATE SEQUENCE seq_pmlist_index_nu
INCREMENT BY 1
START WITH 1; 
--시퀀스 생성 

--계쫘번호 시컨스
CREATE SEQUENCE seq_acclist_ano
INCREMENT BY 1
START WITH 110254000001; 
--시퀀스 생성 

--고객번호 시퀀스 
CREATE SEQUENCE seq_bankuser_uno
INCREMENT BY 1
START WITH 1; 
--시퀀스 생성 

CREATE SEQUENCE seq_acclist_ano_real
INCREMENT BY 1
START WITH 110254001; 

--추가 계좌 시퀀스
CREATE SEQUENCE seq_useracc_userano
INCREMENT BY 1
START WITH 113254001; 
--시퀀스 생성 


--시간 (년월일만 나옴) 
ALTER SESSION SET NLS_DATE_FORMAT='yyyy-mm-dd';
