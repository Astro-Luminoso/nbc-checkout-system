-- MySQL dummy data (H2 local-dummy-data.sql을 MySQL 문법으로 변환)
-- Run this after the app has started once (Hibernate creates tables via ddl-auto: update).
-- All dummy members use this password: password1234

SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM refunds;
DELETE FROM point_transactions;
DELETE FROM webhook_events;
DELETE FROM payments;
DELETE FROM order_items;
DELETE FROM orders;
DELETE FROM cart_items;
DELETE FROM products;
DELETE FROM members;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO members (
    id, email, password, name, phone_number, point_balance, created_date, updated_date
) VALUES
    (1, 'buyer1@example.com', '$2a$10$IWBjlOz40gxtol4.DznkEO1e2I3w6VC9KZ0kkkhyS3dLFV8XCUEFO',
     '김구매', '010-1111-1111', 12000, '2026-05-01 09:00:00', '2026-06-09 09:00:00'),
    (2, 'buyer2@example.com', '$2a$10$IWBjlOz40gxtol4.DznkEO1e2I3w6VC9KZ0kkkhyS3dLFV8XCUEFO',
     '이결제', '010-2222-2222', 6930, '2026-05-10 10:00:00', '2026-06-08 14:30:00'),
    (3, 'buyer3@example.com', '$2a$10$IWBjlOz40gxtol4.DznkEO1e2I3w6VC9KZ0kkkhyS3dLFV8XCUEFO',
     '박테스트', '010-3333-3333', 0, '2026-06-01 11:00:00', '2026-06-07 16:00:00');

INSERT INTO products (
    id, name, description, price, stock_quantity, category, sale_price, sale_status,
    created_date, updated_date
) VALUES
    (1, '무선 키보드', '저소음 기계식 무선 키보드', 89000, 25, 'DIGITAL', '79000', 'ON_SALE',
     '2026-04-01 09:00:00', '2026-06-01 09:00:00'),
    (2, '무선 마우스', '인체공학 블루투스 마우스', 39000, 40, 'DIGITAL', '35000', 'ON_SALE',
     '2026-04-02 09:00:00', '2026-06-02 09:00:00'),
    (3, '27인치 모니터', 'QHD IPS 사무용 모니터', 329000, 8, 'DIGITAL', '299000', 'ON_SALE',
     '2026-04-03 09:00:00', '2026-06-03 09:00:00'),
    (4, 'USB-C 허브', 'HDMI와 PD 충전을 지원하는 7-in-1 허브', 59000, 0, 'DIGITAL', '49000', 'OUT_OF_STOCK',
     '2026-04-04 09:00:00', '2026-06-04 09:00:00'),
    (5, '노트북 거치대', '높이 조절 알루미늄 거치대', 45000, 18, 'OFFICE', '39000', 'ON_SALE',
     '2026-04-05 09:00:00', '2026-06-05 09:00:00'),
    (6, '데스크 매트', '방수 가죽 대형 데스크 매트', 32000, 50, 'OFFICE', '28000', 'ON_SALE',
     '2026-04-06 09:00:00', '2026-06-06 09:00:00'),
    (7, '텀블러', '보온 보냉 스테인리스 텀블러', 28000, 30, 'LIVING', '25000', 'ON_SALE',
     '2026-04-07 09:00:00', '2026-06-07 09:00:00'),
    (8, '캠핑 의자', '접이식 경량 캠핑 의자', 65000, 12, 'OUTDOOR', '59000', 'ON_SALE',
     '2026-04-08 09:00:00', '2026-06-08 09:00:00'),
    (9, '구형 웹캠', '단종된 웹캠 상품', 49000, 0, 'DIGITAL', '49000', 'DISCONTINUED',
     '2026-03-01 09:00:00', '2026-05-01 09:00:00'),
    (10, '개발자 스티커 세트', '노트북용 방수 스티커 10종', 12000, 100, 'ETC', '9900', 'ON_SALE',
     '2026-05-01 09:00:00', '2026-06-09 09:00:00');

