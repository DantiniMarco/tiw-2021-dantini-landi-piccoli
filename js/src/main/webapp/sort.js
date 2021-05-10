// TABLE SORTING MANAGEMENT FUNCTIONS

/*
 * Self invoking unnamed function. This generates a scope around the code which
 * causes variables and functions not to end up in the global scope.
 */

(function() {

  // Returns the text content of a cell.
  function getCellValue(tr, idx) {
    return tr.children[idx].textContent; // idx indexes the columns of the tr
    // row
  }

  /*
	 * Creates a function that compares two rows based on the cell in the idx
	 * position.
	 */
  function createComparer(idx, asc) {
    return function(a, b) {
      // get values to compare at column idx
      // if order is ascending, compare 1st row to 2nd , otherwise 2nd to 1st
      var v1 = getCellValue(asc ? a : b, idx),
        v2 = getCellValue(asc ? b : a, idx);
      // If non numeric value
      if (v1 === '' || v2 === '' || isNaN(v1) || isNaN(v2)) {
        return v1.toString().localeCompare(v2); // lexical comparison
      }
      // If numeric value
      return v1 - v2; // v1 greater than v2 --> true
    };
  }

  // For all table headers f class sortable
  document.querySelectorAll('th.sortable').forEach(function(th) {
    // Add a listener on the click event
    th.addEventListener('click', function () {
      var table = th.closest('table'); // get the closest table tag
      // For every row in the table body
      // Use Array.from to build an array from table.querySelectorAll result
      // which is an Array Like Object (see DOM specifications)
      Array.from(table.querySelectorAll('tbody > tr'))
        // Toggle the criterion and to sort rows with the comparator function
        // passing
        // (index of column to compare, sort criterion asc or desc) --this is
        // the the
        // element
        .sort(createComparer(Array.from(th.parentNode.children).indexOf(th), this.asc = !this.asc))
        // Append the sorted rows in the table body
        .forEach(function(tr) {
          table.querySelector('tbody').appendChild(tr)
        });
    });
  });
})(); // evaluate the function after its definition
