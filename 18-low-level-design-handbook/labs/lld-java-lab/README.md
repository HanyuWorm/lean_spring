# LLD Java 21 Lab

Project Java thuần để nhìn rõ design, không để framework che object/state/concurrency boundary.

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
mvn -f 18-low-level-design-handbook/labs/lld-java-lab/pom.xml test
```

## Modules theo package

- `parking`: aggregate `ParkingLot`, encapsulated spot state và allocation Strategy.
- `vending`: explicit state machine, typed result và illegal transition.
- `reservation`: atomic `ConcurrentHashMap.compute`, version và idempotency.
- `notification`: Strategy registry + retry Decorator, phân loại transient/permanent error.

## Bài tập

1. Parking: thêm pricing theo duration và ticket lifecycle `ACTIVE→PAID→CLOSED` bằng injected `Clock`.
2. Vending: thêm `DISPENSING` và hardware port có failure/recovery.
3. Reservation: thay in-memory store bằng H2, unique constraint + optimistic `version`, viết integration race test.
4. Notification: thêm `MetricsChannel` decorator và chứng minh decorator ordering bằng test.
5. Viết application use case quanh mỗi domain package; domain không import Spring.
