# 12 - Spring Annotation Cheat Sheet

Cheat sheet để tra nhanh; đọc chương chi tiết để hiểu proxy/lifecycle/failure semantics.

## Boot và configuration

| Annotation | Ai xử lý/khi nào | Ý nghĩa |
|---|---|---|
| `@SpringBootApplication` | Boot startup | Configuration + auto-config + component scan |
| `@Configuration` | IoC container | Class khai báo bean definitions |
| `@Bean` | Configuration processing | Return value của method là bean |
| `@ConfigurationProperties` | Boot binder | Bind property group vào typed object |
| `@ConfigurationPropertiesScan` | Boot startup | Scan properties classes |
| `@Value` | Bean post-processing | Inject expression/property đơn lẻ |
| `@Profile` | Environment/bean registration | Chỉ register bean/config ở profile phù hợp |
| `@ConditionalOnProperty` | Boot condition | Register theo property |
| `@ConditionalOnMissingBean` | Boot auto-config | Register nếu application chưa có bean phù hợp |

## Components và DI

| Annotation | Ý nghĩa/cách dùng |
|---|---|
| `@Component` | Managed component tổng quát |
| `@Service` | Service role |
| `@Repository` | Persistence role + exception translation integration |
| `@Controller` | MVC controller/view |
| `@RestController` | Controller có response body mặc định |
| `@Autowired` | Inject dependency; một constructor không cần annotation |
| `@Primary` | Candidate ưu tiên cho injection đơn trị |
| `@Qualifier` | Lọc candidates bằng semantic/name qualifier |
| `@Fallback` | Candidate ưu tiên thấp hơn bean thường |
| `@Lazy` | Trì hoãn creation/inject lazy proxy |
| `@Scope` | Chọn singleton/prototype/request/session... |
| `@Order` | Sắp xếp ordered collection/chain; semantics tùy consumer |
| `@PostConstruct` | Callback sau injection |
| `@PreDestroy` | Callback trước bean destruction |

## Web MVC

| Annotation | Ý nghĩa |
|---|---|
| `@RequestMapping` | Mapping tổng quát/base path |
| `@GetMapping`/`@PostMapping`/... | Mapping theo HTTP method |
| `@PathVariable` | Bind URI path segment |
| `@RequestParam` | Bind query/form parameter |
| `@RequestHeader` | Bind header |
| `@RequestBody` | Deserialize request body |
| `@ResponseBody` | Serialize return value vào body |
| `@ResponseStatus` | Chọn response status tĩnh |
| `@RestControllerAdvice` | Global REST controller advice |
| `@ExceptionHandler` | Map exception thành response |
| `@Valid` | Trigger cascaded Bean Validation |
| `@Validated` | Spring validation groups/method validation integration |

## Transactions, async, cache và events

| Annotation | Mechanism/giới hạn chính |
|---|---|
| `@Transactional` | Thường proxy-based; self-invocation/private call caveat |
| `@EnableTransactionManagement` | Enable annotation transaction processing; Boot thường auto-config khi phù hợp |
| `@Async` | Proxy chuyển execution sang executor; task không durable |
| `@EnableAsync` | Enable async annotation processing |
| `@Scheduled` | Scheduler gọi method theo delay/rate/cron |
| `@EnableScheduling` | Enable scheduled processing |
| `@EventListener` | In-process event listener, mặc định synchronous |
| `@TransactionalEventListener` | Listener theo transaction phase |
| `@Cacheable` | Cache hit skip target method |
| `@CachePut` | Luôn chạy target rồi update cache |
| `@CacheEvict` | Xóa key/all entries |
| `@Caching` | Gom nhiều cache operations |
| `@EnableCaching` | Enable cache annotation processing |
| `@Retryable` | Spring 7 resilience proxy retry; cần enable resilient methods |
| `@ConcurrencyLimit` | Giới hạn concurrent proxy invocations |

## JPA/Jakarta Persistence

| Annotation | Ý nghĩa |
|---|---|
| `@Entity` | Persistent entity |
| `@Table` | Table mapping/index/unique constraints |
| `@Id` | Entity identifier |
| `@GeneratedValue` | ID generation strategy |
| `@Version` | Optimistic lock version |
| `@Column` | Column mapping/nullability/uniqueness |
| `@Enumerated` | Enum mapping; ưu tiên STRING trong nhiều case |
| `@Embedded`/`@Embeddable` | Value object mapped vào owner table |
| `@OneToMany`/`@ManyToOne` | Relationships; fetch/cascade/ownership phải explicit |
| `@JoinColumn` | Foreign-key column mapping |
| `@Transient` | Không persist field |
| `@Convert` | Attribute converter |
| `@MappedSuperclass` | Mapping fields inherited, không phải entity độc lập |

## Spring Data JPA

| Annotation | Ý nghĩa |
|---|---|
| `@Query` | Explicit JPQL/native query |
| `@Param` | Bind named query parameter |
| `@Modifying` | Query thực hiện update/delete |
| `@EntityGraph` | Fetch graph cho repository query |
| `@Lock` | Query lock mode |
| `@EnableJpaAuditing` | Enable auditing callbacks |
| `@CreatedDate`/`@LastModifiedDate` | Audit timestamps |
| `@CreatedBy`/`@LastModifiedBy` | Audit actor |

## Security

| Annotation | Ý nghĩa |
|---|---|
| `@EnableWebSecurity` | Enable/configure servlet web security; Boot integration thường tự thiết lập nền |
| `@EnableMethodSecurity` | Enable method authorization annotations |
| `@PreAuthorize` | Authorize trước method |
| `@PostAuthorize` | Authorize dựa cả return value; dùng thận trọng |
| `@Secured` | Role-based method security kiểu đơn giản |
| `@AuthenticationPrincipal` | Bind current principal trong controller |

## Testing

| Annotation | Scope |
|---|---|
| `@SpringBootTest` | Full Boot context |
| `@WebMvcTest` | MVC slice |
| `@DataJpaTest` | JPA slice/transaction rollback mặc định |
| `@JdbcTest` | JDBC slice |
| `@JsonTest` | JSON slice |
| `@RestClientTest` | REST client slice |
| `@MockitoBean` | Override bean bằng Mockito mock trong test context |
| `@TestConfiguration` | Extra test-only bean config |
| `@ActiveProfiles` | Active profiles cho test |
| `@DynamicPropertySource` | Dynamic container/resource properties |
| `@DirtiesContext` | Đánh dấu context bẩn; dùng ít vì làm chậm cache |
| `@Sql` | Chạy SQL setup/cleanup |

## Ba câu hỏi trước khi thêm annotation

1. Object có phải Spring bean/JPA entity/controller argument đúng lifecycle không?
2. Annotation được xử lý trực tiếp hay cần proxy/processor/enable annotation/starter?
3. Test nào chứng minh behavior và trường hợp annotation không có hiệu lực?

