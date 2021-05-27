function AuctionListSell(_alert, _listopen, _listopenbody, _listclosed, _listclosedbody, _addauctionform) {
    this.alert = _alert;
    this.listopen = _listopen;
    this.listopenbody = _listopenbody;
    this.listclosed = _listclosed;
    this.listclosedbody = _listclosedbody;
    this.addauctionform = _addauctionform;
    this.addauctionform.querySelector('button[type="submit"]').addEventListener('click', (e) => {
        e.preventDefault();
        let form = e.target.closest("form");
        if (form.checkValidity()) {
            let self = this
            makeCall("POST", 'SellHelperServlet', form,
                function (req) {
                    if (req.readyState === 4) {
                        var message = req.responseText;
                        if (req.status === 200) {
                            //has added auction
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
                } else {
                    self.alert.textContent = req.responseText;
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
        priceCell.textContent = new Intl.NumberFormat('it-IT', {
            style: 'currency',
            currency: 'EUR'
        }).format(auction.price);
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
        /*linkcell = document.createElement("td");
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
        anchor.href = "#";
        row.appendChild(linkcell);*/
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

    this.autoclick = function (missionId) {
        var e = new Event("click");
        var selector = "a[auctionid='" + auctionId + "']";
        var anchorToClick =
            (auctionId) ? document.querySelector(selector) : this.listcontainerbody.querySelectorAll("a")[0];
        if (anchorToClick) anchorToClick.dispatchEvent(e);
    }

}
