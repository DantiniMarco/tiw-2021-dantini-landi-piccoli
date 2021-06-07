function AuctionListSell(_alert, _listopen, _listopenbody, _listclosed, _listclosedbody, _addauctionform, _username) {
    this.alert = _alert;
    this.listopen = _listopen;
    this.listopenbody = _listopenbody;
    this.listclosed = _listclosed;
    this.listclosedbody = _listclosedbody;
    this.addauctionform = _addauctionform;
    this.username = _username;
    this.addauctionform.querySelector('button[type="submit"]').addEventListener('click', (e) => {
        e.preventDefault();
        let form = e.target.closest("form");
        if (form.checkValidity()) {
            let self = this
            makeCall("POST", 'SellHelperServlet', form,
                function (req) {
                    if (req.readyState === 4) {
                        let message = req.responseText;
                        if (req.status === 200) {
                            //has added auction
                            let userDataStored = JSON.parse(localStorage.getItem("userData"));
                            userDataStored[self.username].lastAction = "sell";
                            localStorage.setItem("userData", JSON.stringify(userDataStored));
                            self.show();
                        } else {
                            self.alert.textContent = message;
                        }
                    }
                }
            );
        } else {
            form.reportValidity();
        }
    });

    this.reset = function () {
        /*this.listcontainer.style.visibility = "hidden";
        this.listcontainer.style.display = "none";*/
    }

    this.show = function () {
        var self = this;
        makeCall("GET", "SellServlet", null,
            function (req) {
                if (req.readyState === 4) {
                    if (req.status === 200) {
                        self.alert.textContent = ""
                        console.log(req.responseText);
                        var auctionsToShow = JSON.parse(req.responseText);
                        console.log(auctionsToShow);
                        if (auctionsToShow.length === 0) {
                            self.alert.textContent = "No auctions found!";
                            return;
                        }
                        self.update(auctionsToShow); // self visible by closure
                    } else {
                        self.searchalert.textContent = req.responseText;
                        self.listcontainer.style.visibility = "hidden";
                        self.listcontainer.style.display = "none";
                    }
                }
            }
        );
    };

    this.setRowAuction = function(auction){
        let row, priceCell, raiseCell, dateCell, itemCell, itemDescCell, linkcell, linkText, anchor;
        row = document.createElement("tr");
        itemCell = document.createElement("td");
        itemCell.textContent = auction.itemName;
        row.appendChild(itemCell);
        itemDescCell = document.createElement("td");
        itemDescCell.textContent = auction.itemDescription;
        row.appendChild(itemDescCell);
        priceCell = document.createElement("td");
        if(auction.price==0){
            priceCell.textContent = "No bids";
        }else{
            priceCell.textContent = new Intl.NumberFormat('it-IT', {
                style: 'currency',
                currency: 'EUR'
            }).format(auction.price);
        }
        row.appendChild(priceCell);
        raiseCell = document.createElement("td");
        raiseCell.textContent = new Intl.NumberFormat('it-IT', {
            style: 'currency',
            currency: 'EUR'
        }).format(auction.minRaise);
        row.appendChild(raiseCell);
        dateCell = document.createElement("td");
        dateCell.textContent = auction.deadline;
        row.appendChild(dateCell);
        linkcell = document.createElement("td");
        anchor = document.createElement("a");
        linkcell.appendChild(anchor);
        linkText = document.createTextNode("Details");
        anchor.appendChild(linkText);
        //anchor.auctionid = auction.id; // make list item clickable
        anchor.setAttribute('auctionid', auction.idAuction); // set a custom HTML attribute
        anchor.addEventListener("click", (e) => {
            // dependency via module parameter
            auctionDetails.show(e.target.getAttribute("auctionid")); // the list must know the details container
        }, false);
        anchor.href = "SellServletHelper";
        row.appendChild(linkcell);
        return row;
    }

    this.update = function (arrayAuctions) {
        this.listopenbody.innerHTML = ""; // empty the table body
        this.listclosedbody.innerHTML = ""; // empty the table body
        // build updated list
        let self = this;
        arrayAuctions.openAuctions.forEach(function (auction) { // self visible here, not this
            self.listopenbody.appendChild(self.setRowAuction(auction));
        });
        arrayAuctions.closedAuctions.forEach(function (auction) { // self visible here, not this
            self.listclosedbody.appendChild(self.setRowAuction(auction));
        });
        //FIXME: can delete, check
        self.listopenbody.style.visibility = "visible";
        self.listopenbody.style.display = null;
        self.listclosedbody.style.visibility = "visible";
        self.listclosedbody.style.display = null;
    }

}


function AuctionDetailsSell(_alert, _auctionData, _openAuctionDetails, _closedAuctionDetails, _closeAuctionForm) {
    this.alert=_alert;
    this.auctionData=_auctionData;
    this.openAuctionDetails = _openAuctionDetails;
    this.closeAuctionDetails=_closedAuctionDetails;
    this.closeAuctionForm = _closeAuctionForm;
    this.closeAuctionForm.querySelector('button[type="submit"]').addEventListener('click', (e) => {
        e.preventDefault();
        let form = e.target.closest("form");
        if (form.checkValidity()) {
            let self = this
            makeCall("POST", 'AuctionDetailsServletHelper', form,
                function (req) {
                    if (req.readyState === 4) {
                        let message = req.responseText;
                        if (req.status === 200) {
                            self.show();
                        } else {
                            self.alert.textContent = message;
                        }
                    }
                }
            );
        } else {
            form.reportValidity();
        }
    });


    this.show = function () {
        var self = this;
        makeCall("GET", "AuctionDetailsServlet", null,
            function (req) {
                if (req.readyState === 4) {
                    if (req.status === 200) {
                        self.alert.textContent = ""
                        console.log(req.responseText);
                        var auctionData = JSON.parse(req.responseText);
                        console.log(auctionData);
                        if (auctionData.length === 0) {
                            self.alert.textContent = "No auctions found!";
                            return;
                        }
                        self.update(auctionData);
                    } else {
                        self.searchalert.textContent = req.responseText;
                        self.listcontainer.style.visibility = "hidden";
                        self.listcontainer.style.display = "none";
                    }
                }
            }
        );
    };

    this.update = function (auctionData) {
        if(auctionData.)
        this.listopenbody.innerHTML = ""; // empty the table body
        this.listclosedbody.innerHTML = ""; // empty the table body
        // build updated list
        let self = this;
        arrayAuctions.openAuctions.forEach(function (auction) { // self visible here, not this
            self.listopenbody.appendChild(self.setRowAuction(auction));
        });
        arrayAuctions.closedAuctions.forEach(function (auction) { // self visible here, not this
            self.listclosedbody.appendChild(self.setRowAuction(auction));
        });
        //FIXME: can delete, check
        self.listopenbody.style.visibility = "visible";
        self.listopenbody.style.display = null;
        self.listclosedbody.style.visibility = "visible";
        self.listclosedbody.style.display = null;
    }
}
