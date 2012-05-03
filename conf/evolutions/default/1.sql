# Tasks schema
 
# --- !Ups

CREATE TABLE budget (
	id integer NOT NULL PRIMARY KEY AUTO_INCREMENT,
	name varchar(20) NOT NULL
);

CREATE TABLE currency (
	id integer NOT NULL PRIMARY KEY AUTO_INCREMENT,
	abbreviation varchar(10) NOT NULL
);

CREATE TABLE location (
	id integer NOT NULL PRIMARY KEY AUTO_INCREMENT,
	code varchar(5) NOT NULL,
	description varchar(100) NOT NULL,
	address varchar(100) 
);

CREATE TABLE contract_type (
	id integer NOT NULL PRIMARY KEY AUTO_INCREMENT,
	name varchar(20) NOT NULL
);


CREATE TABLE person (
	id integer NOT NULL PRIMARY KEY AUTO_INCREMENT,
	name varchar(80) NOT NULL,
	email varchar(80) NOT NULL,
	telephone varchar(20),
	company_id integer NOT NULL
);

CREATE TABLE company (
	id integer NOT NULL PRIMARY KEY AUTO_INCREMENT,
	name varchar(50) NOT NULL,
	primary_contact_id integer 
);


CREATE TABLE reminder (
	id integer NOT NULL KEY AUTO_INCREMENT,
	reminder_date date NOT NULL,
	contract_id	integer NOT NULL,
	sent boolean NOT NULL DEFAULT FALSE
);


CREATE TABLE reminder_person (
	reminder_id integer NOT NULL,
	person_id integer NOT NULL
);


-- For period units, 0 = day, 1 = month, 2 = year
CREATE TABLE contract (
	id integer NOT NULL PRIMARY KEY AUTO_INCREMENT,
	company_id integer NOT NULL,
	vendor_contract_id varchar(30) NOT NULL,
	billing_account varchar(20),
	is_msa boolean NOT NULL default 0,
	msa_id integer,
	name varchar(200) NOT NULL,
	description varchar(255),
	contract_type_id integer NOT NULL,
	a_end_id bigint NOT NULL, 
	z_end_id bigint,
	mrc double NOT NULL,
	nrc double NOT NULL,
	currency_id bigint NOT NULL, 
	budget_id integer NOT NULL,
	start_date date NOT NULL,
	term integer NOT NULL,
	term_units integer NOT NULL, 
	cancellation_period integer NOT NULL,
	cancellation_period_units integer NOT NULL,
	cancelled_date date,
	auto_renew_period integer,
	auto_renew__period_units integer,
	attention varchar(300),
	last_modifying_user varchar(40),
	last_modified_time datetime
);

alter table contract add constraint fk_contract_currency_1 
	foreign key (currency_id) references currency(id) 
	on delete restrict on update restrict;
create index ix_contract_currency_1 on contract(currency_id);

alter table contract add constraint fk_contract_a_end_1 
	foreign key (a_end_id) references location(id) 
	on delete restrict on update restrict;
create index ix_contract_a_end_1 on contract(a_end_id);
				 
alter table contract add constraint fk_contract_z_end_1 
	foreign key (z_end_id) references location(id) 
	on delete restrict on update restrict;
create index ix_contract_z_end_1 on contract(z_end_id);
				 

# --- !Downs
 
/*SET REFERENTIAL_INTEGRITY FALSE;*/

DROP TABLE budget;
DROP TABLE currency;
DROP TABLE location;
DROP TABLE reminder_person;
DROP TABLE reminder;
DROP TABLE person;
DROP TABLE company;
DROP TABLE contract_type;
DROP TABLE contract;

/*SET REFERENTIAL_INTEGRITY TRUE;*/