INSERT INTO cart_items (
    id, member_id, product_id, quantities, created_date, updated_date
) VALUES
    (1, 1, 1, 1, '2026-06-09 09:10:00', '2026-06-09 09:10:00'),
    (2, 1, 2, 2, '2026-06-09 09:15:00', '2026-06-09 09:15:00'),
    (3, 2, 3, 1, '2026-06-08 13:00:00', '2026-06-08 13:00:00'),
    (4, 3, 6, 3, '2026-06-07 15:00:00', '2026-06-07 15:00:00');

INSERT INTO orders (
    id, member_id, total_amount, used_point, order_status, created_date, updated_date
) VALUES
    (1, 1, 78000, 3000, 'STANDBY', '2026-06-09 10:00:00', '2026-06-09 10:00:00'),
    (2, 2, 198000, 5000, 'PAID', '2026-06-08 14:00:00', '2026-06-08 14:05:00'),
    (3, 1, 45000, 0, 'CANCELLED', '2026-06-06 12:00:00', '2026-06-06 13:00:00'),
    (4, 3, 32000, 0, 'DECLINED', '2026-06-07 16:00:00', '2026-06-07 16:01:00');

INSERT INTO order_items (
    id, order_id, product_id, name, price, quantities, created_date, updated_date
) VALUES
    (1, 1, 2, '무선 마우스', 39000, 2, '2026-06-09 10:00:00', '2026-06-09 10:00:00'),
    (2, 2, 1, '무선 키보드', 89000, 2, '2026-06-08 14:00:00', '2026-06-08 14:05:00'),
    (3, 2, 10, '개발자 스티커 세트', 12000, 1, '2026-06-08 14:00:00', '2026-06-08 14:05:00'),
    (4, 3, 5, '노트북 거치대', 45000, 1, '2026-06-06 12:00:00', '2026-06-06 13:00:00'),
    (5, 4, 6, '데스크 매트', 32000, 1, '2026-06-07 16:00:00', '2026-06-07 16:01:00');

INSERT INTO payments (
    id, version, order_id, port_one_payment_id, paid_amount, status, paid_at
) VALUES
    (1, 0, 1, 'payment-dummy-pending-001', 75000, 'PENDING', NULL),
    (2, 0, 2, 'payment-dummy-completed-002', 193000, 'COMPLETED', '2026-06-08 14:05:00'),
    (3, 1, 3, 'payment-dummy-refunded-003', 45000, 'REFUNDED', '2026-06-06 12:05:00'),
    (4, 0, 4, 'payment-dummy-failed-004', 32000, 'FAILED', NULL);

INSERT INTO refunds (
    id, payment_id, pg_refund_amount, point_refund_amount, reason, created_date
) VALUES
    (1, 3, 45000, 0, '단순 변심으로 인한 전체 환불', '2026-06-06 13:00:00');

INSERT INTO point_transactions (
    id, members_id, amount, type, order_id, created_date
) VALUES
    (1, 1, -3000, 'USE', 1, '2026-06-09 10:00:00'),
    (2, 2, -5000, 'USE', 2, '2026-06-08 14:00:00'),
    (3, 2, 1930, 'EARN', 2, '2026-06-08 14:05:00'),
    (4, 1, 450, 'EARN', 3, '2026-06-06 12:05:00'),
    (5, 1, -450, 'EARN_CANCEL', 3, '2026-06-06 13:00:00');

INSERT INTO webhook_events (
    id, webhook_id, event_type, payload, status, error_message, created_date
) VALUES
    (1, 'webhook-dummy-001', 'Transaction.Paid',
     '{"type":"Transaction.Paid","data":{"paymentId":"payment-dummy-completed-002"}}',
     'PROCESSED', NULL, '2026-06-08 14:05:01'),
    (2, 'webhook-dummy-002', 'Transaction.Failed',
     '{"type":"Transaction.Failed","data":{"paymentId":"payment-dummy-failed-004"}}',
     'PROCESSED', NULL, '2026-06-07 16:01:01'),
    (3, 'webhook-dummy-003', 'Unknown.Event',
     '{"type":"Unknown.Event"}',
     'IGNORED', '지원하지 않는 이벤트 타입', '2026-06-09 08:00:00');
