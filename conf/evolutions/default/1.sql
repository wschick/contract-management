# Tasks schema
 
# --- !Ups

CREATE TABLE currency (
	id integer NOT NULL PRIMARY KEY AUTO_INCREMENT,
	label varchar(10) NOT NULL,
);

CREATE TABLE location (
	id integer NOT NULL PRIMARY KEY AUTO_INCREMENT,
	code varchar(5) NOT NULL,
	description varchar(100) NOT NULL,
);

CREATE TABLE contact_information (
	id integer NOT NULL PRIMARY KEY AUTO_INCREMENT,
	name varchar(80) NOT NULL,
	email varchar(80),
	telephone varchar(20),
	company_id integer # Foreign key
);

CREATE TABLE company (
	id integer NOT NULL PRIMARY KEY AUTO_INCREMENT,
	name varchar(50) NOT NULL,
	primary_contact_id integer, # Foreign key
);


CREATE TABLE reminder (
	id integer NOT NULL AUTO_INCREMENT,
	reminder_date date,
	contract_id	integer # Foreign key
);


CREATE TABLE reminder_contract (
	reminder_id integer NOT NULL,
	contact_information_id NOT NULL
);


CREATE TABLE contract (
	id integer NOT NULL AUTO_INCREMENT,
	contract_id varchar(30),
	name varchar(200),
	description varchar(255),
	mrc float,
	nrc float,
	currency_id bigint, # Foreign key
	a_end_id bigint, # Foreign key
	z_end_id bigint, # Foreign key
	start_date date,
	term integer,
	term_units integer,  # 0 = day, 1 = month, 2 = year
	cancellation_period integer,
	cancellation_period_units integer,
	reminder_period integer,
	reminder_period_units integer,
	last_modified_user varchar(40),
	last_modified_time datetime,
	company_id integer # Foreign key

	constraint pk_contract primary key (id)
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
 
SET REFERENTIAL_INTEGRITY FALSE;

DROP TABLE currency;
DROP TABLE location;
DROP TABLE contract;

SET REFERENTIAL_INTEGRITY TRUE;

