# --- Sample dataset

# --- !Ups

insert into currency(id, abbreviation) values (1, 'USD');
insert into currency(id, abbreviation) values (2, 'GBP');

insert into location(id, code, description) values (1, ' NA', 'None'); 
insert into location(id, code, description) values (2, 'WDC1', 'DC office');
insert into location(id, code, description) values (3, 'WDC2', 'Dept. of Labor Lockup');
insert into location(id, code, description) values (4, 'WDC3', 'Dept. of Commerce Lockup');
insert into location(id, code, description) values (5, 'WDC6', 'CoreSite colo');

insert into person(id, name, email, telephone, company_id) values(1, 'Abbot', 'abbot@funny', '4555', 1);
insert into person(id, name, email, telephone, company_id) values(2, 'Costello', 'me@nowhere', Null, 1);
insert into person(id, name, email, telephone, company_id) values(3, 'John', 'me@nowhere', Null, 1);

insert into company(id, name, primary_contact_id) values(1, 'Acme', 1);

insert into contract_type(id, name) values(1, "Line");
insert into contract_type(id, name) values(2, "Colo");
insert into contract_type(id, name) values(3, "Support");

insert into contract(id, contract_id, name, description, mrc, nrc, currency_id, 
	a_end_id, z_end_id, start_date, term, term_units,
	cancellation_period, cancellation_period_units,
	last_modifying_user, last_modified_time, company_id, contract_type_id)
	values (1, 'Tela1', 'Telesta Singapore line 1', 'Line between Sydney and Singapore', 
		1000, 1000, 1, 2, 1, '2012-03-01', 1, 1, 90, 0, 'wrk', '2012-03-20', 1, 1);

insert into contract(id, contract_id, name, description, mrc, nrc, currency_id, 
	a_end_id, z_end_id, start_date, term, term_units,
	cancellation_period, cancellation_period_units,
	last_modifying_user, last_modified_time, company_id, contract_type_id)
	values (2, 'Tela2', 'Telesta Singapore line 2', 'Far warning', 
		7500, 33.40, 1, 2, 1, '2012-01-01', 4, 1, 1, 0, 'wrk', '2012-03-20', 1, 1);

insert into contract(id, contract_id, name, description, mrc, nrc, currency_id, 
	a_end_id, z_end_id, start_date, term, term_units,
	cancellation_period, cancellation_period_units,
	last_modifying_user, last_modified_time, company_id, contract_type_id)
	values (3, 'Tela3', 'Telesta Singapore line 3', 'near warning', 
		11000, 2333, 1, 2, 1, '2012-01-01', 4, 1, 10, 0, 'wrk', '2012-03-20', 1, 1);

insert into contract(id, contract_id, name, description, mrc, nrc, currency_id, 
	a_end_id, z_end_id, start_date, term, term_units,
	cancellation_period, cancellation_period_units,
	last_modifying_user, last_modified_time, company_id, contract_type_id)
	values (4, 'Tela4', 'Telesta Singapore line 4', 'Line between Sydney and Singapore', 
		33000, 2500, 1, 2, 1, '2012-01-01', 1, 2, 30, 0, 'wrk', '2012-03-20', 1, 1);

insert into contract(id, contract_id, name, description, mrc, nrc, currency_id, 
	a_end_id, z_end_id, start_date, term, term_units,
	cancellation_period, cancellation_period_units, cancelled_date,
	last_modifying_user, last_modified_time, company_id, contract_type_id)
	values (5, 'Cancelled', 'Telesta Singapore line 5', 'cancelled', 
		150, 85, 1, 2, 1, '2010-01-01', 1, 2, 30, 0, '2010-11-15', 'wrk', '2012-03-20', 1, 1);

insert into contract(id, contract_id, name, description, mrc, nrc, currency_id, 
	a_end_id, z_end_id, start_date, term, term_units,
	cancellation_period, cancellation_period_units,
	last_modifying_user, last_modified_time, company_id, contract_type_id)
	values (6, 'Cisco', 'Cisco Support Contract', 'Support contract on the XXX cisco device', 
		2000, 0, 2, 2, 1, '2012-01-01', 1, 2, 30, 0, 'wrk', '2012-04-12', 1, 3);

insert into reminder(id, reminder_date, contract_id) values (1, '2012-01-01', 1);

insert into reminder_person(reminder_id, person_id) values (1, 1);
insert into reminder_person(reminder_id, person_id) values (1, 2);

# --- !Downs

delete from reminder_person;
delete from reminder;
delete from contract;
delete from currency;
delete from location;
delete from person;
delete from company;
