function AuctionList(_searchalert, _alert, _listcontainer, _listcontainerbody, _auctionDetailsInt) {
    this.searchalert = _searchalert;
    this.alert = _alert;
    this.listcontainer = _listcontainer;
    this.listcontainerbody = _listcontainerbody;
    this.auctionDetailsInt = _auctionDetailsInt

    this.reset = function () {
        this.listcontainer.style.visibility = "hidden";
        this.listcontainer.style.display = "none";
    }

    this.show = function (keyword) {
        let self = this;
        makeCall("GET", "GetSearchedAuction?keyword=" + keyword, null,
            function (req) {
                if (req.readyState === 4) {
                    if (req.status === 200) {
                        self.searchalert.textContent = ""
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
                } else {
                    self.alert.textContent = req.responseText;
                }
            }
        );
    };


    this.update = function (arrayAuctions) {
        let row, priceCell, raiseCell, dateCell, itemCell, idAuctionCell, linkcell, linkText, anchor;
        this.listcontainerbody.innerHTML = ""; // empty the table body
        // build updated list
        let self = this;
        arrayAuctions.forEach(function (auction) { // self visible here, not this
            row = document.createElement("tr");
            idAuctionCell = document.createElement("td");
            idAuctionCell.textContent = auction.idAuction;
            row.appendChild(idAuctionCell);
            itemCell = document.createElement("td");
            itemCell.textContent = auction.itemName;
            row.appendChild(itemCell);
            priceCell = document.createElement("td");
            priceCell.textContent = new Intl.NumberFormat('it-IT', {
                style: 'currency',
                currency: 'EUR'
            }).format(auction.initialPrice);
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
                self.auctionDetailsInt.show(e.target.getAttribute("auctionid")); // the list must know the details container
            }, false);
            anchor.href = "#";
            row.appendChild(linkcell);
            self.listcontainerbody.appendChild(row);
        });
        self.listcontainer.style.visibility = "visible";
        self.listcontainer.style.display = null;
    }

}

function SearchAuction(formId, alert, auctionsListInt, username) {
    this.form = formId;
    this.alert = alert;
    this.auctionsListInt = auctionsListInt;
    this.username = username

    this.registerEvents = function (orchestrator) {
        // Manage submit button
        this.form.querySelector('button[type="submit"]').addEventListener('click', (e) => {
            e.preventDefault();
            console.log(new FormData(e.target.form));
            let userDataStored = JSON.parse(localStorage.getItem("userData"));
            userDataStored[this.username].lastAction = "buy";
            localStorage.setItem("userData", JSON.stringify(userDataStored));
            this.auctionsListInt.show(new FormData(e.target.form).get("keyword"));
        });
    };

    this.reset = function () {
        // delete keyword from search
    }
}

