# Tasks schema
 
# --- !Ups

CREATE TABLE budget (
	id integer NOT NULL PRIMARY KEY AUTO_INCREMENT,
	name varchar(20) NOT NULL
) ENGINE=INNODB;
 
CREATE TABLE currency (
	id integer NOT NULL PRIMARY KEY AUTO_INCREMENT,
	abbreviation varchar(10) NOT NULL
) ENGINE=INNODB;

CREATE TABLE location (
	id integer NOT NULL PRIMARY KEY AUTO_INCREMENT,
	code varchar(5) NOT NULL,
	description varchar(100) NOT NULL,
	address varchar(100) 
) ENGINE=INNODB;

CREATE TABLE contract_type (
	id integer NOT NULL PRIMARY KEY AUTO_INCREMENT,
	name varchar(20) NOT NULL
) ENGINE=INNODB;


CREATE TABLE person (
	id integer NOT NULL PRIMARY KEY AUTO_INCREMENT,
	name varchar(80) NOT NULL,
	email varchar(80) NOT NULL,
	telephone varchar(20),
	company_id integer NOT NULL
) ENGINE=INNODB;

CREATE TABLE company (
	id integer NOT NULL PRIMARY KEY AUTO_INCREMENT,
	name varchar(50) NOT NULL,
	primary_contact_id integer,
	FOREIGN KEY (primary_contact_id) REFERENCES person(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=INNODB;


CREATE TABLE reminder (
	id integer NOT NULL KEY AUTO_INCREMENT,
	reminder_date date NOT NULL,
	contract_id	integer NOT NULL,
	sent boolean NOT NULL DEFAULT FALSE
) ENGINE=INNODB;


CREATE TABLE reminder_person (
	reminder_id integer NOT NULL,
	person_id integer NOT NULL,
	FOREIGN KEY (reminder_id) REFERENCES reminder(id) ON DELETE RESTRICT ON UPDATE CASCADE,
	FOREIGN KEY (person_id) REFERENCES person(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=INNODB;


-- For period units, 0 = day, 1 = month, 2 = year
CREATE TABLE contract (
	id integer NOT NULL PRIMARY KEY AUTO_INCREMENT,
	vendor_id integer NOT NULL,
	vendor_contract_id varchar(30) NOT NULL,
	billing_account varchar(20),
	is_msa boolean NOT NULL default 0,
	msa_id integer,
	extra_info varchar(500),
	description varchar(1000),
	contract_type_id integer NOT NULL,
	a_end_id integer NOT NULL, 
	z_end_id integer,
	mrc double NOT NULL,
	nrc double NOT NULL,
	currency_id integer NOT NULL, 
	budget_id integer NOT NULL,
	start_date date NOT NULL,
	term integer NOT NULL,
	term_units integer NOT NULL, 
	cancellation_period integer NOT NULL,
	cancellation_period_units integer NOT NULL,
	cancelled_date date,
	auto_renew_period integer,
	auto_renew_period_units integer,
	attention varchar(500),
	last_modifying_user varchar(80),
	last_modified_time datetime,
	foreign key (vendor_id) references company(id) on delete restrict on update cascade,
	foreign key (budget_id) references budget(id) on delete restrict on update cascade,
	foreign key (msa_id) references contract(id) on delete restrict on update cascade,
	foreign key (contract_type_id) references contract_type(id) on delete restrict on update cascade,
	foreign key (a_end_id) references location(id) on delete restrict on update cascade,
	foreign key (z_end_id) references location(id) on delete restrict on update cascade,
	foreign key (currency_id) references currency(id) on delete restrict on update cascade
) ENGINE=INNODB;


# --- !Downs
 

DROP TABLE contract;
DROP TABLE budget;
DROP TABLE currency;
DROP TABLE location;
DROP TABLE reminder_person;
DROP TABLE reminder;
DROP TABLE person;
DROP TABLE company;
DROP TABLE contract_type;


