(function () { // avoid variables ending up in the global scope

    // page components
    let auctionsList, auctionsListSell, searchForm, auctionDetails, buttonManager, userData,
        pageOrchestrator = new PageOrchestrator(); // main controller
    window.addEventListener("load", () => {
        makeCall("GET", "GetUserData", null,
            function (req) {
                let message = req.responseText;
                if (req.readyState === 4) {
                    if (req.status === 200) {
                        console.log(req.responseText)
                        userData = JSON.parse(req.responseText);
                        pageOrchestrator.start(); // initialize the components
                        pageOrchestrator.refresh();
                    } else {
                        self.alert.textContent = message;
                    }
                }
            }
        );
    }, false);


    // Constructors of view components

    function PersonalMessage(_alert, _messagecontainer) {
        this.alert = _alert;
        this.userdata = null;
        this.messagecontainer = _messagecontainer;
        let self = this;
        self.messagecontainer.textContent = userData.firstName + " " + userData.lastName + " (" + userData.username + ")";

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
            let self = this;
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
        let alertWonAuction = document.getElementById("id_alertWonAuctions");
        let alertContainer = document.getElementById("id_alert");
        let alertSearchContainer = document.getElementById("id_alert_search");
        let buyContainer = document.getElementById("id_buy");
        let buySearchContainer = document.getElementById("id_buy_search");
        let buyDetailsContainer = document.getElementById("id_buy_details");
        let latestDetailsContainer = document.getElementById("id_latest_details");
        let sellContainer = document.getElementById("id_sell");
        let buyBar = document.getElementById("id_buybar");
        let sellBar = document.getElementById("id_sellbar");
        this.start = function () {
            new PersonalMessage(alertContainer, document.getElementById("id_username"));

            auctionDetails = new AuctionDetails({ // many parameters, wrap them in an
                // object
                alert: alertContainer,
                username: userData.username,
                bidlistcontainer: document.getElementById("id_bidlistcontainer"),
                bidlistcontainerbody: document.getElementById("id_bidlistcontainerbody"),
                bidform: document.getElementById("id_bidform"),
                itemName: document.getElementById("id_itemnametitle"),
                itemDescription: document.getElementById("id_itemdescriptiontitle"),
                itemImage: document.getElementById("id_itemimage"),
                currentPrice: document.getElementById("id_currentPriceTitle"),
                backButton: document.getElementById("id_goBackButtonDetails"),
                buySearchContainer: buySearchContainer,
                buyDetailsContainer: buyDetailsContainer,
                latestDetailsContainer : latestDetailsContainer
            });
            auctionDetails.registerEvents(this);

            wonAndLatestAuction = new WonAndLatestAuction(alertWonAuction, alertContainer, document.getElementById("id_wonAuctions"),
                document.getElementById("id_wonAuctions_body"), userData.username, document.getElementById("id_visitedAuctions"), document.getElementById("id_visitedAuctions_body"), auctionDetails );
            auctionsList = new AuctionList(
                alertSearchContainer,
                alertContainer,
                document.getElementById("id_listcontainer"),
                document.getElementById("id_listcontainerbody"),
                auctionDetails);

            auctionsListSell = new AuctionListSell(
                alertContainer,
                document.getElementById("id_sellopencontainer"),
                document.getElementById("id_sellopencontainerbody"),
                document.getElementById("id_sellclosedcontainer"),
                document.getElementById("id_sellclosedcontainerbody"),
                document.getElementById("id_addAuctionForm"),
                userData.username);


            searchForm = new SearchAuction(document.getElementById("id_searchauctionform"), alertSearchContainer, auctionsList, userData.username);
            searchForm.registerEvents(this);


            document.querySelector("a[href='Logout']").addEventListener('click', () => {
                window.sessionStorage.removeItem('username');
            })
            let userDataStored = JSON.parse(localStorage.getItem("userData"));
            if(userDataStored == null) {
                userDataStored = {};
            }
            if(userDataStored[userData.username] == null){
                userDataStored[userData.username] = {}
            }
            //let expirationDate = JSON.parse(localStorage.getItem("expirationDate"));
            let expirationDate = userDataStored[userData.username].expirationDate;
            let currentDate = new Date();
            let userDataToStore = userDataStored;
            let lastAction;
            let auctionsVisited;

            if (expirationDate == null || expirationDate < currentDate.getDate()) {
                currentDate.setMonth(currentDate.getMonth() + 1)
                userDataToStore[userData.username].expirationDate = currentDate;
                lastAction = "buy"
                userDataToStore[userData.username].lastAction = lastAction
                auctionsVisited = {};
                userDataToStore[userData.username].auctionsVisited = auctionsVisited;
                localStorage.setItem("userData", JSON.stringify(userDataToStore));
            }else {
                lastAction = userDataStored[userData.username].lastAction
                auctionsVisited = userDataStored[userData.username].auctionsVisited
            }
            buttonManager = new ButtonManager(alertContainer, buyContainer, sellContainer, buyBar, sellBar);

            buttonManager.show(lastAction, auctionsVisited);

        };


        this.refresh = function (buyOrSell) {
            alertContainer.textContent = "";
            if (buyOrSell === "buy") {
                auctionsList.reset();
                auctionDetails.reset();
                wonAndLatestAuction.show();
            } else {
                auctionsListSell.reset();
                auctionsListSell.show();
            }
            //searchForm.reset();
            //searchForm.show();
        };
    }
})();
