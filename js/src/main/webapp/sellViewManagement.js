function AuctionListSell(_alert, _listopen, _listopenbody, _listclosed, _listclosedbody, _addauctionform, _username, _auctionDetails, _auctionDetailsSell) {
    this.alert = _alert;
    this.listopenbody = _listopenbody;
    this.listclosedbody = _listclosedbody;
    this.addauctionform = _addauctionform;
    this.username = _username;
    this.auctionDetails = _auctionDetails;
    this.auctionDetailsSell = _auctionDetailsSell;

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
                            let currentDate = new Date();
                            currentDate.setMonth(currentDate.getMonth() + 1)
                            userDataStored[self.username].expirationDate = currentDate;
                            userDataStored[self.username].lastAction = "sell";
                            localStorage.setItem("userData", JSON.stringify(userDataStored));
                            self.show();
                            self.auctionDetails.style.visibility="hidden";
                            self.auctionDetails.style.display="none";
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
        makeCall("GET", "SellServlet", null,
            function (req) {
                if (req.readyState === 4) {
                    if (req.status === 200) {
                        self.alert.textContent = "";
                        var sellData = JSON.parse(req.responseText);
                        if (sellData.length === 0) {
                            self.alert.textContent = "No auctions found!";
                            return;
                        }
                        self.update(sellData); // self visible by closure

                    } else {
                        self.searchalert.textContent = req.responseText;
                        self.listcontainer.style.visibility = "hidden";
                        self.listcontainer.style.display = "none";
                    }
                }
            }
        );
    };

    this.setRowAuction = function(auction, status){
        let row, priceCell, raiseCell, dateCell, timeLeftCell,itemCell, itemDescCell, linkcell, linkText, anchor;
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
        if(status==0){
            timeLeftCell = document.createElement("td");
            timeLeftCell.textContent = "" + this.calculateTimeLeft(auction.deadline);
            row.appendChild(timeLeftCell);
        }
        dateCell = document.createElement("td");
        dateCell.textContent = new Date(auction.deadline).toLocaleString();
        row.appendChild(dateCell);
        linkcell = document.createElement("td");
        anchor = document.createElement("a");
        linkcell.appendChild(anchor);
        linkText = document.createTextNode("Details");
        anchor.appendChild(linkText);
        anchor.setAttribute("auctionId", auction.idAuction);
        anchor.addEventListener("click", (e) => {
            // dependency via module parameter
            this.auctionDetailsSell.show(e.target.getAttribute("auctionId")); // the list must know the details container
        }, false);
        anchor.href = "#";
        row.appendChild(linkcell);
        return row;
    }

    this.update = function (sellData) {
        this.listopenbody.innerHTML = ""; // empty the table body
        this.listclosedbody.innerHTML = ""; // empty the table body
        // build updated list
        let self = this;
        this.addauctionform.querySelector('#deadlineLocalDateTime').min = dateToIsoString(new Date(sellData.dateLowerBound));
        this.addauctionform.querySelector('#deadlineLocalDateTime').max = dateToIsoString(new Date(sellData.dateUpperBound));
        sellData.openAuctions.forEach(function (auction) { // self visible here, not this
            self.listopenbody.appendChild(self.setRowAuction(auction, 0));
        });
        sellData.closedAuctions.forEach(function (auction) { // self visible here, not this
            self.listclosedbody.appendChild(self.setRowAuction(auction, 1));
        });
        //FIXME: can delete, check
        self.listopenbody.style.visibility = "visible";
        self.listopenbody.style.display = null;
        self.listclosedbody.style.visibility = "visible";
        self.listclosedbody.style.display = null;
    }


    this.calculateTimeLeft = function (deadline) {
        let timeLeft, diffHours, diffDays;
        let dateDeadline = new Date(deadline);
        let diff = dateDeadline - new Date().valueOf();
        if (diff <= 3600*1000) {
            if (diff < 1) {
                timeLeft = "Expired";
            } else {
                timeLeft = "Less than an hour";
            }
        } else {
            diffHours = diff / (60 * 60 * 1000) % 24;
            diffDays = diff / (24 * 60 * 60 * 1000);
            timeLeft = ((Math.floor(diffDays) > 0)?Math.floor(diffDays) + " days and ":"")+ Math.floor(diffHours) + " hour(s)";
        }

        return timeLeft;
    }


}


