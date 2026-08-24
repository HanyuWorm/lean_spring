// Chạy: mongosh ... commerce labs/queries/mongo.js

// 1. Order detail đã embed item snapshot: một aggregate read.
db.orders.findOne({ _id: NumberLong(1) });

// 2. Revenue theo customer 30 ngày, join customer để trình diễn aggregation.
db.orders.aggregate([
  {
    $match: {
      status: "PAID",
      createdAt: { $gte: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000) }
    }
  },
  { $group: { _id: "$customerId", revenue: { $sum: "$totalAmount" } } },
  { $lookup: { from: "customers", localField: "_id", foreignField: "_id", as: "customer" } },
  { $unwind: "$customer" },
  { $project: { _id: 0, customerId: "$_id", email: "$customer.email", revenue: 1 } },
  { $sort: { revenue: -1 } }
]);

// 3. Keyset pagination. Thay hai giá trị bằng row cuối page trước.
const boundaryCreatedAt = new Date();
const boundaryId = NumberLong("9223372036854775807");
db.orders.find({
  customerId: NumberLong(1),
  $or: [
    { createdAt: { $lt: boundaryCreatedAt } },
    { createdAt: boundaryCreatedAt, _id: { $lt: boundaryId } }
  ]
}).sort({ createdAt: -1, _id: -1 }).limit(20);

// 4. Plan statistics.
db.orders.find({ customerId: NumberLong(1) })
  .sort({ createdAt: -1, _id: -1 })
  .limit(20)
  .explain("executionStats");

// 5. Atomic inventory decrement; null nghĩa là không đủ stock/not found.
db.products.findOneAndUpdate(
  { _id: NumberLong(2), stock: { $gte: 3 } },
  { $inc: { stock: -3, version: NumberLong(1) } },
  { returnDocument: "after" }
);

// 6. Query order chứa product. Multikey index có thể thêm nếu đây là hot path:
// db.orders.createIndex({ "items.productId": 1, createdAt: -1 })
db.orders.find({ "items.productId": NumberLong(2) })
  .sort({ createdAt: -1 });

// 7. Unpublished outbox. Standalone lab không hỗ trợ transaction/change stream.
db.outboxEvents.find({ publishedAt: null })
  .sort({ occurredAt: 1, _id: 1 })
  .limit(100);
