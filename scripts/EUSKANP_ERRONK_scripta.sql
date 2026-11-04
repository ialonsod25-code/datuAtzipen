
CREATE TYPE helbide_typ AS OBJECT(
    KODEPOSTALA VARCHAR2(5),
    HELBIDEA    VARCHAR2(45),
    PROBINTZIA  VARCHAR2(25),
    HERRIA      VARCHAR2(25)
);


CREATE TYPE helbide_taula_typ AS TABLE OF helbide_typ;


CREATE TYPE telefono_typ AS TABLE OF VARCHAR2(15);

CREATE TYPE langile_typ AS OBJECT(
    NAN             VARCHAR2(9),
    IZENA           VARCHAR2(25),
    ABIZENA         VARCHAR2(25),
    HELBIDEA        helbide_taula_typ,
    TELEFONOAK      telefono_typ,
    KONTRATATZEDATA DATE,
    SOLDATAOINARRIA NUMBER(8,2)
) NOT FINAL;


CREATE TYPE administrari_typ UNDER langile_typ (
    IKASKETAK VARCHAR2(50)
);


CREATE TYPE teknikari_typ UNDER langile_typ (
    EGINKIZUNA VARCHAR2(30)
);

CREATE TABLE administratzaileak OF administrari_typ (
    NAN PRIMARY KEY
)

NESTED TABLE HELBIDEA   STORE AS adm_helbide_nt
NESTED TABLE TELEFONOAK STORE AS adm_telefono_nt;

CREATE TABLE teknikariak OF teknikari_typ (
    NAN PRIMARY KEY
)
NESTED TABLE HELBIDEA   STORE AS tek_helbide_nt
NESTED TABLE TELEFONOAK STORE AS tek_telefono_nt;


INSERT INTO administratzaileak VALUES (
    administrari_typ(
        '11111111A', 'Iker', 'Alonso',
        helbide_taula_typ(
            helbide_typ('48920', 'Periko de los palotes Kalea', 'Bizkaia', 'Portugalete')
        ),
        telefono_typ('600111222'),
        DATE '2022-03-01', 2100.50,
        'Informatika'
    )
);

INSERT INTO administratzaileak VALUES (
    administrari_typ(
        '22222222B', 'Hugo', 'Merino',
        helbide_taula_typ(
            helbide_typ('48980', 'Kale nagusia 1', 'Bizkaia', 'Santurtzi')
        ),
        telefono_typ('600333444'),
        DATE '2023-10-10', 2100.50,
        'ADE'
    )
);


INSERT INTO teknikariak VALUES (
    teknikari_typ(
        '33333333B', 'Endika', 'Garcia',
        helbide_taula_typ(
            helbide_typ('48920', 'Kale Txikia 5', 'Bizkaia', 'Portugalete')
        ),
        telefono_typ('611222333', '944555666'),
        DATE '2023-06-15', 1850.00,
        'Sare-mantentzea'
    )
);

INSERT INTO teknikariak VALUES (
    teknikari_typ(
        '44444444C', 'Jonathan', 'Fernandez',
        helbide_taula_typ(
            helbide_typ('48910', 'Kale Txikia 6', 'Bizkaia', 'Sestao')
        ),
        telefono_typ('944555666'),
        DATE '2023-08-05', 1850.00,
        'Sare-mantentzea'
    )
);

SET SERVEROUTPUT ON;

BEGIN
   
   FOR a IN (SELECT VALUE(p) AS emp FROM administratzaileak p) LOOP
      DBMS_OUTPUT.PUT_LINE('NAN: ' || a.emp.nan);
      DBMS_OUTPUT.PUT_LINE('Izena: ' || a.emp.izena || ' ' || a.emp.abizena);
      DBMS_OUTPUT.PUT_LINE('Soldata Oinarria: ' || a.emp.soldataoinarria);
      DBMS_OUTPUT.PUT_LINE('Soldata Totala: ' || a.emp.soldata_totala());

    
      DBMS_OUTPUT.PUT_LINE('Helbideak:');
      FOR h IN (SELECT * FROM TABLE(a.emp.helbidea)) LOOP
         DBMS_OUTPUT.PUT_LINE('   ' || h.helbidea || ' ' || h.herria);
      END LOOP;

  
      DBMS_OUTPUT.PUT_LINE('Telefonoak:');
      FOR t IN (SELECT COLUMN_VALUE AS tel FROM TABLE(a.emp.telefonoak)) LOOP
         DBMS_OUTPUT.PUT_LINE('   ' || t.tel);
      END LOOP;

      DBMS_OUTPUT.PUT_LINE('-----------------------------------');
   END LOOP;
END;
/


CREATE OR REPLACE TYPE BODY langile_typ AS
    MEMBER FUNCTION soldata_totala RETURN NUMBER IS
        v_urteak  NUMBER;
        v_totala  NUMBER(10,2); 
    BEGIN
        v_urteak := TRUNC(MONTHS_BETWEEN(SYSDATE, SELF.kontratatzedata) / 12);
        v_totala := ROUND(SELF.soldataoinarria * POWER(1.02, v_urteak), 2);
        RETURN v_totala;         
    END soldata_totala;
END;
/












