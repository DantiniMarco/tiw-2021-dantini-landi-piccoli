(function () { // avoid variables ending up in the global scope

    // page components
    var auctionsList, auctionsListSell, searchForm, auctionDetails, buttonManager,
        pageOrchestrator = new PageOrchestrator(); // main controller
    window.addEventListener("load", () => {
        pageOrchestrator.start(); // initialize the components
        pageOrchestrator.refresh();
    }, false);


    // Constructors of view components

    function AuctionList(_searchalert, _alert, _listcontainer, _listcontainerbody) {
        this.searchalert = _searchalert;
        this.alert = _alert;
        this.listcontainer = _listcontainer;
        this.listcontainerbody = _listcontainerbody;


        this.reset = function () {
            this.listcontainer.style.visibility = "hidden";
            this.listcontainer.style.display = "none";
        }

        this.show = function (keyword) {
            var self = this;
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
                        self.alert.textContent = message;
                    }
                }
            );
        };


        this.update = function (arrayAuctions) {
            var row, priceCell, raiseCell, dateCell, itemCell, idAuctionCell, linkcell, linkText, anchor;
            this.listcontainerbody.innerHTML = ""; // empty the table body
            // build updated list
            var self = this;
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
                    auctionDetails.show(e.target.getAttribute("auctionid")); // the list must know the details container
                }, false);
                anchor.href = "#";
                row.appendChild(linkcell);
                self.listcontainerbody.appendChild(row);
            });
            self.listcontainer.style.visibility = "visible";
            self.listcontainer.style.display = null;
        }

        this.autoclick = function (missionId) {
            var e = new Event("click");
            var selector = "a[auctionid='" + auctionId + "']";
            var anchorToClick =
                (auctionId) ? document.querySelector(selector) : this.listcontainerbody.querySelectorAll("a")[0];
            if (anchorToClick) anchorToClick.dispatchEvent(e);
        }

    }

    function AuctionListSell(_alert, _listopen, _listopenbody, _listclosed, _listclosedbody, _addauctionform) {
        this.alert = _alert;
        this.listopen = _listopen;
        this.listopenbody = _listopenbody;
        this.listclosed = _listclosed;
        this.listclosedbody = _listclosedbody;
        this.addauctionform = _addauctionform;
        this.addauctionform.querySelector('button[type="submit"]').addEventListener('click', (e) => {
            e.preventDefault();
            var form = e.target.closest("form");
            if (form.checkValidity()) {
                var self = this
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
                        self.alert.textContent = message;
                    }
                }
            );
        };


        this.update = function (arrayAuctions) {
            var row, priceCell, raiseCell, dateCell, itemCell, itemDescCell, linkcell, linkText, anchor;
            this.listopenbody.innerHTML = ""; // empty the table body
            this.listclosedbody.innerHTML = ""; // empty the table body
            // build updated list
            var self = this;
            arrayAuctions.openAuctions.forEach(function (auction) { // self visible here, not this
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
                self.listopenbody.appendChild(row);
            });
            arrayAuctions.closedAuctions.forEach(function (auction) { // self visible here, not this
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
                self.listclosedbody.appendChild(row);
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

    function PersonalMessage(_alert, _messagecontainer) {
        this.alert = _alert;
        this.userdata = null;
        this.messagecontainer = _messagecontainer;
        var self = this;
        makeCall("GET", "GetUserData", null,
            function (req) {
                let message = req.responseText;
                if (req.readyState === 4) {
                    if (req.status === 200) {
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
        let now = new Date()
        this.form = formId;
        this.alert = alert;

        this.registerEvents = function (orchestrator) {
            // Manage submit button
            this.form.querySelector('button[type="submit"]').addEventListener('click', (e) => {
                e.preventDefault();
                console.log(new FormData(e.target.form));
                auctionsList.show(new FormData(e.target.form).get("keyword"));
            });
        };

        this.reset = function () {
            /*var fieldsets = document.querySelectorAll("#" + this.form.id + " fieldset");
            fieldsets[0].hidden = false;
            fieldsets[1].hidden = true;
            fieldsets[2].hidden = true;*/
            //this.form.style.visibility = "hidden";

        }
    }

    function AuctionDetails(options) {
        this.alert = options['alert'];
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
                            self.update(formdata); // self is the object on which the function
                            // is applied

                        } else {
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
            this.currentPrice.textContent = "The current max price for this auctions is: " + new Intl.NumberFormat('it-IT', {
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

    function ButtonManager(_alert, _buycontainer, _sellcontainer, _buybar, _sellbar) {
        this.alert = _alert;
        this.buyContainer = _buycontainer;
        this.sellContainer = _sellcontainer;
        this.buyBar = _buybar;
        this.sellBar = _sellbar;
        this.buyBar.addEventListener('click', (e) => {
            this.show("buy")
        });
        this.sellBar.addEventListener('click', (e) => {
            this.show("sell")
        });
        this.show = function (buyOrSell) {
            var self = this;
            if (buyOrSell === "sell") {
                self.buyBar.className = null;
                self.sellBar.className = "active";
                self.buyContainer.style.visibility = "hidden";
                self.buyContainer.style.display = "none";
                self.sellContainer.style.visibility = "visible";
                self.sellContainer.style.display = null;
                pageOrchestrator.refresh(buyOrSell);
                return;
            }
            self.buyBar.className = "active";
            self.sellBar.className = null;
            self.buyContainer.style.visibility = "visible";
            self.buyContainer.style.display = null;
            self.sellContainer.style.visibility = "hidden";
            self.sellContainer.style.display = "none";
            pageOrchestrator.refresh(buyOrSell);
        };
    }

    function PageOrchestrator() {
        let alertContainer = document.getElementById("id_alert");
        let alertSearchContainer = document.getElementById("id_alert_search");
        let buyContainer = document.getElementById("id_buy");
        let buySearchContainer = document.getElementById("id_buy_search");
        let buyDetailsContainer = document.getElementById("id_buy_details");
        let sellContainer = document.getElementById("id_sell");
        let buyBar = document.getElementById("id_buybar");
        let sellBar = document.getElementById("id_sellbar");
        this.start = function () {


            new PersonalMessage(alertContainer, document.getElementById("id_username"));

            auctionsList = new AuctionList(
                alertSearchContainer,
                alertContainer,
                document.getElementById("id_listcontainer"),
                document.getElementById("id_listcontainerbody"));

            auctionsListSell = new AuctionListSell(
                alertContainer,
                document.getElementById("id_sellopencontainer"),
                document.getElementById("id_sellopencontainerbody"),
                document.getElementById("id_sellclosedcontainer"),
                document.getElementById("id_sellclosedcontainerbody"),
                document.getElementById("id_addAuctionForm"));


            searchForm = new SearchAuction(document.getElementById("id_searchauctionform"), alertSearchContainer);
            searchForm.registerEvents(this);

            auctionDetails = new AuctionDetails({ // many parameters, wrap them in an
                // object
                alert: alertContainer,
                bidlistcontainer: document.getElementById("id_bidlistcontainer"),
                bidlistcontainerbody: document.getElementById("id_bidlistcontainerbody"),
                bidform: document.getElementById("id_bidform"),
                itemName: document.getElementById("id_itemnametitle"),
                itemDescription: document.getElementById("id_itemdescriptiontitle"),
                itemImage: document.getElementById("id_itemimage"),
                currentPrice: document.getElementById("id_currentPriceTitle"),
                backButton: document.getElementById("id_goBackButtonDetails"),
                buySearchContainer: buySearchContainer,
                buyDetailsContainer: buyDetailsContainer
            });
            auctionDetails.registerEvents(this);


            document.querySelector("a[href='Logout']").addEventListener('click', () => {
                window.sessionStorage.removeItem('username');
            })

            buttonManager = new ButtonManager(alertContainer, buyContainer, sellContainer, buyBar, sellBar);

            buttonManager.show("buy");

        };


        this.refresh = function (buyOrSell) {
            alertContainer.textContent = "";
            if (buyOrSell === "buy") {
                auctionsList.reset();
                auctionDetails.reset();
            } else {
                auctionsListSell.reset();
                auctionsListSell.show();
            }
            //searchForm.reset();
            //searchForm.show();
        };
    }
})();
