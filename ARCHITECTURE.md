# Online Auction System - Architecture Document

> **Project**: Online Auction System (Console-Based)
> **Language**: Java (JDK 8+)
> **Storage**: In-Memory (HashMap / ArrayList)
> **Author**: Adithya

---

## 1. System Overview

The Online Auction System is a console-based Java application that simulates a real-time auction platform. Users can register, list items for sale, place bids, and win auctions — all through an interactive text menu.

```mermaid
flowchart LR
    U["User (Console)"] -->|"Input"| APP["AuctionApp\n(Main UI)"]
    APP -->|"Auth"| US["UserService"]
    APP -->|"Auctions & Bids"| AS["AuctionService"]
    APP -->|"Alerts"| NS["NotificationService"]
    US -->|"CRUD"| UD[("Users\n(HashMap)")]
    AS -->|"CRUD"| AD[("Auctions\n(HashMap)")]
    AS -->|"CRUD"| BD[("Bids\n(HashMap)")]
    NS -->|"CRUD"| ND[("Notifications\n(HashMap)")]
```

---

## 2. Project Structure

```
Java Auction/
|-- src/
|   |-- AuctionApp.java         # Main entry point & console UI
|   |-- User.java               # User model (data class)
|   |-- AuctionItem.java        # Auction item model
|   |-- Bid.java                # Bid model
|   |-- AuctionStatus.java      # Enum: ACTIVE, SOLD, UNSOLD, CANCELLED
|   |-- UserService.java        # User registration, login, wallet
|   |-- AuctionService.java     # Auction CRUD, bidding, expiration
|   |-- NotificationService.java# In-memory notification store
|-- bin/                         # Compiled .class files
|-- ARCHITECTURE.md              # This file
```

---

## 3. Layered Architecture

The system follows a **3-layer architecture**:

```mermaid
flowchart TD
    subgraph Presentation ["Presentation Layer"]
        A["AuctionApp.java\n(Console Menus, I/O, Formatting)"]
    end

    subgraph Business ["Business Logic Layer (Services)"]
        B["UserService"]
        C["AuctionService"]
        D["NotificationService"]
    end

    subgraph Data ["Data Layer (In-Memory Storage)"]
        E["HashMap &lt; String, User &gt;"]
        F["HashMap &lt; String, AuctionItem &gt;"]
        G["HashMap &lt; String, List &lt; Bid &gt; &gt;"]
        H["HashMap &lt; String, List &lt; String &gt; &gt;"]
    end

    A --> B
    A --> C
    A --> D
    B --> E
    C --> F
    C --> G
    D --> H
```

| Layer | Responsibility | Files |
|-------|---------------|-------|
| **Presentation** | Console I/O, menus, formatting, input validation | `AuctionApp.java` |
| **Business Logic** | Core rules — auth, bidding validation, expiration, wallet ops | `UserService`, `AuctionService`, `NotificationService` |
| **Data / Model** | Plain Java objects + in-memory HashMap storage | `User`, `AuctionItem`, `Bid`, `AuctionStatus` |

---

## 4. Class Diagram

