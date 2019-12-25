
--���� ����
CREATE TABLE bankuser (
uno NUMBER
CONSTRAINT bankusser_uno PRIMARY KEY,
uname VARCHAR2 (50) NOT NULL,
upw VARCHAR2(50) NOT NULL,
pnum VARCHAR2(50) NOT NULL, --����
ano NUMBER --���¹�ȣ
CONSTRAINT bankuser_ano UNIQUE
);

--���¸���Ʈ
CREATE TABLE acclist (
ano NUMBER NOT NULL
CONSTRAINT acclist_ano_fk REFERENCES bankuser (ano)
);

--���� ���� ���̺�
CREATE TABLE bankacc (
uno NUMBER,
ano NUMBER NOT NULL
);
--�ŷ�����
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


--���� ���°���
CREATE TABLE USERACC (
userno NUMBER
CONSTRAINT USERACC_userno PRIMARY KEY,
userano NUMBER, --���¹�ȣ
depositacc VARCHAR2 (1) CONSTRAINT useracc_ck CHECK (depositacc IN('Y','N')),--���� ����
mdate VARCHAR2(60), --���� ��¥
edate VARCHAR2(60), -- ������¥
depositelist NUMBER
);
--��������
--alter table bankacc  add foreign key(ano) references bankuser (ano);
--alter table bankacc  add foreign key(uno) references bankuser (uno);
--alter table pmlist  add foreign key(ano) references bankuser (ano);



--�ŷ����� �ε��� ������
CREATE SEQUENCE seq_pmlist_index_nu
INCREMENT BY 1
START WITH 1; 
--������ ���� 

--���ҹ�ȣ ������
CREATE SEQUENCE seq_acclist_ano
INCREMENT BY 1
START WITH 110254000001; 
--������ ���� 

--����ȣ ������ 
CREATE SEQUENCE seq_bankuser_uno
INCREMENT BY 1
START WITH 1; 
--������ ���� 

CREATE SEQUENCE seq_acclist_ano_real
INCREMENT BY 1
START WITH 110254001; 

--�߰� ���� ������
CREATE SEQUENCE seq_useracc_userano
INCREMENT BY 1
START WITH 113254001; 
--������ ���� 


--�ð� (����ϸ� ����) 
ALTER SESSION SET NLS_DATE_FORMAT='yyyy-mm-dd';
