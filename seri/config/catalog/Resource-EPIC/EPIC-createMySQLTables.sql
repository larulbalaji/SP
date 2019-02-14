create database epic;
create user 'epic' identified by 'epic';
grant all on epic.* to 'epic';

use epic;
CREATE TABLE UserTable (
  UserID int not null auto_increment,
  Name varchar(255),
  SystemLoginID int,
  UserAlias varchar(255),
  StartDate date,
  IsPasswordChangeRequired int DEFAULT 0,
  BlockStatus varchar(255),
  EndDate date,
  DefaultLoginDepartmentID varchar(255),
  AuthenticationConfigurationID varchar(255),
  LinkedProviderID varchar(255),
  Primary Key (UserID)
);
INSERT INTO UserTable (Name,SystemLoginID,UserAlias,StartDate,IsPasswordChangeRequired,BlockStatus,EndDate,DefaultLoginDepartmentID,AuthenticationConfigurationID,LinkedProviderID) values ('Jordan.Sullivan',1,'Jordan.Sullivan',DATE'2014-01-01',0,'Active',DATE'2014-12-31','Nursing','','');
INSERT INTO UserTable (Name,SystemLoginID,UserAlias,StartDate,IsPasswordChangeRequired,BlockStatus,EndDate,DefaultLoginDepartmentID,AuthenticationConfigurationID,LinkedProviderID) values ('Elliot.Reid',2,'Elliot.Reid',DATE'2014-01-01',0,'Active',DATE'2014-12-31','Doctors','','');
INSERT INTO UserTable (Name,SystemLoginID,UserAlias,StartDate,IsPasswordChangeRequired,BlockStatus,EndDate,DefaultLoginDepartmentID,AuthenticationConfigurationID,LinkedProviderID) values ('Christopher.Turk',3,'Christopher.Turk',DATE'2014-01-01',0,'Active',DATE'2014-12-31','Doctors','','');

CREATE TABLE UserLinkedSubtemplateLink (
  UserID int,
  LinkedSubtemplateIDs varchar(255)
);
INSERT INTO UserLinkedSubtemplateLink values (1,'HHC HB RWB FINANCIAL ACCESS - READ ONLY');
INSERT INTO UserLinkedSubtemplateLink values (1,'HHC ER BKR RAD RT');
INSERT INTO UserLinkedSubtemplateLink values (2,'HHC HB RWB FINANCIAL ACCESS - READ ONLY');
INSERT INTO UserLinkedSubtemplateLink values (3,'HHC HB RWB FINANCIAL ACCESS - READ ONLY');


CREATE TABLE LinkedSubtemplateTable (
  LinkedSubtemplateIDs varchar(255) NOT NULL
);
INSERT INTO LinkedSubtemplateTable VALUES ('HHC HB RWB FINANCIAL ACCESS - READ ONLY');
INSERT INTO LinkedSubtemplateTable VALUES ('HHC ER BKR RAD RT');


CREATE TABLE UserLinkedTemplateLink (
  UserID int,
  LinkedTemplateID varchar(255)
);
INSERT INTO UserLinkedTemplateLink values (1,'HHC ER RESP LINK');
INSERT INTO UserLinkedTemplateLink values (2,'HHC ER RESP LINK');
INSERT INTO UserLinkedTemplateLink values (3,'HHC ER RESP LINK');
CREATE TABLE LinkedTemplateTable (
  LinkedTemplateID varchar(255) NOT NULL
);
INSERT INTO LinkedTemplateTable VALUES ('HHC ER RESP LINK');


CREATE TABLE UserRolesLink (
  UserID int,
  UserRoles varchar(255)
);
INSERT INTO UserRolesLink values (1,'SMP MR_IP_PROV_SCHED_DAR');
INSERT INTO UserRolesLink values (1,'SMP_ER_NURSE');
INSERT INTO UserRolesLink values (1,'HAH SMP PATIENT LIST');
INSERT INTO UserRolesLink values (2,'HAH SMP PATIENT LIST');
INSERT INTO UserRolesLink values (3,'HAH SMP PATIENT LIST');




CREATE TABLE UserRolesTable (
  UserRoles varchar(255) NOT NULL
);
INSERT INTO UserRolesTable VALUES ('SMP MR_IP_PROV_SCHED_DAR');
INSERT INTO UserRolesTable VALUES ('SMP_ER_NURSE');
INSERT INTO UserRolesTable VALUES ('HAH SMP PATIENT LIST');
