<?php render('_header',array('title'=>$title, 'activePage'=>$activePage))?>


<script>  

	function submit_form(){
		// simulate a submit button
		document.forms["popular_item_form"].submit();
	}

	function selectBrowseMode() {
		var name_element = document.getElementById('controller');
		if (name_element != 'null') { 
			name_element.value = "PopularItemsController";
			submit_form();
		}
	}	
	
	function selectItem(item_id) {
		var name_element = document.getElementById('select_item');
		if (name_element != 'null') {
			name_element.value = item_id;
		
			var name_element = document.getElementById('controller');
			name_element.value = "ItemDetailController";
			submit_form();
		}
	}	
</script>

<form action="popular.php" method="get" name="popular_item_form" id="popular_item_form">
	<!-- name of controller to process this page -->
	<input type="hidden" name="controller" id="controller" value="PopularItemsController">
	<input type="hidden" name="select_item" id="select_item" value="">
	<input type="hidden" name="activePage" id="activePage" value="popularItem">

<!--
	 <div data-role="fieldcontain">
		<fieldset data-role="controlgroup" data-type="horizontal">
			<legend>
				View By:
			</legend>
<?php
if ($browseMode == "Title") {
?>			
			<input name="browseMode" id="radio1" value="Title" type="radio" onclick="selectBrowseMode(); return false" checked="checked" />
<?php
} else {
?>			
			<input name="browseMode" id="radio1" value="Title" type="radio" onclick="selectBrowseMode(); return false" />
<?php
};
?>			
			
			<label for="radio1">
				Title
			</label>
			
<?php
if ($browseMode == "Category") {
?>			
			<input name="browseMode" id="radio2" value="Category" type="radio" onclick="selectBrowseMode(); return false" checked="checked" />
<?php
} else {
?>			
			<input name="browseMode" id="radio2" value="Category" type="radio" onclick="selectBrowseMode(); return false" />
<?php
};
?>	

			<label for="radio2">
				Category
			</label>
		</fieldset>
	</div>

-->
	
	<div data-role="fieldcontain">
		<h3>Choose an item:</h3>	
		
<?php
if ($browseMode == "Category") {
		// display items by category
?>			

		<ul name="itemList" id="itemList" data-role="listview" data-inset="true">

<?php
		$oldCategory = "";
		foreach ($items as $item) {
			if ($item->meaning <> $oldCategory) {
				$oldCategory = $item->meaning;
				echo ('<li data-role="list-divider">' . $item->meaning . '</li>');
			}; 
			
			echo ('<li><a data-icon="arrow-r" data-iconpos="right"  onclick="selectItem(\'' . $item->id . '\'); return false" rel="external">' . $item->title . '</a></li>');							
		};

?>		
					
		</ul>
<?php
} else {
		// display items by date
?>	
		<ul name="itemList" id="itemList" data-role="listview" data-inset="true">
			<?php render($items) ?>
		</ul>
<?php
};
?>		
		
	</div>

</form> 

<?php render('_footer')?>
