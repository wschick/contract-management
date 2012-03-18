# --- Sample dataset

# --- !Ups

insert into currency(id, label) values (1, 'USD');
insert into currency(id, label) values (2, 'GBP');

insert into location(id, code, description) values (1, ' NA', 'None');
insert into location(id, code, description) values (2, 'WDC1', 'DC office');
insert into location(id, code, description) values (3, 'WDC2', 'Dept. of Labor Lockup');
insert into location(id, code, description) values (4, 'WDC3', 'Dept. of Commerce Lockup');
insert into location(id, code, description) values (5, 'WDC6', 'CoreSite colo');


insert into contract(id, contract_id, name, description, mrc, nrc, currency_id, a_end_id, z_end_id) 
values (1, 'Tel1', 'Telesta Singapore', 'Line between Sydney and Singapore', 11000, 33.40, 1, 2, 1);

# --- !Downs

delete from contract;
delete from currency;
delete from location;
