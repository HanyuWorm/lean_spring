# Nguồn chính thức

Đã kiểm tra ngày **24/08/2026**. Chỉ dùng primary documentation/release notes cho phần version/changelog.

## MySQL

- [MySQL release model và version numbering](https://dev.mysql.com/doc/refman/26.7/en/mysql-releases.html)
- [Which MySQL version and distribution to install](https://dev.mysql.com/doc/refman/9.7/en/which-version.html)
- [MySQL 8.4 release notes index](https://dev.mysql.com/doc/relnotes/mysql/8.4/en/)
- [MySQL 9.7 release notes index](https://dev.mysql.com/doc/relnotes/mysql/9.7/en/)
- [What is new in MySQL 8.4](https://dev.mysql.com/doc/en/mysql-nutshell.html)
- [Upgrade from previous series to MySQL 8.4](https://dev.mysql.com/doc/refman/8.4/en/upgrading-from-previous-series.html)
- [MySQL upgrade best practices](https://dev.mysql.com/doc/refman/9.7/en/upgrade-best-practices.html)
- [MySQL 26.7 FAQ: LTS and Innovation](https://dev.mysql.com/doc/refman/26.7/en/faqs-general.html)

## PostgreSQL

- [PostgreSQL release notes index](https://www.postgresql.org/docs/release/)
- [PostgreSQL 16 release notes](https://www.postgresql.org/docs/release/16.0/)
- [PostgreSQL 17 release notes](https://www.postgresql.org/docs/17/release-17.html)
- [PostgreSQL 18 release notes](https://www.postgresql.org/docs/18/release-18.html)
- [PostgreSQL current documentation](https://www.postgresql.org/docs/)
- [Upgrading a PostgreSQL cluster](https://www.postgresql.org/docs/current/upgrading.html)

## MongoDB

- [MongoDB release notes index](https://www.mongodb.com/docs/manual/release-notes/)
- [MongoDB 7.0 release notes](https://www.mongodb.com/docs/manual/release-notes/7.0/)
- [MongoDB 8.0 release notes](https://www.mongodb.com/docs/manual/release-notes/8.0/)
- [MongoDB 8.2 release notes](https://www.mongodb.com/docs/manual/release-notes/8.2/)
- [MongoDB 8.3 release notes](https://www.mongodb.com/docs/manual/release-notes/8.3/)
- [Upgrade to MongoDB 8.0](https://www.mongodb.com/docs/manual/release-notes/8.0-upgrade-standalone/)

## Cách dùng nguồn

- Dùng release index để xác nhận current patch và support line.
- Đọc mọi intermediate release note trước upgrade; file changelog chỉ là bản đồ học.
- Với managed database, đối chiếu thêm version/feature matrix và maintenance policy của provider.
- Với preview/beta, không suy luận SLA/compatibility tương đương GA.