function AuctionDetailsSell(_alert,_sellContainer, _itemImage,_auctionDetails, _auctionData, _additionalAuctionDetails, _closeAuctionForm, _sellBackButton, _bidsTable,
                            _bidsTableBody, _auctionDetailsAlert) {
    this.alert=_alert;
    this.sellContainer=_sellContainer;
    this.itemImage =_itemImage;
    this.auctionDetails = _auctionDetails;
    this.auctionData = _auctionData;
    this.additionalAuctionDetails = _additionalAuctionDetails;
    this.closeAuctionForm = _closeAuctionForm;
    this.sellBackButton = _sellBackButton;
    this.bidsTable = _bidsTable;
    this.bidsTableBody = _bidsTableBody;
    this.auctionDetailsAlert = _auctionDetailsAlert;


    this.registerEvents = function (auctionListSell) {
        this.sellBackButton.addEventListener('click', (e) => {
            auctionListSell.show();
            this.sellContainer.style.visibility = "visible";
            this.sellContainer.style.display = null;
            this.auctionDetails.style.visibility = "hidden";
            this.auctionDetails.style.display = "none";
            this.additionalAuctionDetails.style.visibility="hidden";
            this.additionalAuctionDetails.style.display="none";
            this.auctionDetailsAlert.style.visibility="hidden";
            this.auctionDetailsAlert.style.display="none";
            this.closeAuctionForm.style.visibility = "hidden";
            this.closeAuctionForm.style.display = "none";
            this.bidsTable.style.visibility = "hidden";
            this.bidsTable.style.display = "none";
        });

    }
    this.show = function (auctionId) {
        var self = this;
        makeCall("GET", "AuctionDetailsServlet?auctionId=" + auctionId, null,
            function (req) {
                if (req.readyState === 4) {
                    if (req.status === 200) {
                        self.sellContainer.visibility="hidden";
                        self.sellContainer.style.display = "none";
                        self.alert.textContent = "";
                        let message = req.responseText;
                        var auctionDataBox = JSON.parse(req.responseText);
                        if (auctionDataBox.length === 0) {
                            self.alert.textContent = "No auctions found!";
                            return;
                        }
                        self.update(auctionDataBox);
                    } else {
                        self.alert.textContent = "Server error";
                    }
                }
            }
        );
    };

    this.update = function (auctionDataBox) {
        //Empty the table body
        this.auctionData.innerHTML = "";
        //Remove the possible pre-activated button
        let closeAuctionButton = this.closeAuctionForm.querySelector('button[type="submit"]');
        if(closeAuctionButton!=null){
            closeAuctionButton.remove();
        }
        //Auction data collection
        let itemName, itemDescription, price, minraise, deadline, status;
        itemName = document.createElement("p");
        itemName.textContent = "Name: " + auctionDataBox.auction.itemName;
        this.auctionData.appendChild(itemName);
        location.pathname.substring(0, location.pathname.lastIndexOf("/") + 1) + "ImageServlet?name=" + auctionDataBox.auction.itemImage;
        itemDescription = document.createElement("p");
        itemDescription.textContent = "Description: " + auctionDataBox.auction.itemDescription;
        this.auctionData.appendChild(itemDescription);
        price = document.createElement("p");
        if(auctionDataBox.auction.price == 0){
            price.textContent = "Last bid: No bids";
        }else{
            price.textContent = "Last bid: " +  new Intl.NumberFormat('it-IT', {
                style: 'currency',
                currency: 'EUR'
            }).format(auctionDataBox.auction.price);
        }
        this.auctionData.appendChild(price);
        minraise = document.createElement("p");
        minraise.textContent = "Minimum raise: " + auctionDataBox.auction.minRaise;
        this.auctionData.appendChild(minraise);
        deadline = document.createElement("p");
        deadline.textContent = "Deadline: " + new Date(auctionDataBox.auction.deadline).toLocaleString();
        this.auctionData.appendChild(deadline);
        status = document.createElement("p");
        status.textContent = "Status: " + auctionDataBox.auction.status;
        this.auctionData.appendChild(status);
        this.itemImage.src = location.pathname.substring(0, location.pathname.lastIndexOf("/") + 1) + "ImageServlet?name=" + auctionDataBox.auction.itemImage;
        this.auctionDetails.style.visibility = "visible";
        this.auctionData.style.visibility = "visible";
        this.auctionDetails.style.display = null;
        this.auctionData.style.display = null;
        this.auctionDetailsAlert.style.visibility="visible";
        this.auctionDetailsAlert.style.display=null;
        this.additionalAuctionDetails.style.visibility = "visible";
        this.additionalAuctionDetails.style.display = null;
        this.bidsTable.style.visibility = "hidden";
        this.bidsTable.style.display = "none";
        this.closeAuctionForm.style.visibility = "hidden";
        this.closeAuctionForm.style.display = "none";
        if (auctionDataBox.auction.status === "OPEN") {
            if (auctionDataBox.bids.length === 0) {
                this.auctionDetailsAlert.textContent = "This has item has recieved no bids";
            } else {
                let self = this;
                this.bidsTableBody.innerHTML = "";
                this.auctionDetailsAlert.textContent = "List of bids";
                auctionDataBox.bids.forEach(function (bid) { // self visible here, not this
                    self.bidsTableBody.appendChild(self.setRowBids(bid));
                });
                this.bidsTable.style.visibility = "visible";
                this.bidsTable.style.display = null;
            }
            let currDate = new Date().toISOString();
            if(auctionDataBox.auction.deadline<currDate){
                var auctionId = auctionDataBox.auction.idAuction;
                this.closeAuctionForm.style.visibility="visible";
                this.closeAuctionForm.style.display=null;
                this.closeAuctionForm.querySelector('input[type="hidden"]').value = auctionDataBox.auction.idAuction;
                let closeAuctionButton = document.createElement("button");
                closeAuctionButton.type = "submit";
                closeAuctionButton.textContent = "Close auction";
                this.closeAuctionForm.appendChild(closeAuctionButton);
                this.closeAuctionForm.querySelector('button[type="submit"]').addEventListener('click', (e)=>{
                    e.preventDefault();
                    let form = e.target.closest("form");
                    if (form.checkValidity()) {
                        let self = this;
                        makeCall("POST", 'AuctionDetailsServletHelper', form,
                            function (req) {
                                if (req.readyState === 4) {
                                    let message = req.responseText;
                                    if (req.status === 200) {
                                        self.sellContainer.style.visibility="hidden";
                                        self.sellContainer.style.display="none";
                                        self.show(auctionId);
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
            }
        } else {
            if(auctionDataBox.winner==null){
                this.auctionDetailsAlert.textContent = "Auction closed without a winner";
            }else{
                this.auctionDetailsAlert.innerHTML = "The winner is " + auctionDataBox.winner.firstName + " " + auctionDataBox.winner.lastName + "<br>" +
                "Send item here: " + auctionDataBox.winner.address;
            }
        }
    }

    this.setRowBids = function(bid){
        let row, bidderUsername, personalBid, raiseCell, dateAndTime;
        row = document.createElement("tr");
        bidderUsername = document.createElement("td");
        bidderUsername.textContent = bid.bidderUsername;
        row.appendChild(bidderUsername);
        personalBid = document.createElement("td");
        personalBid.textContent = new Intl.NumberFormat('it-IT', {
            style: 'currency',
            currency: 'EUR'
        }).format(bid.bidPrice);
        row.appendChild(personalBid);
        dateAndTime = document.createElement("td");
        dateAndTime.textContent = new Date(bid.dateTime).toLocaleString()
        row.appendChild(dateAndTime);

        return row;
    }

}
