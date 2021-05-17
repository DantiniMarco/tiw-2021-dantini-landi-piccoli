	(function() { // avoid variables ending up in the global scope

	  // page components
	  var pageOrchestrator = new PageOrchestrator(); // main controller

	  window.addEventListener("load", () => {
		  pageOrchestrator.start(); // initialize the components
		  pageOrchestrator.refresh();
	  }, false);


	  // Constructors of view components

	  function PersonalMessage(_alert, _messagecontainer) {
		  this.alert = _alert;
	  	  this.userdata = null;
	  	  this.messagecontainer = _messagecontainer;
		  var self = this;
		  makeCall("GET", "GetUserData", null,
			  function(req) {
				  if (req.readyState == 4) {
					  var message = req.responseText;
					  if (req.status == 200) {
						  self.userdata = JSON.parse(req.responseText);
						  console.log(self.userdata);
						  self.messagecontainer.textContent = self.userdata.username;
					  }
				  } else {
					  self.alert.textContent = message;
				  }
			  }
		  );
	  }

	  function PageOrchestrator() {
	    var alertContainer = document.getElementById("id_alert");
	    this.start = function() {
	      personalMessage = new PersonalMessage(alertContainer, document.getElementById("id_username"));
	      //personalMessage.show();

	      document.querySelector("a[href='Logout']").addEventListener('click', () => {
	        window.sessionStorage.removeItem('username');
	      })
	    };


	    this.refresh = function() {
	      alertContainer.textContent = "";
	    };
	  }
	})();
