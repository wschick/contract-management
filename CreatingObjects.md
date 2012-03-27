# Creating an Editable Object

This project contains several different examples of how to create editable objects.

* The Location is a simple 2-field object. The form takes a tuple of individual fields as parameter (e.g. Form[(String, String)].
* The ContractType is a simple 1-field object. Its form takes a ContractType, rather than a tuple of fields (e.g. Form[ContractType])

## The Model

In the model, create a file that defines the case class. If, as is typical, you have an
integer id, then make it of type Pk[Long] and give it a default value of NotAssigned.

	case class MyClass (
		id: Pk[Long] = NotAssigned,
		... more members ...
	)

You can add methods to this class if you wish.

### Companion Object

In the case class' companion object, create the following:

#### An object to parse database results

	val myClass = {
		get[Pk[Long]]("id") ~
		get[<type>]("name of variable") map {
			case id~<name of variable> => MyClass(id, <name of variable>)

### Methods to return information

* __all__ : provide a list of all the objects

		def all(): List[MyClass] = DB.withConnection { implicit connection =>
			SQL("select * from my_class_table order by something").as(myClass *)
	
	This uses the database parser you created earlier.

* __create__ : create a new object

	This gets passed an object, and it stores it in the database

		def create(myObj: MyClass) = {
			DB.withConnection { implicit connection =>
				SQL(
					"""
						insert into my_class_table (column1, column2) values ({column1val}, {column2val})
				"""
				).on(
					'column1val -> myObj.field1,
					'column2val -> myObj.field2
				).executeUpdate()
			}
		}

* __update__: update an existing object

	This takes the object id and an object with the data as parameters. The object with the data
	has been created from the form on the page, but it doesn't have the id. We take the id from
	the URL.

		def update(id: Long, myObj: MyClass) = {
			DB.withConnection { implicit connection =>
				SQL(
					"""
						update my_class_table set column1={column1val}, column2={column2val} where id={id}
					"""
					).on(
					'id -> id,
					'column1 -> contractType.field1,
					'column2 -> contractType.field2
				).executeUpdate()
			}
		}

* __delete__ : delete an object

		def delete(id: Long) {
			DB.withConnection { implicit connection =>
				SQL("delete from my_class_table where id = {id}").on(
					'id -> id).executeUpdate()
			}
		}

* __findById(id: Long)__ : look up an instance by id

		def findById(id: Long): Option[MyClass] = {
			DB.withConnection { implicit connection =>
				SQL(select * from my_class_table where id= {id}").on('id -> id).as(MyClass.myClass.singleOpt)
			}
		}

		We use singleOpt to get just a single instance.

You may also want to create methods that return some field of an object that you looked up

	def fieldById(id: Long): Option[FieldType] = {
		findById(id).map(myObj => Some(myObj.name)).getOrElse(None)
	}

If you want to use values of this object in an HTML select (a dropdown in a form), then create
a method that returns a sequence of (String, String) tuples. The first element will be the value
the form returns, typically the id, and the second is some string created from the object.

	def options: Seq[(String, String)] = DB.withConnection { 
		implicit connection => 
		SQL("select * from my_class_table order by column1")
			.as(MyClass.myClass *).map(c => c.id.toString -> (c.field1))
	}

## The Controller

In the controller, you will define a form, and define action methods to be invoked by HTML requests.

The [Play form documention] has information on how to create a form. The easiest case is for a
simple object where the object fields are the same as the database columns.

	val myForm: Form[MyClass] = Form(
		mapping(
			"id" -> ignored(NotAssigned:Pk[Long]),
			"formfield1" -> nonEmptyText,
			"formfield2" -> number
		)
		(MyClass.apply)(MyClass.unapply)
	)

If you have a more complex object, such as one where it takes several form fields to specify
an object, the you need to be more specific about the apply and unapply functions. For instance,
you might have:

	(id, formfield1, formfield2) => i
		MyClass(NotAssigned, new Thingy(formfield1, formfield2)
	)
	((myObj: MyClass => Some((
		myObj.id,
		myObj.thingy.field1value,
		myObj.thingy.field2value
	))

For the actions, you will typically want these:

* __all__ : Show a list

		def all = Action {
			Ok(views.html.my_class.list(MyClass.all()))
		}

	For a simple class, you might want to include the form on the page with the list.

		def all = Action {
			Ok(views.html.my_class.list(MyClass.all(), myForm))
		}

* __create__ : Make a new object

	Handle bad requests, or make the object. This example assumes that you have the form
	on the same page as the list of all objects.

		def create = Action { implicit request =>
			contractTypeForm.bindFromRequest.fold(
				formWithErrors => BadRequest(html.my_class.list(MyClass.all(), formWithErrors)),
				myObj => {
					MyClass.create(myObj)
					Ok(html.my_class.list(MyClass.all(), myForm))
				}
			)
		}

* __edit__: Put up an edit form for an object.

  def edit(id: Long) = Action {
		MyClass.findById(id).map { existingObject =>
			Ok(html.my_class.edit_form(existingObject, MyForm.fill(existingObject)))
		}.getOrElse(NotFound)
	}

* __update__: Update action for an object

	Submitting the edit form results in an update action. Updates come as a post. 
	The ID comes from the URL, but the object data comes from the data
	in the http post. Be prepared to handle bad forms.


		def update(id: Long) = Action { implicit request =>
			myForm.bindFromRequest.fold(
				formWithErrors => {
					MyClass.findById(id).map { 
						existingObj =>
							BadRequest(html.my_class.edit_form( Obj, formWithErrors))
					}.getOrElse(NotFound)
				},
				newObject => {
					MyClass.update(id, newObject)
					Ok(html.my_class.list(MyClass.all(), myForm))
				}
			)
		}

* __delete__ : Delete an exiting object.

	This deletes and existing object, then shows the list. The delete could fail if the object
	isn't there, but you were trying to delete it, anyway.

		def delete(id: Long) = Action {
			MyClass.delete(id)
			Redirect(routes.MyClass.all)
		}

* _-view__ : View the details of an existing object.

	This class is necessary only if you want to have a separate page to show the details
	of an object.

		def view(id: Long) = Action { implicit request =>
			MyClass.findById(id).map { existingObj =>
				Ok(html.my_class.details(existingObj))
			}.getOrElse(NotFound)
		}

## Routes

You need to put entries into the routes to invoke these methods


GET			/my_class							controllers.MyClass.all
GET			/my_class/:id/edit		controllers.MyClass.edit(id: Long)
POST		/my_class/:id/edit		controllers.MyClass.update(id: Long)
POST		/my_class/new					controllers.MyClass.create
GET			/my_class/:id/delete	controllers.ContractTypes.delete(id: Long)

If you have the details page, add

GET			/contract_types/:id/view 		controllers.MyClass.view(id: Long)

## Templates

We will make 3 templates for the simple scenario where the form to create a new object is on the 
same page as the last of the objects. For neatness, make a subdirectory of app/views to hold
the templates. In keeping with our example, the directory would be app/view/my_class. In it 
are 

* __form_html.scala.html We have the form in 2 locations: on the list of all objects, and 
	on the page to edit an object. Let's make a single file with the form so we can use it in
	both locations. It takes a form, prefilled with object information if we are editing an 
	object. We need to pass 2 actions to the form, one for a form submission, and one for cancelling
	the form. We will make the 2nd of these optional, so we won't show the cancel button if it
	would be useless. We also need to pass in some text that is specific to the form's usage, 
	such as the submit button text.

	@(myClassForm: Form[MyClass], formAction: Call, cancelAction: Option[Call], formLegend: String, submitButtonName: String = "Submit") 

		@if(contractTypeForm.hasErrors) {
			<div class="alert-message error">
				<p>Please fix all errors</p>
			</dev>
		}

		@helper.form(action = formAction) { 

		<fieldset>
			<legend>@formLegend</legend>

			... Your inputs go here ...
			@helper.inputText(
				contractTypeForm("formfield1"),
					'_label -> "Label for form field 1", '_showConstraints -> false, 'size -> 20
				)

		</fieldset>

		<div class="actions">
			<input type="submit" class="btn primary" value="@submitButtonName">
			@if(cancelAction != None) {
			<a href="@cancelAction" class="btn">Cancel</a>
			}
		</div>

	If the form is on the page that shows the list of all objects, we won't bother showing the
	cancel button. When we are editing, however, we want to show the cancel button to bring
	the user back to the object list.

* __list.scala.html__ Show a list of all the objects, along with edit and delete links. 
	Put the form to create a new object at the bottom of the page. Don't show the cancel button
	on the form.

		@(myObjs: List[MyClass], myForm: Form[MyClass])

		@main(... parameter for your main template ...) {

			@if(myObjs.isEmpty) {
				<h3>Nothing to show .</h3>
			} else {
			<table class="my-class-types">
				<tr>
					<th>Field 1</th>
					<th>Field 2</th>
					<th><th>
				</tr>
				@myObjs.map { myObj =>
					<tr>
						<td> @myObj.field1</td>
						<td> @myObj.field2</td>
						<td><a class="edit_link" href="@routes.MyClasses.edit(myObj.id.get)">edit</a>
						<a class="delete_link" href="@routes.MyClasses.delete(myObj.id.get)">delete</a></td>
					</tr>
				}
				</table>
			}

  @form_html(myForm, routes.MyClasses.create, formLegend = "Create a new one")

* __edit__ Edit an existing object. This is a short template.


		@(myObj: MyClass, myForm: Form[MyClass])

		@main(... parameters for main templates ...) {

		@form_html(myForm, routes.MyClass.update(myObj.id.get), 
		Some(routes.MyClasses.all), formLegend = "Update " + myObj.name, 
		submitButtonName = "Update")


[Play form documentation]: http://www.playframework.org/documentation/2.0/ScalaForms
