CREATE TABLE USER_DELTA(USER_ID VARCHAR(20), ACTION VARCHAR(10));

DROP TRIGGER IF EXISTS TI1;
DROP TRIGGER IF EXISTS TI2;
DROP TRIGGER IF EXISTS TU1;
DROP TRIGGER IF EXISTS TU2;
DROP TRIGGER IF EXISTS TD1;
DROP TRIGGER IF EXISTS TD2;
DROP TRIGGER IF EXISTS LI1;

delimiter |
CREATE TRIGGER TI1
 AFTER INSERT ON hs_hr_employee
 FOR EACH ROW
 BEGIN
   INSERT INTO USER_DELTA (USER_ID, ACTION)
   VALUES (NEW.employee_id, 'Insert');
 END;
|

CREATE TRIGGER TU1
 AFTER UPDATE ON hs_hr_employee
 FOR EACH ROW
 BEGIN
  INSERT INTO USER_DELTA (USER_ID, ACTION)
   VALUES (NEW.employee_id, 'Update');
 IF NEW.termination_id IS NULL AND OLD.termination_id IS NOT NULL
 THEN  
  delete from orangehrm.ohrm_emp_termination where id = OLD.termination_id;
 END IF;
 END;
|
 
CREATE TRIGGER TD1
 AFTER DELETE ON hs_hr_employee
 FOR EACH ROW
 BEGIN
  INSERT INTO USER_DELTA (USER_ID, ACTION)
   VALUES (OLD.employee_id, 'Delete');
 END;
|

CREATE TRIGGER TI2
 AFTER INSERT ON hs_hr_emp_reportto
 FOR EACH ROW
 BEGIN
  INSERT INTO USER_DELTA (USER_ID, ACTION)
   SELECT emp.employee_id, 'Update' from hs_hr_employee as emp
    WHERE emp.emp_number=NEW.erep_sub_emp_number;
 END;
|

CREATE TRIGGER TU2
 AFTER UPDATE ON hs_hr_emp_reportto
 FOR EACH ROW
 BEGIN
  INSERT INTO USER_DELTA (USER_ID, ACTION)
   SELECT emp.employee_id, 'Update' from hs_hr_employee as emp
    WHERE emp.emp_number=NEW.erep_sub_emp_number;
 END;
|

CREATE TRIGGER TD2
 AFTER DELETE ON hs_hr_emp_reportto
 FOR EACH ROW
 BEGIN
  INSERT INTO USER_DELTA (USER_ID, ACTION)
   SELECT emp.employee_id, 'Update' from hs_hr_employee as emp
    WHERE emp.emp_number=OLD.erep_sub_emp_number;
 END;
|

CREATE TRIGGER LI1
 AFTER INSERT ON ohrm_leave
 FOR EACH ROW
 BEGIN
  INSERT INTO USER_DELTA (USER_ID, ACTION)
   SELECT emp.employee_id, 'Update' from hs_hr_employee as emp
   where emp.emp_number=NEW.emp_number
   AND NOT EXISTS (SELECT * FROM user_delta
     where emp.emp_number=NEW.emp_number
     AND action='Update');
 END;
|

delimiter ;
