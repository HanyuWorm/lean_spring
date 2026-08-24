const commerce = db.getSiblingDB("commerce");

commerce.createCollection("customers", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["_id", "email", "fullName", "createdAt"],
      properties: {
        _id: { bsonType: "long" },
        email: { bsonType: "string" },
        fullName: { bsonType: "string" },
        createdAt: { bsonType: "date" }
      }
    }
  }
});

commerce.createCollection("products", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["_id", "sku", "name", "price", "stock", "version", "active"],
      properties: {
        _id: { bsonType: "long" },
        sku: { bsonType: "string" },
        price: { bsonType: "decimal" },
        stock: { bsonType: "int", minimum: 0 },
        version: { bsonType: "long" },
        active: { bsonType: "bool" }
      }
    }
  }
});

commerce.createCollection("orders", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["_id", "customerId", "idempotencyKey", "status", "totalAmount", "items", "createdAt"],
      properties: {
        _id: { bsonType: "long" },
        customerId: { bsonType: "long" },
        idempotencyKey: { bsonType: "string" },
        status: { enum: ["PENDING", "PAID", "CANCELLED"] },
        totalAmount: { bsonType: "decimal" },
        items: {
          bsonType: "array",
          items: {
            bsonType: "object",
            required: ["productId", "productNameSnapshot", "unitPrice", "quantity"]
          }
        },
        createdAt: { bsonType: "date" }
      }
    }
  }
});

commerce.createCollection("outboxEvents");

commerce.customers.createIndex({ email: 1 }, { unique: true });
commerce.products.createIndex({ sku: 1 }, { unique: true });
commerce.orders.createIndex({ customerId: 1, idempotencyKey: 1 }, { unique: true });
commerce.orders.createIndex({ customerId: 1, createdAt: -1, _id: -1 });
commerce.orders.createIndex({ status: 1, createdAt: -1 });
commerce.outboxEvents.createIndex(
  { occurredAt: 1 },
  { partialFilterExpression: { publishedAt: null } }
);

const now = new Date();
const daysAgo = days => new Date(now.getTime() - days * 24 * 60 * 60 * 1000);

commerce.customers.insertMany([
  { _id: NumberLong(1), email: "an@example.com", fullName: "Nguyen An", createdAt: daysAgo(30) },
  { _id: NumberLong(2), email: "binh@example.com", fullName: "Tran Binh", createdAt: daysAgo(20) },
  { _id: NumberLong(3), email: "chi@example.com", fullName: "Le Chi", createdAt: daysAgo(10) }
]);

commerce.products.insertMany([
  { _id: NumberLong(1), sku: "JAVA-21", name: "Modern Java 21", price: Decimal128("39.90"), stock: 100, version: NumberLong(0), active: true },
  { _id: NumberLong(2), sku: "DB-ARCH", name: "Database Architecture", price: Decimal128("49.90"), stock: 50, version: NumberLong(0), active: true },
  { _id: NumberLong(3), sku: "SPRING-4", name: "Spring Boot 4", price: Decimal128("44.90"), stock: 75, version: NumberLong(0), active: true }
]);

commerce.orders.insertMany([
  {
    _id: NumberLong(1), customerId: NumberLong(1), idempotencyKey: "checkout-001",
    status: "PAID", totalAmount: Decimal128("89.80"), createdAt: daysAgo(5),
    items: [
      { productId: NumberLong(1), productNameSnapshot: "Modern Java 21", unitPrice: Decimal128("39.90"), quantity: 1 },
      { productId: NumberLong(2), productNameSnapshot: "Database Architecture", unitPrice: Decimal128("49.90"), quantity: 1 }
    ]
  },
  {
    _id: NumberLong(2), customerId: NumberLong(1), idempotencyKey: "checkout-002",
    status: "PENDING", totalAmount: Decimal128("44.90"), createdAt: daysAgo(1),
    items: [
      { productId: NumberLong(3), productNameSnapshot: "Spring Boot 4", unitPrice: Decimal128("44.90"), quantity: 1 }
    ]
  },
  {
    _id: NumberLong(3), customerId: NumberLong(2), idempotencyKey: "checkout-003",
    status: "PAID", totalAmount: Decimal128("49.90"), createdAt: daysAgo(2),
    items: [
      { productId: NumberLong(2), productNameSnapshot: "Database Architecture", unitPrice: Decimal128("49.90"), quantity: 1 }
    ]
  }
]);

commerce.outboxEvents.insertOne({
  _id: UUID("10000000-0000-0000-0000-000000000001"),
  aggregateType: "Order", aggregateId: "1", eventType: "OrderPaid",
  payload: { orderId: NumberLong(1), customerId: NumberLong(1), totalAmount: Decimal128("89.80") },
  occurredAt: now, publishedAt: null
});