function AuctionDetails(options) {
    this.alert = options['alert'];
    this.username = options['username'];
    this.bidlistcontainer = options['bidlistcontainer'];
    this.bidlistcontainerbody = options['bidlistcontainerbody'];
    this.bidform = options['bidform'];
    this.itemName = options['itemName'];
    this.itemImage = options['itemImage'];
    this.itemDescription = options['itemDescription'];
    this.currentPrice = options['currentPrice'];
    this.buySearchContainer = options['buySearchContainer'];
    this.buyDetailsContainer = options['buyDetailsContainer'];
    this.backButton = options['backButton'];
    this.currentAuctionId = '1';

    this.registerEvents = function (orchestrator) {
        this.bidform.querySelector('button[type="submit"]').addEventListener('click', (e) => {
            e.preventDefault();
            let form = e.target.closest("form");
            if (form.checkValidity()) {
                let self = this;
                makeCall("POST", 'CreateBid', form,
                    function (req) {
                        if (req.readyState === 4) {
                            let message = req.responseText;
                            if (req.status === 200) {
                                //add bid
                                self.show(self.currentAuctionId);
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
        this.backButton.addEventListener('click', (e) => {
            this.buySearchContainer.style.visibility = "visible"
            this.buySearchContainer.style.display = null
            this.buyDetailsContainer.style.visibility = "hidden"
            this.buyDetailsContainer.style.display = "none"
        });
    }

    this.show = function (auctionid) {
        var self = this;
        this.currentAuctionId = auctionid;
        makeCall("GET", "GoToBidPage?idauction=" + auctionid, null,
            function (req) {
                if (req.readyState === 4) {
                    let message = req.responseText;
                    if (req.status === 200) {
                        let formdata = JSON.parse(req.responseText);
                        let userDataStored = JSON.parse(localStorage.getItem("userData"));
                        userDataStored[self.username].auctionsVisited[auctionid] = Date.now();
                        localStorage.setItem("userData", JSON.stringify(userDataStored));
                        self.update(formdata); // self is the object on which the function
                        // is applied
                    }
                    else {
                        self.alert.textContent = message;

                    }
                }
            }
        );
    };

    this.reset = function () {
        this.buyDetailsContainer.style.visibility = "hidden"
        this.buyDetailsContainer.style.display = "none"
        this.buySearchContainer.style.visibility = "visible"
        this.buySearchContainer.style.display = null
    }

    this.update = function (formdata) {
        this.itemName.textContent = formdata.item.name;
        this.itemImage.src = location.pathname.substring(0, location.pathname.lastIndexOf("/") + 1) + "ImageServlet?name=" + formdata.item.image;
        this.itemDescription.textContent = formdata.item.description;
        this.currentPrice.textContent = "The minimum bet is: " + new Intl.NumberFormat('it-IT', {
            style: 'currency',
            currency: 'EUR'
        }).format(formdata.currMax);
        this.bidlistcontainerbody.innerHTML = ""; // empty the table body
        var row, idBid, priceCell, dateCell;
        // build updated list
        var self = this;
        if (formdata.bids.length === 0) {
            console.log("No bids")
        }
        formdata.bids.forEach(function (bid) { // self visible here, not this
            row = document.createElement("tr");
            idBid = document.createElement("td");
            idBid.textContent = bid.idBid;
            row.appendChild(idBid);
            priceCell = document.createElement("td");
            priceCell.textContent = new Intl.NumberFormat('it-IT', {
                style: 'currency',
                currency: 'EUR'
            }).format(bid.bidPrice);
            row.appendChild(priceCell);
            dateCell = document.createElement("td");
            dateCell.textContent = bid.dateTime;
            row.appendChild(dateCell);
            self.bidlistcontainerbody.appendChild(row);
        });
        this.bidform.querySelector("input[type = 'hidden']").value = this.currentAuctionId
        this.buySearchContainer.style.visibility = "hidden"
        this.buySearchContainer.style.display = "none"
        this.buyDetailsContainer.style.visibility = "visible"
        this.buyDetailsContainer.style.display = null
    }
}

// TODO: Marco da fare

function WonAndLatestAuction(_alertWonAuction,_alert, _wonAuctions, _wonAuctions_body, _username, _visitedAuctions, _visitedAuctions_body) {
    this.alert = _alert;
    this.alertWonAuction = _alertWonAuction;
    this.wonAuctions = _wonAuctions;
    this.wonAuctions_body = _wonAuctions_body;
    this.username = _username;
    this.visitedAuctions = _visitedAuctions;
    this.visitedAuctions_body = _visitedAuctions_body;
    this.show = function () {
        let self = this;
        let userDataStored = JSON.parse(localStorage.getItem("userData"));
        let auctionsVisited = userDataStored[this.username].auctionsVisited;
        console.log(auctionsVisited);
        let getParameters = "";
        for (const auctionId of Object.keys(auctionsVisited)){
            getParameters += "auction=" + auctionId + "&";
        }
        makeCall("GET", "GetWonAuctions?" + getParameters, null,
            function (req) {
                if (req.readyState === 4) {
                    if (req.status === 200) {
                        self.alertWonAuction.textContent = ""
                        self.alert.textContent = ""
                        console.log(req.responseText);
                        var auctionsToShow = JSON.parse(req.responseText);
                        console.log(auctionsToShow);


                        if (auctionsToShow.wonAuction.length === 0) {
                            self.wonAuctions.style.visibility = "hidden";
                            self.wonAuctions.style.display = "none";
                            self.alertWonAuction.textContent = "You haven't won an auction yet";
                        } else {
                            self.updateWon(auctionsToShow.wonAuction); // self visible by closure
                            self.wonAuctions.style.visibility = "visible";
                            self.wonAuctions.style.display = null;
                        }
                        if (auctionsToShow.auctionsVisited.length === 0) {
                            // hide last visited
                            self.visitedAuctions.style.visibility = "hidden";
                            self.visitedAuctions.style.display = "none";
                        }else{
                            // show last visited
                            self.updateVisited(auctionsToShow.auctionsVisited);
                            self.visitedAuctions.style.visibility = "visible";
                            self.visitedAuctions.style.display = null;
                        }
                    } else {
                        self.alert.textContent = req.responseText;
                        self.wonAuctions.style.visibility = "hidden";
                        self.wonAuctions.style.display = "none";
                    }
                } else {
                    self.alert.textContent = req.responseText;
                }
            }
        );
    };

    this.updateVisited = function (arrayAuctions){
        let row, priceCell, descriptionCell, dateCell, itemCell, idAuctionCell, linkcell, linkText, anchor;
        this.visitedAuctions_body.innerHTML = ""; // empty the table body
        // build updated list
        let self = this;
        arrayAuctions.forEach(function (auction) { // self visible here, not this
            row = document.createElement("tr");

            /*priceCell = document.createElement("td");
            priceCell.textContent = new Intl.NumberFormat('it-IT', {
                style: 'currency',
                currency: 'EUR'
            }).format(auction.price);
            row.appendChild(priceCell);*/

            itemCell = document.createElement("td");
            itemCell.textContent = auction.itemName;
            row.appendChild(itemCell);

            descriptionCell = document.createElement("td");
            descriptionCell.textContent = auction.itemDescription;
            row.appendChild(descriptionCell);

            anchor = document.createElement("a");
            linkcell.appendChild(anchor);
            linkText = document.createTextNode("Details");
            anchor.appendChild(linkText);
            anchor.auctionid = auction.id; // make list item clickable
            anchor.setAttribute('auctionid', auction.idAuction); // set a custom HTML attribute
            anchor.addEventListener("click", (e) => {
                // dependency via module parameter
                self.auctionDetailsInt.show(e.target.getAttribute("auctionid")); // the list must know the details container
            }, false);
            anchor.href = "#";
            row.appendChild(linkcell);



            self.visitedAuctions_body.appendChild(row);
        });
        self.visitedAuctions.style.visibility = "visible";
        self.visitedAuctions.style.display = null;
    }

    this.updateWon = function (arrayAuctions) {
        let row, priceCell, descriptionCell, dateCell, itemCell, idAuctionCell, linkcell, linkText, anchor;
        this.wonAuctions_body.innerHTML = ""; // empty the table body
        // build updated list
        let self = this;
        arrayAuctions.forEach(function (auction) { // self visible here, not this
            row = document.createElement("tr");
            priceCell = document.createElement("td");
            priceCell.textContent = new Intl.NumberFormat('it-IT', {
                style: 'currency',
                currency: 'EUR'
            }).format(auction.price);
            row.appendChild(priceCell);

            itemCell = document.createElement("td");
            itemCell.textContent = auction.itemName;
            row.appendChild(itemCell);

            descriptionCell = document.createElement("td");
            descriptionCell.textContent = auction.itemDescription;
            row.appendChild(descriptionCell);

            self.wonAuctions_body.appendChild(row);
        });
        self.wonAuctions.style.visibility = "visible";
        self.wonAuctions.style.display = null;
    }


}