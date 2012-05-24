# --- Sample dataset

# --- !Ups

insert into budget(id, name) values (1, 'Unbudgeted');
insert into budget(id, name) values (2, 'BAU');
insert into budget(id, name) values (3, 'Expansion');

insert into currency(id, abbreviation) values (1, 'USD');
insert into currency(id, abbreviation) values (2, 'EUR');
insert into currency(id, abbreviation) values (3, 'GBP');
insert into currency(id, abbreviation) values (4, 'JPY');
insert into currency(id, abbreviation) values (5, 'DKK');
insert into currency(id, abbreviation) values (5, 'AUD');

insert into location(id, code, description) values (1, 'NA', 'None'); 
insert into location(id, code, description, address) values (2, 'WDC1', 'DC office', '529 14th Street NW, Washington, DC 2xsxx');
insert into location(id, code, description) values (3, 'WDC2', 'Dept. of Labor Lockup');
insert into location(id, code, description, address) values (4, 'WDC3', 'Dept. of Commerce Lockup', '200 14th Street NW, Washtingon, DC');
insert into location(id, code, description, address) values (5, 'WDC8', 'Our colo', '14 L Street NE, Washington, DC');

insert into person(id, name, email, telephone, company_id) values(1, 'Abbot', 'abbot@funny', '4555', 1);
insert into person(id, name, email, telephone, company_id) values(2, 'Costello', 'me@nowhere', Null, 1);
insert into person(id, name, email, telephone, company_id) values(3, 'John', 'me@nowhere', Null, 1);

insert into company(id, name, primary_contact_id) values(1, 'Acme', 1);
insert into company(id, name, primary_contact_id) values(2, 'Cisco', 3);

insert into contract_type(id, name) values(1, "Line");
insert into contract_type(id, name) values(2, "Colo");
insert into contract_type(id, name) values(3, "Support");

insert into contract(id, vendor_contract_id, billing_account, extra_info, description, mrc, nrc, currency_id, 
	a_end_id, z_end_id, start_date, term, term_units,
	cancellation_period, cancellation_period_units,
	last_modifying_user, last_modified_time, vendor_id, contract_type_id, budget_id)
	values (1, '138209f8', '34803', 'Circuit 4572', 'Line between Sydney and Singapore', 
		1000, 1000, 1, 2, 3, '2012-03-01', 1, 1, 90, 0, 'wrk', '2012-03-20', 1, 1, 2);

insert into contract(id, vendor_contract_id, extra_info, description, mrc, nrc, currency_id, 
	a_end_id, z_end_id, start_date, term, term_units,
	cancellation_period, cancellation_period_units,
	last_modifying_user, last_modified_time, vendor_id, contract_type_id, attention, budget_id)
	values (2, '4980238', '', '.This has a far warning. Some description of contract 2', 
		7500, 33.40, 2, 2, 1, '2012-01-01', 4, 1, 1, 0, 'wrk', '2012-03-20', 1, 1, "Verify contract", 3);

insert into contract(id, vendor_contract_id, billing_account, extra_info, description, mrc, nrc, currency_id, 
	a_end_id, z_end_id, start_date, term, term_units,
	cancellation_period, cancellation_period_units,
	last_modifying_user, last_modified_time, vendor_id, contract_type_id, budget_id)
	values (3, '1146378', '370999', 'Line 45x9', 'This has a near warning', 
		11000, 2333, 3, 2, 4, '2012-01-01', 4, 1, 10, 0, 'wrk', '2012-03-20', 1, 1, 1);

insert into contract(id, vendor_contract_id, billing_account, extra_info, description, mrc, nrc, currency_id, 
	a_end_id, z_end_id, start_date, term, term_units,
	cancellation_period, cancellation_period_units,
	last_modifying_user, last_modified_time, vendor_id, contract_type_id, budget_id)
	values (4, 'RT87309', 'N21028', '', 'Line between Chicago an3 LA', 
		33000, 2500, 4, 2, 1, '2012-01-01', 1, 2, 30, 0, 'wrk', '2012-03-20', 1, 1, 2);

insert into contract(id, vendor_contract_id, billing_account, extra_info, description, mrc, nrc, currency_id, 
	a_end_id, z_end_id, start_date, term, term_units,
	cancellation_period, cancellation_period_units, cancelled_date,
	last_modifying_user, last_modified_time, vendor_id, contract_type_id, budget_id)
	values (5, '182098', '1280398302', 'Line 1038', 'From Chicago to North Pole. Cancelled, replaced by Acme RT87309', 
		150, 85, 5, 2, 1, '2010-01-01', 1, 2, 30, 0, '2010-11-15', 'wrk', '2012-03-20', 1, 1, 1);

insert into contract(id, vendor_contract_id, extra_info, description, mrc, nrc, currency_id, 
	a_end_id, z_end_id, start_date, term, term_units,
	cancellation_period, cancellation_period_units,
	last_modifying_user, last_modified_time, vendor_id, contract_type_id, attention, budget_id)
	values (6, '372098', 'Cisco Support Contract', 'Support contract on the XXX cisco device', 
		2000, 0, 2, 2, 1, '2012-01-01', 1, 2, 30, 0, 'wrk', '2012-04-12', 2, 3, "Transfer to billing", 2);

insert into reminder(id, reminder_date, contract_id) values (1, '2012-01-01', 1);

insert into reminder_person(reminder_id, person_id) values (1, 1);
insert into reminder_person(reminder_id, person_id) values (1, 2);

# --- !Downs

delete from budget;
delete from currency;
delete from location;
delete from reminder_person;
delete from reminder;
delete from person;
delete from company;
delete from contract_type;
delete from contract;
