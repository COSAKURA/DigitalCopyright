// SPDX-License-Identifier: MIT
pragma solidity ^0.6.10;

contract DigitalCopyright {
    // 定义管理员和审核人员角色
    address public owner;
    mapping(address => bool) public reviewers;

    // 作品结构体
    struct Work {
        uint256 workId;
        string title;
        string description;
        string workHash;
        address currentOwner;
        address reviewer;
        uint8 status; // 0 - 未申请版权, 1 - 已申请版权
        string copyrightCertificate;
        uint256 createdAt;
        bool isOnAuction;
    }

    // 拍卖结构体
    struct Auction {
        uint256 auctionId;
        uint256 workId;
        address seller;
        uint256 startPrice;
        uint256 currentPrice;
        address highestBidder;
        uint256 endTime;
        bool isActive;
    }

    uint256 public workCounter;
    mapping(uint256 => Work) public works;

    uint256 public auctionCounter;
    mapping(uint256 => Auction) public auctions;

    event WorkRegistered(uint256 workId, address indexed owner, string title);
    event CopyrightReviewed(
        uint256 workId,
        string certificate,
        address reviewer
    );
    event AuctionStarted(
        uint256 auctionId,
        uint256 workId,
        address seller,
        uint256 startPrice,
        uint256 endTime
    );
    event NewBid(uint256 auctionId, address indexed bidder, uint256 bidAmount);
    event AuctionEnded(
        uint256 auctionId,
        address indexed winner,
        uint256 finalPrice
    );
    event AuctionCancelled(uint256 auctionId, address indexed seller);
    event OwnershipTransferred(uint256 workId, address indexed newOwner);

    // 构造函数，设置合约部署者为管理员
    constructor() public{
        owner = msg.sender;
        reviewers[msg.sender] = true;
    }

    // 修饰符：仅管理员
    modifier onlyOwner() {
        require(msg.sender == owner, "Caller is not the owner");
        _;
    }

    // 修饰符：仅审核人员
    modifier onlyReviewer() {
        require(reviewers[msg.sender], "Caller is not a reviewer");
        _;
    }

    // 修饰符：仅作品拥有者
    modifier onlyWorkOwner(uint256 _workId) {
        require(works[_workId].currentOwner == msg.sender, "Caller is not the owner of the work");
        _;
    }

    // 注册作品
    function registerWork(
        string memory _title,
        string memory _description,
        string memory _workHash
    ) public {
        workCounter++;
        works[workCounter] = Work({
            workId: workCounter,
            title: _title,
            description: _description,
            workHash: _workHash,
            currentOwner: msg.sender,
            reviewer: address(0),
            status: 0, // 初始状态为未申请版权
            copyrightCertificate: "",
            createdAt: block.timestamp,
            isOnAuction: false
        });

        emit WorkRegistered(workCounter, msg.sender, _title);
    }

    // 查询作品详细信息
    function getWorkDetails(uint256 _workId) public view returns (
        uint256 workId,
        string memory title,
        string memory description,
        string memory workHash,
        address currentOwner,
        address reviewer,
        uint8 status,
        string memory copyrightCertificate,
        uint256 createdAt,
        bool isOnAuction
    ) {

        Work storage work = works[_workId]; // 获取作品

        // 返回作品的详细信息
        return (
            work.workId,
            work.title,
            work.description,
            work.workHash,
            work.currentOwner,
            work.reviewer,
            work.status,
            work.copyrightCertificate,
            work.createdAt,
            work.isOnAuction
        );
    }

    // 审核作品版权
    function reviewCopyright(
        uint256 _workId,
        string memory _copyrightCertificate
    ) public onlyReviewer {
        require(
            works[_workId].currentOwner != msg.sender,
            "Reviewer cannot be the owner of the work"
        );
        require(
            works[_workId].status == 0,
            "Work must be in 'unapplied copyright' status to be reviewed"
        );
        require(
            bytes(_copyrightCertificate).length > 0,
            "Copyright certificate must be provided"
        );

        works[_workId].status = 1; // 更新状态为已申请版权
        works[_workId].copyrightCertificate = _copyrightCertificate;
        works[_workId].reviewer = msg.sender; // 记录审核人的地址

        emit CopyrightReviewed(
            _workId,
            _copyrightCertificate,
            msg.sender
        );
    }

    // 添加审核人员（仅管理员可调用）
    function addReviewer(address account) public onlyOwner {
        reviewers[account] = true;
    }

    // 移除审核人员（仅管理员可调用）
    function removeReviewer(address account) public onlyOwner {
        reviewers[account] = false;
    }

    // 开始作品拍卖
    function startAuction(
        uint256 _workId,
        uint256 _startPrice,
        uint256 _duration
    ) public {
        require(
            works[_workId].currentOwner == msg.sender,
            "Only the owner can start the auction"
        );
        require(
            works[_workId].status == 1,
            "Work must have an approved copyright"
        );
        require(
            !works[_workId].isOnAuction,
            "This work is already on auction"
        );

        auctionCounter++;
        uint256 auctionEndTime = block.timestamp + _duration;

        auctions[auctionCounter] = Auction({
            auctionId: auctionCounter,
            workId: _workId,
            seller: msg.sender,
            startPrice: _startPrice,
            currentPrice: _startPrice,
            highestBidder: address(0),
            endTime: auctionEndTime,
            isActive: true
        });

        works[_workId].isOnAuction = true;

        emit AuctionStarted(
            auctionCounter,
            _workId,
            msg.sender,
            _startPrice,
            auctionEndTime
        );
    }

    // 对活跃拍卖出价
    function placeBid(
        uint256 _auctionId,
        uint256 _bidAmount
    ) public {
        require(
            _auctionId > 0 && _auctionId <= auctionCounter,
            "Invalid auction ID"
        );
        Auction storage auction = auctions[_auctionId];

        require(auction.isActive, "Auction is no longer active");
        require(
            block.timestamp < auction.endTime,
            "Auction has ended"
        );
        require(
            _bidAmount > auction.currentPrice,
            "Bid must be higher than the current price"
        );

        // 获取拍卖对应的作品
        Work storage work = works[auction.workId];

        // 添加检查，防止作品拥有者出价
        require(
            work.currentOwner != msg.sender,
            "Owner cannot bid on their own work"
        );

        // 更新最高出价者和价格
        auction.highestBidder = msg.sender;
        auction.currentPrice = _bidAmount;

        emit NewBid(_auctionId, msg.sender, _bidAmount);
    }

    function endAuction(uint256 _auctionId) public {
        require(
            _auctionId > 0 && _auctionId <= auctionCounter,
            "Invalid auction ID"
        );
        Auction storage auction = auctions[_auctionId];

        require(auction.isActive, "Auction is no longer active");

        if (block.timestamp >= auction.endTime) {
            // 拍卖时间已结束，任何人都可以结束拍卖
        } else if (msg.sender == auction.seller) {
            // 卖家可以提前结束拍卖，但当有出价时不能提前结束
            require(
                auction.highestBidder == address(0),
                "Cannot end auction early with active bids"
            );
        } else {
            // 拍卖尚未结束，且调用者不是卖家，不能结束拍卖
            revert("Auction is still ongoing");
        }

        // 结束拍卖
        auction.isActive = false;
        works[auction.workId].isOnAuction = false;

        if (auction.highestBidder != address(0)) {
            works[auction.workId].currentOwner = auction.highestBidder;
            emit OwnershipTransferred(
                auction.workId,
                auction.highestBidder
            );
        }

        emit AuctionEnded(
            _auctionId,
            auction.highestBidder,
            auction.currentPrice
        );
    }


    // 取消活跃拍卖
    function cancelAuction(uint256 _auctionId) public {
        require(
            _auctionId > 0 && _auctionId <= auctionCounter,
            "Invalid auction ID"
        );
        Auction storage auction = auctions[_auctionId];

        require(auction.isActive, "Auction is no longer active");
        require(
            auction.seller == msg.sender,
            "Only the seller can cancel the auction"
        );
        require(
            auction.highestBidder == address(0),
            "Cannot cancel an auction with active bids"
        );

        auction.isActive = false;
        works[auction.workId].isOnAuction = false;

        emit AuctionCancelled(_auctionId, auction.seller);
    }

    // 禁用所有权转移函数，使管理员（合约部署者）无法更改
    function transferOwnership(address newOwner) public onlyOwner {
        revert("Ownership cannot be transferred");
    }
}
