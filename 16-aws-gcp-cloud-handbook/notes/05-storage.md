# 05 — Storage

## Object storage

- **Amazon S3 / Cloud Storage:** lưu object theo bucket/key, durability cao, không phải POSIX filesystem/database.
- Use case: static asset, data lake, backup, artifact, log.
- Controls: block public access/public access prevention, IAM, encryption/KMS, versioning, retention/object lock, lifecycle, replication, access/audit logs.
- Costs: GB-month, operations/request class, retrieval, early deletion và egress.

Presigned/signed URL cấp quyền tạm thời trực tiếp; giới hạn operation/object/expiry/content và không log URL. Object name không phải access control.

## Block storage

- **EBS / Persistent Disk, Hyperdisk:** volume gắn VM, filesystem/database; performance theo type/size/provisioned IOPS/throughput.
- Scope/attachment và snapshot semantics khác; design zone failure, encryption, backup consistency và detach/reattach.
- Instance/local SSD rất nhanh nhưng ephemeral; phải replicate/checkpoint.

## File storage

- **EFS / Filestore:** managed shared NFS-like filesystem.
- **FSx families / Managed Lustre options:** specialized Windows/Lustre/ONTAP workloads; GCP có service/partner tương ứng tùy workload.
- File share dễ tạo hot metadata/throughput và broad permissions; benchmark small files/concurrency.

## Storage class/lifecycle

Hot/standard → infrequent/nearline → archive/coldline theo access/retention. Archive retrieval có latency và fee; lifecycle transition/delete phải tương thích legal hold và restore RTO. Versioning chống overwrite nhưng không tự chống privileged deletion; dùng retention lock/separate backup account/project.
