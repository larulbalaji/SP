--
-- Script to create data in db
--

-- Note that we do not specify a COLLATE - this will default to utf8_general_ci,
-- which causes queries to be case-insensitive.


DROP TABLE IF EXISTS  container_permission_assignments;
DROP TABLE IF EXISTS  container_priv_data_assignments;
DROP TABLE IF EXISTS  containers;
DROP TABLE IF EXISTS  priv_data_permission_assignments;
DROP TABLE IF EXISTS  privileged_data;
DROP TABLE IF EXISTS  user_group_assignments;
DROP TABLE IF EXISTS  groups;
DROP TABLE IF EXISTS  users;



CREATE TABLE
    container_permission_assignments
    (
        id INT NOT NULL AUTO_INCREMENT,
        container_id VARCHAR(128) NOT NULL,
        user_id VARCHAR(128),
        group_id VARCHAR(128),
        rights VARCHAR(256),
        PRIMARY KEY (id)
    )
    ;

CREATE INDEX containerpermissionassignments_fk1 ON container_permission_assignments (container_id);
CREATE INDEX containerpermissionassignments_fk2 ON container_permission_assignments (user_id);
CREATE INDEX containerpermissionassignments_fk3 ON container_permission_assignments (group_id);

CREATE TABLE
    container_priv_data_assignments
    (
        container_id VARCHAR(128) NOT NULL,
        privileged_data_id VARCHAR(128) NOT NULL
    )
    ;

CREATE INDEX containerprvidataassignments_fk1 ON container_priv_data_assignments (container_id);
CREATE INDEX containerprvidataassignments_fk2 ON container_priv_data_assignments (privileged_data_id);

CREATE TABLE
    containers
    (
        id VARCHAR(128) NOT NULL,
        name VARCHAR(128) NOT NULL,
        displayname VARCHAR(128),
        description VARCHAR(1024),
        type VARCHAR(128),
        ownerId VARCHAR(128),
        PRIMARY KEY (id),
        CONSTRAINT containers_ix1 UNIQUE (name)
    )
    ;

CREATE TABLE
    groups
    (
        id VARCHAR(128) NOT NULL,
        displayName VARCHAR(128) NOT NULL,
        parent_group_id VARCHAR(128),
        source VARCHAR(128),
        source_native_identifier VARCHAR(450),
        PRIMARY KEY (id)
    )
    ;

CREATE TABLE
    priv_data_permission_assignments
    (
        id INT NOT NULL AUTO_INCREMENT,
        privileged_data_id VARCHAR(128) NOT NULL,
        user_id VARCHAR(128),
        group_id VARCHAR(128),
        rights VARCHAR(256) NOT NULL,
        PRIMARY KEY (id)
    )
    ;

CREATE INDEX privdatapermissionassignments_fk2 ON priv_data_permission_assignments (user_id);
CREATE INDEX privdatapermissionassignments_fk3 ON priv_data_permission_assignments (group_id);
CREATE INDEX privdatapermissionassignments_fk1 ON priv_data_permission_assignments (privileged_data_id);

CREATE TABLE
    privileged_data
    (
        id VARCHAR(128) NOT NULL,
        name VARCHAR(128) NOT NULL,
        description VARCHAR(1024),
        type VARCHAR(128),
        PRIMARY KEY (id)
    )
    ;

CREATE TABLE
    user_group_assignments
    (
        user_id VARCHAR(128),
        group_id VARCHAR(128)
    )
    ;

CREATE INDEX groupassignments_fk1 ON user_group_assignments (user_id);
CREATE INDEX groupassignments_fk2 ON user_group_assignments (group_id);

