/**
 * Helper functions to JS view
 */

function makeCall(method, url, formElement, cback, reset = true) {
	var req = new XMLHttpRequest(); // visible by closure
	req.onreadystatechange = function() {
		cback(req)
	}; // closure
	req.open(method, url);
	if (formElement == null) {
		req.send();
	} else {
		let formdata = new FormData(formElement);
		if(formdata.get("deadline") != null){
			formdata.set("deadline", new Date(formdata.get("deadline")).toISOString());
		}
		req.send(formdata);
	}
	if (formElement !== null && reset === true) {
		formElement.reset();
	}
}

function dateToIsoString (date) {
	let pad = function(num) {
		var norm = Math.floor(num);
		return (norm < 10 ? '0' : '') + norm;
	};

	return date.getFullYear() +
		'-' + pad(date.getMonth() + 1) +
		'-' + pad(date.getDate()) +
		'T' + pad(date.getHours()) +
		':' + pad(date.getMinutes());
}