```mermaid
classDiagram
    class User {
        -String userId
        -String username
        -String password
        -String email
        -double balance
        +deposit(double amount)
        +withdraw(double amount) boolean
        +getUsername() String
        +getBalance() double
    }

    class AuctionItem {
        -String itemId
        -String sellerId
        -String title
        -String description
        -double startingPrice
        -double currentHighestBid
        -String currentHighestBidderId
        -LocalDateTime startTime
        -LocalDateTime endTime
        -AuctionStatus status
        +isExpired() boolean
        +getRemainingSeconds() long
    }

    class Bid {
        -String bidId
        -String itemId
        -String bidderId
        -double amount
        -LocalDateTime timestamp
    }

    class AuctionStatus {
        <<enumeration>>
        ACTIVE
        SOLD
        UNSOLD
        CANCELLED
        +getLabel() String
    }

    class UserService {
        -Map users
        -Map usernameToId
        +register(username, password, email) User
        +login(username, password) User
        +getUserById(userId) User
        +deposit(userId, amount)
    }

    class AuctionService {
        -Map auctions
        -Map bidsMap
        +createAuction(...) AuctionItem
        +placeBid(itemId, bidderId, amount, UserService) boolean
        +checkAndCloseExpiredAuctions(UserService)
        +getActiveAuctions() List
        +searchAuctions(keyword) List
        +getWonAuctions(userId) List
    }

    class NotificationService {
        -Map notifications
        +addNotification(userId, message)
        +getNotifications(userId) List
        +hasNotifications(userId) boolean
    }

    class AuctionApp {
        -UserService userService$
        -AuctionService auctionService$
        -NotificationService notificationService$
        -User currentUser$
        +main(args)$
    }

    AuctionItem --> AuctionStatus
    AuctionApp --> UserService
    AuctionApp --> AuctionService
    AuctionApp --> NotificationService
    UserService --> User
    AuctionService --> AuctionItem
    AuctionService --> Bid
```

---

## 5. Application Flow

### 5.1 User Authentication Flow

```mermaid
flowchart TD
    A["App Starts"] --> B["Show Welcome Menu"]
    B --> C{"User Choice?"}
    C -->|"Register"| D["Prompt: username, password, email"]
    D --> E{"Username\nunique?"}
    E -->|"Yes"| F["Create User\n(balance = $1000)"]
    E -->|"No"| G["Show error"]
    F --> B
    G --> B
    C -->|"Login"| H["Prompt: username, password"]
    H --> I{"Credentials\nvalid?"}
    I -->|"Yes"| J["Set currentUser\nShow Logged-In Menu"]
    I -->|"No"| K["Show error"]
    K --> B
    C -->|"Exit"| L["Goodbye!"]
```

### 5.2 Auction Lifecycle

```mermaid
stateDiagram-v2
    [*] --> ACTIVE : Seller creates auction
    ACTIVE --> ACTIVE : Bids placed
    ACTIVE --> SOLD : Time expires + has bids
    ACTIVE --> UNSOLD : Time expires + no bids
    ACTIVE --> CANCELLED : Seller cancels
    SOLD --> [*] : Winner pays, seller credited
    UNSOLD --> [*]
    CANCELLED --> [*]
```

### 5.3 Bidding Flow

```mermaid
flowchart TD
    A["User selects\nPlace a Bid"] --> B["Show active auctions"]
    B --> C["User picks auction #\nand enters bid amount"]
    C --> D{"Item\nexists?"}
    D -->|"No"| E["Error: Not found"]
    D -->|"Yes"| F{"Status\n= ACTIVE?"}
    F -->|"No"| G["Error: Not active"]
    F -->|"Yes"| H{"Not\nexpired?"}
    H -->|"No"| I["Error: Expired"]
    H -->|"Yes"| J{"Bidder\n!= Seller?"}
    J -->|"No"| K["Error: Can't bid\non own item"]
    J -->|"Yes"| L{"Amount >\ncurrent bid?"}
    L -->|"No"| M["Error: Bid\ntoo low"]
    L -->|"Yes"| N{"Balance\n>= amount?"}
    N -->|"No"| O["Error: Insufficient\nfunds"]
    N -->|"Yes"| P["Create Bid record\nUpdate highest bid\nSuccess!"]
```

> [!IMPORTANT]
> Balance is **not deducted** when a bid is placed. It is only deducted from the winner when the auction expires and is marked as **SOLD**.

### 5.4 Auction Expiration (Auto-Close)

