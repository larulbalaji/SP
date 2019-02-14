--
--

DROP DATABASE prism;

CREATE DATABASE IF NOT EXISTS prism CHARACTER SET utf8;

GRANT ALL PRIVILEGES ON prism.*
    TO 'root' IDENTIFIED BY 'root';
GRANT ALL PRIVILEGES ON prism.*
    TO 'root'@'%' IDENTIFIED BY 'root';
GRANT ALL PRIVILEGES ON prism.*
    TO 'root'@'localhost' IDENTIFIED BY 'root';

USE prism;

    create table prism.users (
        login varchar(30) not null unique,
        description varchar(1024),
        first varchar(128),
        last varchar(128),
        groups varchar(128),
        password varchar(26),
        status char(1),
	locked char(1),
       	lastLogin DATE, 
        primary key (login)
    ) ENGINE=InnoDB;
	
insert into users (login,first,last,groups,password,status,locked,lastLogin) values ('PRISM ADMIN','','','User,Manager,Super','password','A','N','2012-11-01');
insert into users (login,first,last,groups,password,status,locked,lastLogin) values ('whenderson','Walter','Henderson','User,Manager,Super','password','A','Y','2012-01-01');



    create table prism.groups (
        name varchar(30) not null unique,
        description varchar(1024),
        primary key (name)
    ) ENGINE=InnoDB;
	
insert into groups (name,description) values ('User','This group is used to assign basic user access to PRISM');
insert into groups (name,description) values ('Manager','This group is used to assign manager access to PRISM');
insert into groups (name,description) values ('Super','This is a privileged group with superuser access to PRISM');





