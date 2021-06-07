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
                        var sellData = JSON.parse(req.responseText);
                        console.log(sellData);
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
            auctionDetailsSell.show(e.target.getAttribute("auctionId")); // the list must know the details container
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
        this.addauctionform.querySelector('#deadlineLocalDateTime').min = this.dateToIsoString(new Date(sellData.dateLowerBound));
        this.addauctionform.querySelector('#deadlineLocalDateTime').max = this.dateToIsoString(new Date(sellData.dateUpperBound));
        sellData.openAuctions.forEach(function (auction) { // self visible here, not this
            self.listopenbody.appendChild(self.setRowAuction(auction));
        });
        sellData.closedAuctions.forEach(function (auction) { // self visible here, not this
            self.listclosedbody.appendChild(self.setRowAuction(auction));
        });
        //FIXME: can delete, check
        self.listopenbody.style.visibility = "visible";
        self.listopenbody.style.display = null;
        self.listclosedbody.style.visibility = "visible";
        self.listclosedbody.style.display = null;
    }

    this.dateToIsoString = function (date) {
       pad = function(num) {
           var norm = Math.floor(num);
           return (norm < 10 ? '0' : '') + norm;
       };

        return date.getFullYear() +
            '-' + pad(date.getMonth() + 1) +
            '-' + pad(date.getDate()) +
            'T' + pad(date.getHours()) +
            ':' + pad(date.getMinutes());
    }

}


function AuctionDetailsSell(_alert, _auctionDetails, _auctionData, _openAuctionDetails, _closedAuctionDetails, _closeAuctionForm) {
    this.alert=_alert;
    this.auctionDetails = _auctionDetails;
    this.auctionData = _auctionData;
    this.openAuctionDetails = _openAuctionDetails;
    this.closeAuctionDetails = _closedAuctionDetails;
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


    this.show = function (auctionId) {
        var self = this;
        makeCall("GET", "AuctionDetailsServlet?auctionId=" + auctionId, null,
            function (req) {
                if (req.readyState === 4) {
                    if (req.status === 200) {
                        self.alert.textContent = ""
                        console.log(req.responseText);
                        var auctionDataBox = JSON.parse(req.responseText);
                        console.log(auctionDataBox);
                        if (auctionDataBox.length === 0) {
                            self.alert.textContent = "No auctions found!";
                            return;
                        }
                        self.update(auctionDataBox);
                    } else {
                        self.searchalert.textContent = req.responseText;
                        self.listcontainer.style.visibility = "hidden";
                        self.listcontainer.style.display = "none";
                    }
                }
            }
        );
    };

    //to update
    this.update = function (auctionDataBox) {
        this.auctionData.innerHTML = ""; // empty the table body
        let itemName, itemImage, itemDescription, price, minraise, deadline, status;
        itemName = document.createElement("p");
        itemName.textContent = auctionDataBox.auction.itemName;
        this.auctionData.appendChild(itemName);
        itemImage = document.createElement("p");
        itemImage.textContent = auctionDataBox.auction.itemImage;
        this.auctionData.appendChild(itemImage);
        itemDescription = document.createElement("p");
        itemDescription.textContent = auctionDataBox.auction.itemDescription;
        this.auctionData.appendChild(itemDescription);
        price = document.createElement("p");
        price.textContent = auctionDataBox.auction.price;
        this.auctionData.appendChild(price);
        minraise = document.createElement("p");
        minraise.textContent = auctionDataBox.auction.minRaise;
        this.auctionData.appendChild(minraise);
        deadline = document.createElement("p");
        deadline.textContent = new Date(auctionDataBox.auction.deadline).toLocaleString();
        this.auctionData.appendChild(deadline);
        status = document.createElement("p");
        status.textContent = auctionDataBox.auction.status;
        this.auctionData.appendChild(status);
        this.auctionDetails.style.visibility = "visible";
        this.auctionData.style.visibility = "visible";
        let self = this;
        if(auctionDataBox.auction.status===0){

        }else{
            //if(auctionDataBox.winner)
        }
    }

    //to update
    this.setRowBids = function(bid){
        let row, priceCell, raiseCell, dateCell, itemCell, itemDescCell, linkcell, linkText, anchor;
        row = document.createElement("tr");
        itemCell = document.createElement("td");
        itemCell.textContent = auction.itemName;
        row.appendChild(itemCell);
        itemDescCell = document.createElement("td");
        itemDescCell.textContent = auction.itemDescription;
        row.appendChild(itemDescCell);
        priceCell = document.createElement("td");

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

    }
}
