Current status: Everything around contracts works, now I need to get contracts themselves working

To do:

* If a contract has passed cancellation, automatically add 365 to it (essentially, auto-renew it)
* Possibly have 2 view of front page, one general, one with contract details. The other could have circuit id, contract id, cable id
* Be able to filter on all contracts related to an MSA.
* Search function
* In filter form, get filtering by status working
* Add filtering by sent status and contract name to reminders.
* Mark required fields consistently, and use a star or some color
* Finish contract filtering. On form submission, grab data, generate where clause and display. How to persist it? Cookie?
* In reminder form and filter form, get unapply (obj to map) working properly for mutiselect
* Get daily alerts working.
* Add paging to contracts table
* Handle text area too big on descriptions. Better form validation.
* Date picker?
* Consider using either Int or BigDecimal for the mrc and nrc. If there is never a fractional part, just use Int.
* Why doesn't diagonal pattern for too-late work in Firefox? Why won't it work without webkit in chrome on my site, but works ok on example site?
* Make sure quotes and other sensitive characters are escaped on input fields
* Make new company popup on people page look better
* 2 reminders - one the day before the cancellation date, and the other 30 days prior to that.
* In contract, make member data a Company, not a CompanyId
* On contract view, make link back to list retain filtering
