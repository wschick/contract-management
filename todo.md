Current status: Everything is functional, but there are still features to add.

To do:

* Is the contract name field useless now?

Filtering
=========
* In filter form, get filtering by status working
* Be able to filter on all contracts related to an MSA.
* Search function. What do we need to search on?
* Add filtering by sent status and contract name to reminders.
* Persist the filter information.

Views
=====
* Mark required fields consistently, and use a star or some color
* Possibly have 2 view of front page, one general, one with contract details. The other could have circuit id, contract id, cable id
* Add paging to contracts table
* Make new company popup on people page look better
* On contract view, make link back to list retain filtering

Forms
=====
* Make sure quotes and other sensitive characters are escaped on input fields
* Handle text area too big on descriptions. Better form validation.
* Date picker?

Emails
======
* Get daily alerts working.
* 2 reminders - one the day before the cancellation date, and the other 30 days prior to that.

Internals
=========
* Get login working.
* Consider using either Int or BigDecimal for the mrc and nrc. If there is never a fractional part, just use Int.
* In contract, make member data a Company, not a CompanyId
* Make a print stylesheet
