INSERT INTO `ohrm_subunit` (name,unit_id,description,lft,rgt,level) VALUES 
 ('Finance','','',2,3,1),
 ('Accounting','','',4,5,1),
 ('Engineering','','',6,7,1),
 ('Executive Management','','',8,9,1),
 ('Human Resources','','',10,11,1),
 ('Information Technology','','',12,13,1),
 ('Inventory','','',14,15,1),
 ('Regional Operations','','',16,17,1)
;
UPDATE ohrm_subunit SET rgt=18 where id=1;