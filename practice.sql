SELECT * 
FROM pmlist 
WHERE ano IN 110254001
AND pmlist IN 'P';


SELECT ano FROM bankuser WHERE pnum = '01026781453';


SELECT uname FROM bankuser WHERE uname = '√÷º∫πŒ';

INSERT INTO acclist (ano) 
VALUES (seq_acclist_ano.nextval);

INSERT INTO bankuser(uno, uname, upw, pnum, ano)
VALUES (seq_bankuser_uno.nextval, 'abc', '123', '01026781453', seq_acclist_ano.nextval);

INSERT INTO pmlist (index_nu,pmlist,ano,tlist,etc,tdate) 
VALUES (seq_pmlist_index_nu.nextval, 'P', '110254000001', '20000', '±Ó±ÓªÁ∏‘¿Ω', '19/05/25');

INSERT INTO pmlist (index_nu,pmlist,ano,tlist,etc,tdate) 
VALUES (seq_pmlist_index_nu.nextval, 'P',110254000003, '12312', '±Ó±ÓªÁ∏‘¿Ω', '19/05/25');

SELECT * FROM pmlist WHERE index_nu = 1;