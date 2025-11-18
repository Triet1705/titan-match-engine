TitanMatch - High Performance Order Matching Engine

Dự án Backend Java Spring Boot tập trung vào Concurrency & Low Latency

1. Tổng quan dự án (Overview)

TitanMatch là một hệ thống backend mô phỏng lõi của một sàn giao dịch tài sản số (Crypto/Stock Exchange). Hệ thống tiếp nhận hàng nghìn lệnh đặt mua (Buy) và đặt bán (Sell) đồng thời, sau đó sử dụng thuật toán để khớp các lệnh phù hợp với nhau trong thời gian thực với độ trễ thấp nhất (Low latency).

Mục tiêu: Xử lý > 1.000 orders/giây với độ trễ < 10ms.

Tech Stack:

Language: Java 21 (Tận dụng Virtual Threads nếu có thể).

Framework: Spring Boot 3.x.

Database: PostgreSQL (Lưu trữ Persistence), Redis (Caching - Optional).

Messaging: Apache Kafka (Xử lý hàng đợi lệnh - Input Queue).

Monitoring: Prometheus & Grafana (Thay cho UI Frontend).

Testing: JUnit 5, Mockito, JMeter (Load testing).

2. Tính năng cốt lõi (Core Features)

A. Quản lý Sổ lệnh (Order Book Management)

Hệ thống duy trì một Order Book cho mỗi cặp giao dịch (VD: BTC/USDT).

Buy Side (Bid): Sắp xếp giá giảm dần (Người mua giá cao nhất được ưu tiên).

Sell Side (Ask): Sắp xếp giá tăng dần (Người bán giá thấp nhất được ưu tiên).

Time Priority: Nếu cùng mức giá, ai đặt trước được khớp trước (FIFO).

B. Loại lệnh hỗ trợ (Order Types)

Limit Order: Mua/Bán tại mức giá xác định hoặc tốt hơn. Lệnh sẽ nằm trong Order Book chờ đến khi khớp.

Market Order: Mua/Bán ngay lập tức tại mức giá tốt nhất đang có trên thị trường. (Optional - làm sau).

C. Cơ chế khớp lệnh (Matching Engine)

Logic chạy liên tục để quét Bid và Ask.

Khi Bid.Price >= Ask.Price -> Khớp lệnh (Trade Executed).

Cập nhật lại số lượng (Quantity) còn lại của lệnh hoặc xóa lệnh khỏi sổ nếu khớp hết.

3. Kiến trúc hệ thống (Architecture)

graph LR
    Client[Client / Load Test Script] -- REST API --> Controller[Order Controller]
    Controller -- 1. Submit Order --> Kafka[Kafka Topic: orders.input]
    Kafka -- 2. Consume --> Engine[Matching Engine Core]
    Engine -- 3. Process (In-Memory) --> OrderBook[Order Book (TreeMap/PriorityQueue)]
    Engine -- 4. Match Found --> EventBus[Event Publisher]
    EventBus -- 5. Save Async --> DB[(PostgreSQL)]
    EventBus -- 6. Metrics --> Grafana[Grafana Dashboard]


Giải thích luồng dữ liệu:

API Layer: Nhận request, validate dữ liệu cơ bản, đẩy vào hàng đợi (Queue). Không xử lý khớp lệnh tại đây để tránh block request.

Matching Core: Là một Single-Threaded Loop (hoặc Multi-threaded với Lock kỹ) lấy lệnh từ Queue ra xử lý tuần tự.

In-Memory Processing: Toàn bộ việc khớp lệnh diễn ra trên RAM (Java Heap) để đạt tốc độ tối đa.

Async Persistence: Sau khi khớp xong, kết quả mới được đẩy ra một luồng khác để lưu xuống Database. Việc ghi ổ cứng (I/O) không được phép làm chậm việc khớp lệnh.

4. Cấu trúc dữ liệu & Class Design (Quan trọng)

Đây là phần bạn cần code kỹ bằng Java thuần.

Class: Order

public class Order {
    private Long orderId;
    private Long userId;
    private BigDecimal price;
    private double quantity;
    private Side side; // BUY hoặc SELL
    private long timestamp;
    // Getter, Setter, Constructor
}


Class: OrderBook

Sử dụng TreeMap để tự động sắp xếp giá.

public class OrderBook {
    // Key: Price, Value: List of Orders (FIFO queue at that price)
    // Bid: Giảm dần (Collections.reverseOrder())
    private TreeMap<BigDecimal, List<Order>> bids = new TreeMap<>(Collections.reverseOrder());
    
    // Ask: Tăng dần (Mặc định)
    private TreeMap<BigDecimal, List<Order>> asks = new TreeMap<>();

    public void addOrder(Order order) { ... }
    public void removeOrder(Order order) { ... }
    public void match() { ... logic khớp lệnh nằm ở đây ... }
}
