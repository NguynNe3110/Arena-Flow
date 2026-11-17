# Lộ trình trở lại Kotlin theo bằng chứng

Đây là thứ tự đề xuất dựa trên tài liệu cá nhân, không phải kết luận về trình độ hoặc lịch hoàn tất. Mỗi chặng có thể rút ngắn nếu người học tự làm và lý giải được biến thể. Chỉ đưa chặng đang học và bước kế tiếp trong buổi thường ngày; khi người dùng muốn toàn cảnh mới mở bảng này.

## Trục chính

| Chặng | Vấn đề và nội dung | Dấu hiệu đủ để chuyển |
|---|---|---|
| K0 — Lấy lại cảm giác code | Model Ticket/User; `val/var`, nullable, named/default args, `copy`, chữ ký hàm, callback | Dự đoán trước/sau, sửa một field và giải thích object nào đổi; biết truyền hàm khác gọi hàm |
| K1 — Đọc và viết hàm Kotlin | Function type, lambda/trailing lambda, `it`/receiver, `::`, extension; `map/filter/firstOrNull`; scope functions chỉ khi cần | Tự viết tìm/lọc/cập nhật một item, chỉ được ai gọi lambda và giá trị trả về; không cần học thuộc 5 scope function cùng lúc |
| K2 — Mô hình và contract | Interface/implementation, visibility, sealed + `when`, generic `<T>`/constraint, `object`/companion, `operator invoke`, delegation `by` | Tự dựng kết quả success/error đơn giản, thay kiểu generic và giải thích contract; phân biệt stdlib Result với Result riêng của base |
| A1 — State xuyên suốt | Một user action → ViewModel → `UiState.copy` → StateFlow → UI; local state và state owner | Tự trace và thay đổi một hành vi nhỏ, không expose mutable state để UI sửa tùy tiện |
| A2 — Bất đồng bộ có chủ sở hữu | Request fake trước network thật; `suspend`, `launch`, `withContext`, scope/job, cancellation, error; cold/hot, collect, StateFlow | Giải thích ai sở hữu task, khi nào hủy; phân biệt suspend với background; giữ cancellation khi xử lý lỗi |
| A3 — Dữ liệu và kiểm chứng | Constructor injection, repository/DTO/domain; fake repository → Retrofit/Room theo nhu cầu; loading/empty/error; test hàm/mapper/state transition | Tự thay fake bằng data source phù hợp, kiểm chứng success/error; migration khi thay schema, không xóa DB như lời giải mặc định |
| A4 — UI và lifecycle | Compose state/hoisting/recomposition, lifecycle-aware collection, effect keys, navigation, SavedStateHandle/process recreation khi cần | Giải thích điều gì sống qua recomposition/rotation/process death; tránh request lặp và xác định event có thể mất/phát lại |
| A5 — Tích hợp và tự lực | Một tính năng list/detail/search có cache; DI theo repo, test coroutine/Flow; debug từ symptom về source | Tự thêm một biến thể không có mẫu, mô tả lỗi và chọn công cụ kiểm chứng; chỉ mở framework/base abstraction khi có nhu cầu |

Nếu đang học dự án thật, xen chặng Android vào khi nó làm rõ vấn đề hiện tại; không buộc học xong mọi chi tiết K2 mới được làm UI. Nếu chưa có project, giữ miền Ticket, khởi đầu bằng hàm thuần và fake data; chưa cần mở project mới hoặc cài bộ thư viện chỉ để ôn cú pháp.

## Bài K0 sẵn để bắt đầu

Mục tiêu: đọc được model, nullable/default args và sự khác nhau giữa truyền hàm với gọi hàm. Ví dụ minh họa, chưa phải code đã chạy trong project:

```kotlin
data class Ticket(val title: String, val note: String? = null)

fun rename(ticket: Ticket, transform: (String) -> String): Ticket {
    return ticket.copy(title = transform(ticket.title))
}

fun main() {
    val original = Ticket(title = "  Kotlin  ")
    val updated = rename(original) { text -> text.trim() }
    println(original.title.length)
    println(updated.title)
    println(updated.note ?: "Chưa có ghi chú")
}
```

Giảng một luồng: `original` được truyền vào `rename`; lambda là giá trị hàm cho tham số `transform`; chỉ khi `transform(ticket.title)` chạy mới gọi `trim`; `copy` tạo Ticket mới, không gán lại `original`. Giải nghĩa `(String) -> String`, `text`, `->`, trailing lambda và `?:` ngay tại chỗ, không mở cả khóa functional programming.

Chẩn đoán ngắn: mời dự đoán ba dòng output và vì sao `val original` vẫn giữ title có khoảng trắng. Nếu đã đưa lời giải ví dụ, không tính việc lặp lại output đó là bằng chứng tự lực. Bài tự làm dùng biến thể: viết `displayTitle(ticket)` trả về title đã trim; nếu rỗng thì trả `"Chưa có tiêu đề"`; giữ nguyên ticket đầu vào. Kiểm tra `" Kotlin "`, `"   "` và giải thích giá trị gốc sau lời gọi. Chưa tự đưa đáp án biến thể nếu người học muốn luyện.