CREATE TABLE
    users
    (
        id VARCHAR(128) NOT NULL,
        userName VARCHAR(128) NOT NULL,
        formattedName VARCHAR(128),
        familyName VARCHAR(128),
        givenName VARCHAR(128),
        middleName VARCHAR(128),
        honorificPrefix VARCHAR(128),
        honorificSuffix VARCHAR(128),
        displayName VARCHAR(128),
        nickname VARCHAR(128),
        profileURL VARCHAR(128),
        title VARCHAR(128),
        usertype VARCHAR(128),
        preferredLanguage VARCHAR(128),
        locale VARCHAR(128),
        timezone VARCHAR(128),
        active TINYINT,
        password VARCHAR(128),
        email VARCHAR(128),
        source VARCHAR(128),
        source_native_identifier VARCHAR(450),
        PRIMARY KEY (id),
        CONSTRAINT users_ix1 UNIQUE (userName)
    )
    ;

ALTER TABLE container_priv_data_assignments
	ADD CONSTRAINT containerprivdataassignments_fk1
	FOREIGN KEY (container_id)
	REFERENCES containers (id);

ALTER TABLE container_priv_data_assignments
	ADD CONSTRAINT containerprivdataassignments_fk2
	FOREIGN KEY (privileged_data_id)
	REFERENCES privileged_data (id);

ALTER TABLE priv_data_permission_assignments
	ADD CONSTRAINT privdatapermissionassignments_fk1
	FOREIGN KEY (privileged_data_id)
	REFERENCES privileged_data (id);

ALTER TABLE priv_data_permission_assignments
	ADD CONSTRAINT privdatapermissionassignments_fk2
	FOREIGN KEY (user_id)
	REFERENCES users (id);

ALTER TABLE priv_data_permission_assignments
	ADD CONSTRAINT privdatapermissionassignments_fk3
	FOREIGN KEY (group_id)
	REFERENCES groups (id);

ALTER TABLE user_group_assignments
	ADD CONSTRAINT groupassignments_fk1
	FOREIGN KEY (user_id)
	REFERENCES users (id);

