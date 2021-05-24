(function () { // avoid variables ending up in the global scope

    // page components
    var auctionsList, searchForm, pageOrchestrator = new PageOrchestrator(); // main controller

    window.addEventListener("load", () => {
        pageOrchestrator.start(); // initialize the components
        pageOrchestrator.refresh();
    }, false);


    // Constructors of view components

    function AuctionList(_alert, _listcontainer, _listcontainerbody) {
        this.alert = _alert;
        this.listcontainer = _listcontainer;
        this.listcontainerbody = _listcontainerbody;

        this.reset = function () {
            this.listcontainer.style.visibility = "hidden";
        }

        this.show = function (keyword) {
            var self = this;
            makeCall("GET", "GetSearchedAuction?keyword=" + keyword, null,
                function (req) {
                    if (req.readyState == 4) {
                        var message = req.responseText;
                        if (req.status == 200) {
                            console.log(req.responseText);
                            var auctionsToShow = JSON.parse(req.responseText);
                            console.log(auctionsToShow);
                            if (auctionsToShow.length == 0) {
                                self.alert.textContent = "No auctions found!";
                                return;
                            }
                            self.update(auctionsToShow); // self visible by closure
                        }
                    } else {
                        self.alert.textContent = message;
                    }
                }
            );
        };


        this.update = function (arrayAuctions) {
            var elem, i, row, destcell, datecell, linkcell, anchor;
            this.listcontainerbody.innerHTML = ""; // empty the table body
            // build updated list
            var self = this;
            arrayAuctions.forEach(function (auction) { // self visible here, not this
                row = document.createElement("tr");
                idAuctionCell = document.createElement("td");
                idAuctionCell.textContent = auction.idauction;
                row.appendChild(idAuctionCell);
                itemCell = document.createElement("td");
                itemCell.textContent = auction.itemname;
                row.appendChild(itemCell);
                priceCell = document.createElement("td");
                priceCell.textContent = auction.itemname;
                row.appendChild(priceCell);
                raiseCell = document.createElement("td");
                raiseCell.textContent = auction.itemname;
                row.appendChild(raiseCell);
                dateCell = document.createElement("td");
                dateCell.textContent = auction.itemname;
                row.appendChild(dateCell);
                linkcell = document.createElement("td");
                anchor = document.createElement("a");
                linkcell.appendChild(anchor);
                linkText = document.createTextNode("Details");
                anchor.appendChild(linkText);
                //anchor.auctionid = auction.id; // make list item clickable
                anchor.setAttribute('auctionid', auction.id); // set a custom HTML attribute
                anchor.addEventListener("click", (e) => {
                    // dependency via module parameter
                    auctionDetails.show(e.target.getAttribute("auctionid")); // the list must know the details container
                }, false);
                anchor.href = "#";
                row.appendChild(linkcell);
                self.listcontainerbody.appendChild(row);
            });
            this.listcontainer.style.visibility = "visible";

        }

        this.autoclick = function (missionId) {
            var e = new Event("click");
            var selector = "a[auctionid='" + auctionId + "']";
            var anchorToClick =
                (auctionId) ? document.querySelector(selector) : this.listcontainerbody.querySelectorAll("a")[0];
            if (anchorToClick) anchorToClick.dispatchEvent(e);
        }

    }



    function PersonalMessage(_alert, _messagecontainer) {
        this.alert = _alert;
        this.userdata = null;
        this.messagecontainer = _messagecontainer;
        var self = this;
        makeCall("GET", "GetUserData", null,
            function (req) {
                if (req.readyState == 4) {
                    var message = req.responseText;
                    if (req.status == 200) {
                        console.log(req.responseText);
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

    function SearchAuction(formId, alert) {
        // minimum date the user can choose, in this case now and in the future
        var now = new Date(),
            formattedDate = now.toISOString().substring(0, 10);
        this.form = formId;
        this.alert = alert;

        //this.form.querySelector('input[type="date"]').setAttribute("min", formattedDate);

        this.registerEvents = function(orchestrator) {
            // Manage submit button
            this.form.querySelector('button[type="submit"]').addEventListener('click', (e) => {
                e.preventDefault();
                var eventfieldset = e.target.closest("fieldset"),
                    valid = true;
                /*for (i = 0; i < eventfieldset.elements.length; i++) {
                    if (!eventfieldset.elements[i].checkValidity()) {
                        eventfieldset.elements[i].reportValidity();
                        valid = false;
                        break;
                    }
                }*/

                if (valid) {
                    var self = this;
                    console.log(new FormData(e.target.form));
                    auctionsList.show(new FormData(e.target.form).get("keyword"));
                }
            });
        };

        this.reset = function() {
            /*var fieldsets = document.querySelectorAll("#" + this.form.id + " fieldset");
            fieldsets[0].hidden = false;
            fieldsets[1].hidden = true;
            fieldsets[2].hidden = true;*/
            //this.form.style.visibility = "hidden";
        }
    }

    function PageOrchestrator() {
        let alertContainer = document.getElementById("id_alert");
        this.start = function () {
            let personalMessage = new PersonalMessage(alertContainer, document.getElementById("id_username"));
            //personalMessage.show();
            auctionsList = new AuctionList(
                alertContainer,
                document.getElementById("id_listcontainer"),
                document.getElementById("id_listcontainerbody"));

            searchForm = new SearchAuction(document.getElementById("id_searchauctionform"), alertContainer);
            searchForm.registerEvents(this);

            document.querySelector("a[href='Logout']").addEventListener('click', () => {
                window.sessionStorage.removeItem('username');
            })
        };


        this.refresh = function(currentAuction) {
            alertContainer.textContent = "";
            auctionsList.reset();
            //auctionDetails.reset();
            //searchForm.reset();
            //searchForm.show();
        };
    }
})();