```mermaid
sequenceDiagram
    participant App as AuctionApp
    participant AS as AuctionService
    participant US as UserService

    App->>AS: checkAndCloseExpiredAuctions(userService)
    loop Each ACTIVE auction
        AS->>AS: Is endTime past now?
        alt Has highest bidder
            AS->>AS: Set status = SOLD
            AS->>US: Deduct winner balance
            AS->>US: Credit seller balance
            AS->>App: Print "Auction SOLD" message
        else No bids placed
            AS->>AS: Set status = UNSOLD
            AS->>App: Print "Auction UNSOLD" message
        end
    end
```

---

## 6. Data Models

### 6.1 User

| Field | Type | Description |
|-------|------|-------------|
| `userId` | `String` (UUID) | Unique identifier |
| `username` | `String` | Login name (unique) |
| `password` | `String` | Password (plain text for simplicity) |
| `email` | `String` | Email address |
| `balance` | `double` | Wallet balance (default: $1000) |

### 6.2 AuctionItem

| Field | Type | Description |
|-------|------|-------------|
| `itemId` | `String` (UUID) | Unique auction ID |
| `sellerId` | `String` | User ID of the seller |
| `title` | `String` | Item title |
| `description` | `String` | Item description |
| `startingPrice` | `double` | Minimum starting price |
| `currentHighestBid` | `double` | Current top bid |
| `currentHighestBidderId` | `String` | User ID of top bidder |
| `startTime` | `LocalDateTime` | When auction started |
| `endTime` | `LocalDateTime` | When auction ends |
| `status` | `AuctionStatus` | ACTIVE / SOLD / UNSOLD / CANCELLED |

### 6.3 Bid

| Field | Type | Description |
|-------|------|-------------|
| `bidId` | `String` (UUID) | Unique bid ID |
| `itemId` | `String` | Which auction this bid is for |
| `bidderId` | `String` | User ID of the bidder |
| `amount` | `double` | Bid amount |
| `timestamp` | `LocalDateTime` | When the bid was placed |

---

## 7. Key Features

| Feature | Description |
|---------|-------------|
| **User Registration** | Create account with unique username, get $1000 starting balance |
| **User Login** | Authenticate with username + password |
| **Create Auction** | List an item with title, description, starting price, and duration |
| **Browse Auctions** | View all currently active auctions in a formatted table |
| **Search Auctions** | Find auctions by keyword (title or description) |
| **Place Bid** | Bid on active auctions with full validation |
| **Auto-Expiration** | Auctions auto-close when time runs out |
| **Wallet System** | Each user has a balance; deposit funds anytime |
| **Won Auctions** | View all auctions you've won |
| **Bid History** | View complete bid history for any auction |
| **Notifications** | In-memory notification system |
| **Demo Data** | Pre-seeded users (alice, bob) and auctions for testing |

---

## 8. How to Compile & Run

```bash
# Compile all source files
javac -d bin src/*.java

# Run the application
java -cp bin AuctionApp
```

### Demo Credentials (Pre-Seeded)

| Username | Password | Starting Balance |
|----------|----------|-----------------|
| `alice`  | `pass123` | $1000.00 |
| `bob`    | `pass456` | $1000.00 |

---

## 9. Design Decisions & Trade-offs

| Decision | Rationale |
|----------|-----------|
| **In-memory storage** | Keeps the project simple; no database setup required. Data resets on restart. |
| **No packages** | All files in one directory for easy compilation and understanding. |
| **UUID for IDs** | Guarantees uniqueness without a database sequence. |
| **Balance not deducted on bid** | Only the winner pays — prevents locking funds for losing bidders. |
| **Console-based UI** | Focuses on Java logic rather than GUI frameworks. |
| **Services pattern** | Separates business logic from UI, making it testable and maintainable. |

---

## 10. Possible Enhancements (Future Scope)

- **Database persistence** (MySQL / SQLite via JDBC)
- **GUI** using JavaFX or Swing
- **Multi-threading** for real-time auction countdowns
- **Password hashing** (BCrypt)
- **Email notifications** (JavaMail API)
- **Admin panel** for managing users and auctions
- **Category system** for auction items
- **Image upload** for auction listings
- **REST API** with Spring Boot for web-based access