ALTER TABLE user_group_assignments
	ADD CONSTRAINT groupassignments_fk2
	FOREIGN KEY (group_id)
    REFERENCES groups (id);


    -- Add entries to users table
    insert into users (id,userName,formattedName,familyName,givenName,middleName,honorificPrefix,honorificSuffix,displayName,nickname,profileURL,title,usertype,preferredLanguage,locale,timezone,active,password,email,source,source_native_identifier)
    values
    ('1b2c3d','Catherine.Simmons','Catherine Simmons','Simmons','Catherine','','','','Catherine Simmons','Catherine Simmons','','Cathy','employee','English','','EST',1,'xyzzy','Catherine.Simmons@sailpointdemo.com','',''),
    ('1b','Jerry.Bennett','Jerry Bennett','Bennett','Jerry','','','','Jerry Bennett','Jerry Bennett','','Jerry','employee','English','','EST',1,'xyzzy','Jerry.Bennett@sailpointdemo.com','',''),
    ('040d647a365c4b1f8e917fb2071ca8c3','Mike.Chapman','Mike Chapman','Chapman','Mike',NULL,NULL,NULL,'Mike Chapman',NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'Mike.Chapman@sailpointdemo.com',NULL,NULL),
    ('0bc190a506474928bd7009d38bb0d487','Kelly.Boyd','Kelly.Boyd','Boyd','Kelly',NULL,NULL,NULL,'Kelly Boyd',NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'kelly.boyd@sailpointdemo.com',NULL,NULL),
    ('10d13ce41df64afbaf6598a160478334','Rose.Hunter','Rose Hunter','Hunter','Rose',NULL,NULL,NULL,'Rose Hunter',NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'Rose.Hunter@sailpointdemo.com',NULL,NULL),
    ('40b320e8ac274f84a9e99a9c847dfb9a','Frances.Gonzales','Frances.Gonzales','Gonzales','Frances',NULL,NULL,NULL,'Frances Gonzales',NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'Frances.Gonzales@sailpointdemo.com',NULL,NULL),
    ('4bafaa5c5fd243bca302699c5bc47403','Leonard.Williamson','Leonard.Williamson','Williamson','Leonard',NULL,NULL,NULL,'Leonard Williamson',NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'Leanard.Williamson@sailpointdemo.com',NULL,NULL),
    ('64d4dc0c9fd645f08ce88accd2295821','Albert.Woods','Albert Woods','Woods','Albert',NULL,NULL,NULL,'Albert Woods',NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'Albert.Woods@sailpointdemo.com',NULL,NULL),
    ('76d49576947747cc98a6b22ed928bc8f','Fred.Reyes','Fred.Reyes','Reyes','Fred',NULL,NULL,NULL,'Fred Reyes',NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'fred.reyes@sailpointdemo.com',NULL,NULL),
    ('87e376a7bc4d401a808313f26d109f8a','Barbara.Wilson','Barbara Wilson','Wilson','Barbara',NULL,NULL,NULL,'Barbara Wilson',NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'Barbara.Wilson@sailpointdemo.com',NULL,NULL),
    ('a39038f5bf344d82b4ebf17c5d91fc47','Willie.Gomez','Willie Gomez','Gomez','Willie',NULL,NULL,NULL,'Willie Gomez',NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'Willie.Gomez@sailpointdemo.com',NULL,NULL),
    ('d5ff9a2829b94ef7844770aaeffce9b9','Carol.Adams','Carol.Adams','Adams','Carol',NULL,NULL,NULL,'Carol Adams',NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'carol.adams@sailpointdemo.com',NULL,NULL),
    ('dd0a4fc099b0438baf2be410ba5dd188','Angela.Bell','Angela.Bell','Bell','Angela',NULL,NULL,NULL,'Angela Bell',NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'angela.bell@sailpointdemo.com',NULL,NULL),
    ('e3a4e9f76a6d4ed98331b6837b7b99f5','Peter.Powell','Peter.Powell','Powell','Peter',NULL,NULL,NULL,'Peter Powell',NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'peter.powell@sailpointdemo.com',NULL,NULL),
    ('e983d45de8644b1c96256a7e0a6204f6','Deborah.Collins','Deborah.Collins','Collins','Deborah',NULL,NULL,NULL,'Deborah Collins',NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'Deb.Collins@sailpointdemo.com',NULL,NULL),
    ('f9a52e8edb114680800872be81c740f1','Aaron.Nichols','Aaron.Nichols','Nichols','Aaron',NULL,NULL,NULL,'Aaron Nichols',NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,'aaron.nichols@sailpointdemo.com',NULL,NULL)
    ;

    -- Add entries to groups table
    insert into groups (id,displayName,parent_group_id,source,source_native_identifier)
    values
    ('g1a','IdentityIQ Admins','','',''),
    ('g2','Windows Admins',NULL,NULL,NULL),
    ('g3','Basic PAM Access',NULL,NULL,NULL),
    ('g4','Unix Admins',NULL,NULL,NULL),
    ('g5','PAM Admins',NULL,NULL,NULL);
    ;

    -- Add entries to containers table
    insert into containers (id,name,displayName,description,type,ownerid)
    values
    ('c1','CatherineSimmonsAdminAccount','Catherine Simmons Admin Account','Catherine Simmons Admin Account','accountStore','1b2c3d'),
    ('c2','IdentityIQConnectorAccounts','IdentityIQ Connector Accounts','IdentityIQ Connector Accounts','accountStore','1b'),
    ('c3','WindowsAccounts','Windows Admin Accounts','Admin accounts for Windows servers','accountStore','1b'),
    ('c4','UnixAccounts','Unix Admin Accounts','root accounts for Unix Servers','accountStore','1b'),
    ('c5','EmergencyAccess','Emergency Access','Firefighting account cross servers','accountStore','1b')

    ;

    -- Add entries to privileged_data table
    insert into privileged_data (id,name,description,type)
    values
    ('pa1a','SERI\csimmons-adm','Windows Admin Credential','credential'),
    ('pa2a','SERI\Administrator','Windows Admin Credential','credential'),
    ('pa2b','cn=Directory Manager','Sun One Directory','credential'),
    ('pa2c','orangehrm','Human Resources','credential'),
    ('pa2d','sa','MSSQL Server','credential'),
    ('pa2e','root','PRISM','credential'),
    ('pa2f','root','TRAAK','credential'),
    ('pa3a','SERI-TST\Administrator','Windows Admin Credential','credential'),
    ('pa4a','root@UX12adm04','UX12adm04','credential'),
    ('pa4b','root@UX12brs01','UX12brs01','credential'),
    ('pa4c','root@UX11brs02','UX11brs02','credential'),
    ('pa4d','root@UX11lnd03','UX11lnd03','credential'),
    ('pb1a','firefight','Fire Fight account','credential')
    ;

    -- Add entries to user_group_assignments table
    insert into user_group_assignments (user_id,group_id)
    values
    ('1b','g1a'),
    ('e3a4e9f76a6d4ed98331b6837b7b99f5','g1a'),
    ('e3a4e9f76a6d4ed98331b6837b7b99f5','g2'),
    ('e3a4e9f76a6d4ed98331b6837b7b99f5','g3'),
    ('0bc190a506474928bd7009d38bb0d487','g3'),
    ('4bafaa5c5fd243bca302699c5bc47403','g3'),
    ('40b320e8ac274f84a9e99a9c847dfb9a','g3'),
    ('76d49576947747cc98a6b22ed928bc8f','g3'),
    ('e983d45de8644b1c96256a7e0a6204f6','g3'),
    ('d5ff9a2829b94ef7844770aaeffce9b9','g3'),
    ('dd0a4fc099b0438baf2be410ba5dd188','g3'),
    ('f9a52e8edb114680800872be81c740f1','g3'),
    ('64d4dc0c9fd645f08ce88accd2295821','g2'),
    ('10d13ce41df64afbaf6598a160478334','g4'),
    ('a39038f5bf344d82b4ebf17c5d91fc47','g2'),
    ('87e376a7bc4d401a808313f26d109f8a','g4'),
    ('040d647a365c4b1f8e917fb2071ca8c3','g2')
    ;

    -- Add entries to container_permission_assignments table
    insert into container_permission_assignments (container_id,user_id,group_id,rights)
    values
    ('c1','1b2c3d',NULL,'Create object, Delete object, Update object, Rename object, Unlock object, Manage Owners, Change password, Use password, View password, View, View permissions'),
    ('c2',NULL,'g1a','Create object, Delete object, Update object, Rename object, Unlock object, Manage Owners, Change password, Use password, View password, View, View permissions'),
    ('c2',NULL,'g3','View'),
    ('c3',NULL,'g2','Create object, Delete object, Update object, Rename object, Unlock object, Manage Owners, Change password, Use password, View password, View, View permissions'),
    ('c4',NULL,'g4','Create object, Delete object, Update object, Rename object, Unlock object, Manage Owners, Change password, Use password, View password, View, View permissions'),
    ('c5','1b','',' Use password, View password'),
    ('c5','','g5',' Use password, View password'),
    ('c3',NULL,'g3','View'),
    ('c3','10d13ce41df64afbaf6598a160478334','','View, View permissions'),
    ('c3','76d49576947747cc98a6b22ed928bc8f','','View, View permissions')
    ;

    -- Add entries to container_priv_data_assignments table
    insert into container_priv_data_assignments (container_id,privileged_data_id)
    values
    ('c1','pa1a'),
    ('c2','pa2a'),
    ('c2','pa2b'),
    ('c2','pa2c'),
    ('c2','pa2d'),
    ('c2','pa2e'),
    ('c2','pa2f'),
    ('c4','pa2e'),
    ('c4','pa2f'),
    ('c4','pa4a'),
    ('c4','pa4b'),
    ('c4','pa4c'),
    ('c4','pa4d'),
    ('c5','pb1a'),
    ('c3','pa2a'),
    ('c3','pa3a')
    ;