Nếu quá dễ, chuyển ngay sang cập nhật một Ticket trong `List<Ticket>` bằng id và callback; nếu kẹt type/lambda, tách `transform` thành một biến hàm có kiểu rõ ràng rồi quay lại `rename`. Mỗi nhánh vẫn chỉ thay một phần nhỏ.

## Bản đồ khám phá phần còn thiếu bằng chứng

“Chưa có bằng chứng trong kho” có thể là đã học ở nơi khác; dùng câu hỏi/bài nhỏ để xác minh trước khi gọi là kiến thức mới với người học.

| Ưu tiên | Chủ đề ứng viên | Khi nào mở và bài thử |
|---|---|---|
| Cần ngay nếu đọc code còn vướng | Lambda/receiver/function reference, generics cơ bản, sealed/when, collection transformations, delegation cơ bản | Đọc một chữ ký use case, viết lại lambda tường minh và tự đổi model |
| Cần khi làm async | Structured concurrency, cancellation, exception propagation, hot/cold, `stateIn`, `SharingStarted`, `combine`/`flatMapLatest` | Search fake có độ trễ khác nhau; hủy query cũ và lý giải state cuối; không nhảy vào operator khi chưa rõ collect/scope |
| Cần khi làm app hoàn chỉnh | Test, lifecycle/process death, Room migration, source of truth, cache refresh, DI scope | Viết kiểm chứng cho request lỗi hoặc migration, trace object nào sống theo màn hình/app |
| Sau khi có tình huống cụ thể | Equality/hashCode, shallow copy + nested mutable collections, read-only vs immutable; variance `in/out`, star projection, inline/reified | Cache key, sửa list lồng trong data class, repository generic hoặc đọc base khó; nguồn hiện tại chưa đủ chứng minh đã hiểu |
| Tùy mục tiêu | Java interoperability/platform types, value class, Sequence vs collection; Paging, WorkManager, profiling, accessibility, modularization | Chọn khi có API Java, identity/type safety, list lớn, background task, lỗi hiệu năng hoặc nhu cầu sản phẩm |
| Hoãn cho đến khi có nhu cầu | KMP/backend Kotlin, compiler internals, advanced DSL/reflection/metaprogramming | Không mở chỉ vì mới lạ; giải thích nhu cầu và tiền đề nếu người dùng chủ động muốn khám phá |

## Hiệu chỉnh các ghi chú kỹ thuật cũ

- `val` ngăn gán lại reference; `List` read-only và data class `copy` không đảm bảo bất biến sâu.
- `suspend` không tự chuyển thread; coroutine không đồng nghĩa thread; thư viện hỗ trợ suspend có thể đã tự bảo đảm main-safety. Chọn dispatcher theo công việc thật.
- ViewModel sống qua configuration change trong scope phù hợp, không tự lưu tất cả qua process death. `remember` khác `rememberSaveable` và persistence.
- StateFlow giữ state hiện tại; SharedFlow không tự bảo đảm “đúng một lần” qua collector/lifecycle. Tách ý nghĩa state/event, rồi quyết định cách chuyển giao theo yêu cầu. Loading/error có thể là dữ liệu render trong UiState. Giữ cách của repo khi giải thích, nêu tradeoff khi thiết kế bài mới.
- Repository là một ranh giới dữ liệu hữu ích; không bắt buộc mọi ví dụ hàm thuần phải có 5 tầng hoặc Base class. DI không chỉ là “tránh new”; cần biết ai tạo object, cung cấp và sở hữu vòng đời nó.

## Nguồn chính thức để tra đúng lúc

Đã đối chiếu các trang lambda, coroutine basics và Android UI events khi xây skill ngày 2026-11-12; các trang còn lại là điểm tra cứu, cần mở khi dùng. Không coi nội dung hay phiên bản thư viện là cố định.

- Kotlin language: https://kotlinlang.org/docs/home.html
- Function types/lambda/receiver: https://kotlinlang.org/docs/lambdas.html
- Generics/variance: https://kotlinlang.org/docs/generics.html
- Coroutines basics: https://kotlinlang.org/docs/coroutines-basics.html
- Cancellation: https://kotlinlang.org/docs/cancellation-and-timeouts.html
- Flow: https://kotlinlang.org/docs/flow.html
- Android UI events và quyết định state: https://developer.android.com/topic/architecture/ui-layer/events
- Lifecycle + coroutine: https://developer.android.com/topic/libraries/architecture/coroutines
- Compose state: https://developer.android.com/develop/ui/compose/state

Đây là nguồn cho mentor kiểm tra cơ chế và phiên bản, không phải danh sách bài đọc bắt người học tự mò. Luôn chuyển kiến thức tra được thành ví dụ và bước thực hành phù hợp.
