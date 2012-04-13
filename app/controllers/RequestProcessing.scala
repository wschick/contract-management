package controllers

object RequestProcessing {

	/** Use this to map forms with multiselects into the proper key-value pairs. 
		A multiselect needs keys of the form MyKey[0] instead of just MyKey. One
		way to detect a multiselect key is to check for the presence of more than one 
		key. However, this doesn't work when we have only a single value.
		That comes is as List(value), but so do non-multiselect keys. To solve 
		this for the time moment, you have to end multiselect keys (form field names)
		with an underscore. That way this can alwasy tell what is a multiselect.
		*/

	def translateToPlayInput(inputMap:Map[String, Seq[String]])= { 
			inputMap.flatMap ({ 
				case (key, value) if (value.length > 1 || key.takeRight(1) == "_") => { 
					value.zipWithIndex.map { 
						case (value, index) => (key +"[" + index+"]", value)
				}}
				case (key, value) if value.length == 1 => {
					Map(key->value.head)
				} 
		})
	}

}
