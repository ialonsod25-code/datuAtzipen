-- EUSKANP_ERRONK_scripta (zuzendua)
-- Aldaketak: "/" bakoitzaren amaieran, soldata_totala() espezifikazioan
-- eta BODY-a datuak irakurri baino lehen sortzen da.
SET SERVEROUTPUT ON;

-- 0. Garbiketa (berriro exekutatu ahal izateko)
BEGIN
  FOR t IN (SELECT table_name FROM user_tables WHERE table_name IN ('ADMINISTRATZAILEAK','TEKNIKARIAK')) LOOP
    EXECUTE IMMEDIATE 'DROP TABLE ' || t.table_name || ' PURGE';
  END LOOP;
  FOR ty IN (SELECT type_name FROM user_types WHERE type_name IN ('ADMINISTRARI_TYP','TEKNIKARI_TYP','LANGILE_TYP','TELEFONO_TYP','HELBIDE_TAULA_TYP','HELBIDE_TYP') ORDER BY CASE type_name WHEN 'ADMINISTRARI_TYP' THEN 1 WHEN 'TEKNIKARI_TYP' THEN 2 WHEN 'LANGILE_TYP' THEN 3 WHEN 'TELEFONO_TYP' THEN 4 WHEN 'HELBIDE_TAULA_TYP' THEN 5 ELSE 6 END) LOOP
    EXECUTE IMMEDIATE 'DROP TYPE ' || ty.type_name || ' FORCE';
  END LOOP;
END;
/

-- 1. Motak
CREATE TYPE helbide_typ AS OBJECT(
    KODEPOSTALA VARCHAR2(5),
    HELBIDEA    VARCHAR2(45),
    PROBINTZIA  VARCHAR2(25),
    HERRIA      VARCHAR2(25)
);
/

CREATE TYPE helbide_taula_typ AS TABLE OF helbide_typ;
/

CREATE TYPE telefono_typ AS TABLE OF VARCHAR2(15);
/

CREATE TYPE langile_typ AS OBJECT(
    NAN             VARCHAR2(9),
    IZENA           VARCHAR2(25),
    ABIZENA         VARCHAR2(25),
    HELBIDEA        helbide_taula_typ,
    TELEFONOAK      telefono_typ,
    KONTRATATZEDATA DATE,
    SOLDATAOINARRIA NUMBER(8,2),
    MEMBER FUNCTION soldata_totala RETURN NUMBER
) NOT FINAL;
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

CREATE TYPE administrari_typ UNDER langile_typ (
    IKASKETAK VARCHAR2(50)
);
/

CREATE TYPE teknikari_typ UNDER langile_typ (
    EGINKIZUNA VARCHAR2(30)
);
/

-- 2. Taulak
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

-- 3. Datuak
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

COMMIT;

-- 4. Proba
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