# Hồ sơ học tập của chủ skill

Ưu tiên mới ngày **2026-11-12**: người dùng xác nhận chuyên tâm Kotlin, muốn trở lại ngôn ngữ nhanh và có mentor chỉ đường. Flutter là kinh nghiệm tham chiếu. Xem [hồ sơ tổng hợp mới](../../kotlin-mentor/references/learner-profile.md) và [tiến độ](../../kotlin-mentor/references/progress.md) nếu có gói kotlin-mentor cạnh skill này. Nội dung khảo sát 2026-11-04 dưới đây được giữ làm lịch sử; chưa mở lại các repo trong lần cập nhật này.

Cập nhật: **2026-11-04**. Đây là hồ sơ để chọn cách giảng, không phải chứng nhận trình độ. Dùng phản hồi mới của người dùng để điều chỉnh; không cố định họ ở trạng thái được mô tả hôm nay.

## Thông tin người dùng đã xác nhận

- Chỗ vướng hiện tại: **“Chưa chắc cú pháp, API và khái niệm nền tảng.”** Vì vậy, có code kiến trúc nhiều lớp không phải lý do bỏ qua cú pháp hoặc cơ chế cơ bản trong lời giải thích.
- Các project đầu lòng tự viết phần lớn để làm mốc Kotlin: **Customer, Customer2, Customer3**. Các bản tìm thấy tương ứng là `Customer`, `customer_v2`, `customer_v3` dưới `D:/AppData/Code/Prodech/adroi/`. Người dùng xác nhận tên mốc; đường dẫn v2/v3 được đối chiếu theo nội dung và tên dự án, chưa có xác nhận riêng về từng bản sao.
- Mốc Flutter tự viết phần lớn, đã xác nhận chính xác đường dẫn: **`D:/AppData/Code/Prodech/adroi/fahion`**. Không thay bằng `Project/Android/fashion_app` hoặc `Flutter_second/fashionapp/fashionapp`; đó là những project khác.
- Hiện tại đang **follow base `android-showcase` để làm dự án**. Đọc nó như kiến trúc đang học, không như bằng chứng đã tự thiết kế hoặc hiểu hết mọi abstraction trong đó.
- Có thực hành Kotlin và Flutter; đã nói đang code cả React Native, có thể học thêm Swift. Chưa có xác nhận về độ tự chủ của từng phần React Native hoặc kinh nghiệm Swift.
- Mục tiêu của skill: khi mở một codebase chưa quen, người dùng có thể bắt đầu bằng một câu đơn giản, hiểu code đủ rõ và biết cách tự đọc tiếp.

## Cách giải thích nên ưu tiên

1. Cho bức tranh ngắn: tính năng đang làm gì, các phần nào tham gia. Chưa mở đầu bằng danh sách pattern, thư viện và thuật ngữ chưa giải nghĩa.
2. Chọn một thao tác thật và một giá trị cụ thể, ví dụ đổi username hoặc bấm tăng số lượng sản phẩm. Dẫn qua code theo thứ tự sự kiện xảy ra.
3. Tại đoạn khó, tháo chữ ký và biểu thức: tên nào do tác giả đặt, phần nào là kiểu/keyword/operator, tham số nào được truyền, lambda/function reference đã chạy chưa, giá trị nào đổi. Lấy số dòng từ file đang đọc.
4. Giải thích API làm gì trong tình huống này, ai kích hoạt nó và khi nào consumer nhận kết quả. Phân biệt cơ chế thư viện với helper của base, cũng như compile-time type với runtime behavior.
5. Sau khi người đọc thấy một hành vi cụ thể, nối nó với vai trò rộng hơn như ViewModel, Bloc, use case, repository, DI hoặc reducer. Giải thích vấn đề abstraction giải quyết và đường gọi thật; không chỉ đưa định nghĩa.
6. Chỉ ôn phần liên quan và chưa chắc. Có thể thêm một câu tự kiểm tra hoặc một thay đổi nhỏ để người đọc tự thử; chưa tự sửa code và không giữ phần giải thích tiếp theo làm “phần thưởng” cho việc trả lời.

Có thể nói “ở Customer/fahion đã có ví dụ tương tự” và mở lại đúng file khi cần. Không nói “cái này bạn biết rồi” chỉ từ việc tìm được nó trong project. Không dùng số project, số dependency hoặc độ dài source để xếp hạng trình độ.

## Mốc Kotlin đã khảo sát

Các đường dẫn dưới đây là nguồn tham khảo ngoài gói, lấy mẫu tĩnh ngày cập nhật. Đọc lại nếu cần trích code/số dòng vì file có thể thay đổi.

| Mốc thực tế | Bằng chứng đã đọc | Dùng để mở nghĩa |
| --- | --- | --- |
| Customer: listener gọi ViewModel | [LoginFragment.kt:136](D:/AppData/Code/Prodech/adroi/Customer/app/src/main/java/com/uzuu/customer/feature/start/login/LoginFragment.kt:136) | Ai gọi callback; truyền giá trị khác với truyền một hàm. Nullable/getter/delegation có trong file, cần giải thích tại chỗ nếu còn vướng. |
| Customer: state, event và request | [LoginViewModel.kt:25](D:/AppData/Code/Prodech/adroi/Customer/app/src/main/java/com/uzuu/customer/feature/start/login/LoginViewModel.kt:25), [onClickLogin:43](D:/AppData/Code/Prodech/adroi/Customer/app/src/main/java/com/uzuu/customer/feature/start/login/LoginViewModel.kt:43) | `update { it.copy(...) }`, `launch`, `return@launch`, `when/is`; state để render khác với event navigation/Toast. Đây là code đã tiếp xúc, chưa xác nhận hiểu chắc các ký hiệu. |
| customer_v2: màn hình login bằng Compose | [LoginScreen.kt:58](D:/AppData/Code/Prodech/adroi/customer_v2/app/src/main/java/com/uzuu/customer/feature/start/login/LoginScreen.kt:58), [state/effect:66](D:/AppData/Code/Prodech/adroi/customer_v2/app/src/main/java/com/uzuu/customer/feature/start/login/LoginScreen.kt:66) | So sánh Fragment cập nhật view với Compose đọc state; giải nghĩa `by`, `remember`, `collectAsStateWithLifecycle`, `LaunchedEffect` theo vòng đời thực tế. |
| customer_v3: phần đăng ký đang viết dở | [RegisterViewModel.kt:13](D:/AppData/Code/Prodech/adroi/customer_v3/app/src/main/java/com/uzuu/customer_v3/feature/auth/register/RegisterViewModel.kt:13), [onRegister:30](D:/AppData/Code/Prodech/adroi/customer_v3/app/src/main/java/com/uzuu/customer_v3/feature/auth/register/RegisterViewModel.kt:30) | `@Inject`/constructor có dependency không đồng nghĩa đã gọi nó. `onUsernameChange` đổi state; các handler password và đăng ký còn rỗng/comment ở lần khảo sát. Không dựng luồng đăng ký hoàn tất. |

## Mốc Flutter đã khảo sát: đúng project fahion

Project có code Bloc, base page, model generated, use case/repository và kết nối data. Những thứ này chỉ xác nhận **đã có trong project được người dùng nhận là tự viết phần lớn**, không xác nhận người dùng tự viết hoặc nhớ chắc từng lớp.

| Mốc thực tế | Bằng chứng đã đọc | Dùng để mở nghĩa |
| --- | --- | --- |
| Callback từ product grid lên page | [home_product_grid.dart:17](D:/AppData/Code/Prodech/adroi/fahion/app/lib/ui/home/widgets/home_product_grid.dart:17), [home_page.dart:230](D:/AppData/Code/Prodech/adroi/fahion/app/lib/ui/home/home_page.dart:230) | Kiểu của callback, lambda giữ product, nơi gọi callback, event toggle favorite hoặc navigation. Không coi truyền lambda là thực thi ngay. |
| Tăng/giảm số lượng giỏ hàng | [cart_page.dart:235](D:/AppData/Code/Prodech/adroi/fahion/app/lib/ui/cart/cart_page.dart:235), [cart_bloc.dart:71](D:/AppData/Code/Prodech/adroi/fahion/app/lib/ui/cart/bloc/cart_bloc.dart:71) | Nút tạo event với delta, handler tìm item và kiểm tra quantity, `await` use case, `List.from`/`copyWith`/`emit`, consumer render quantity. Tách từng phép đổi dữ liệu, không nói gộp “Bloc cập nhật UI”. |
| Generic/base page và wiring | [base_page_state.dart:10](D:/AppData/Code/Prodech/adroi/fahion/app/lib/base/base_page_state.dart:10), [khởi tạo Bloc:33](D:/AppData/Code/Prodech/adroi/fahion/app/lib/base/base_page_state.dart:33) | `T/B` là tham số kiểu gì, `late final`, cascade `..`, lấy instance từ GetIt và cung cấp Bloc ở đâu. Tách cú pháp Dart, API thư viện và base tự viết. |
| State generated và consumer | [home_state.dart:9](D:/AppData/Code/Prodech/adroi/fahion/app/lib/ui/home/bloc/home_state.dart:9), [home_page.dart:86](D:/AppData/Code/Prodech/adroi/fahion/app/lib/ui/home/home_page.dart:86) | `factory`, default/nullable, `copyWith`, `BlocBuilder/buildWhen`; declaration nào là nguồn, phần nào do generator tạo và điều kiện consumer được gọi. |

## Trọng tâm hiện tại: học android-showcase

Mốc base: `D:/AppData/Code/base/android-showcase`. DiuAppChat là một checkout đang phát triển riêng tại `D:/AppData/Code/Project/Android/DiuAppChat`; **không dùng nội dung base thay cho việc đọc file của checkout đang được hỏi**. Đã thấy hai bản có cách xử lý query khác nhau ở lần khảo sát.

Lộ trình gợi ý, chỉ mở từng khái niệm khi luồng cần đến:

- Bắt đầu ở một màn hình album và callback/khởi tạo của nó, xác định input và state màn hình nhận. Nối với login Customer hoặc cart fahion, nhưng không giả định cơ chế API giống nhau.
- Khi gặp [GetAlbumListUseCase.kt:8](D:/AppData/Code/base/android-showcase/feature/album/src/main/kotlin/com/igorwojda/showcase/feature/album/domain/usecase/GetAlbumListUseCase.kt:8), giải nghĩa constructor, `String?`, `Result<List<Album>>`, `suspend operator fun invoke`. Cho thấy lời gọi `getAlbumListUseCase(query)` đi tới `invoke`; `Result` ở đây được import từ code dự án, cần mở định nghĩa thay vì tự coi là Kotlin standard library.
- Theo [AlbumListViewModel.kt:29](D:/AppData/Code/base/android-showcase/feature/album/src/main/kotlin/com/igorwojda/showcase/feature/album/presentation/screen/albumlist/AlbumListViewModel.kt:29): tháo lambda, default/nullable, `when/is`, function reference hoặc coroutine/Flow operator đúng phiên bản code đang đọc. Chưa cần giảng toàn bộ coroutine trước khi hiểu một request.
- Theo [BaseViewModel.kt:25](D:/AppData/Code/base/android-showcase/feature/base/src/main/kotlin/com/igorwojda/showcase/feature/base/presentation/viewmodel/BaseViewModel.kt:25): `sendAction` gọi `reduce`, state mới đi vào cơ chế observable rồi StateFlow. Giải thích một giá trị trước/sau và nơi UI collect. Action kết quả ở base này không mặc nhiên là event người dùng của Flutter Bloc.
- Sau khi rõ lời gọi và dữ liệu, mới mở DI: ai tạo ViewModel/use case/repository, registration nào được load và constructor nhận instance ra sao. Không bắt đầu bằng danh sách annotation, scope hoặc định nghĩa Clean Architecture.

## Phạm vi chưa xác minh và cách cập nhật

- Không xác minh mức tự lực của từng đoạn, độ nhớ hiện tại, khả năng tự giải thích khi đổi tình huống, kết quả build/test hoặc hành vi trên thiết bị. Những mục này chưa có dữ liệu; không cần hỏi hết ngay.
- Các ghi chú học trong `D:/AppData/Code` có cả mục tiêu học và định hướng production khác nhau. Chúng là ngữ cảnh lịch sử; không dùng để lấn át tự báo hiện tại về nền tảng, cũng không biến quy ước của base này thành luật cho base khác.
- Khảo sát đã lấy mẫu các vùng `Project/Android`, `base`, `MobileApp`, `Flutter`, `Flutter_second`, `Kotlin` và các project người dùng vừa chỉ rõ trong `Prodech/adroi`. `ReactNative`/`DATN` mới chủ yếu được xem ở mức manifest/danh mục; chưa dùng chúng làm mốc năng lực cá nhân. Không đọc toàn bộ mọi project; không đánh giá năng lực theo danh mục thư mục.
- Khi người dùng tự mô tả hoặc tự giải thích được một khái niệm, nếu họ yêu cầu lưu hồ sơ thì ghi đúng khái niệm, ví dụ, ngày và mức xác minh. AI vừa giải thích xong không phải bằng chứng người học đã nắm.
- Khi mang sang IDE/máy khác, mọi link `D:/...` là mốc tùy chọn có thể không tồn tại. Hồ sơ vẫn đủ ngữ cảnh để chọn cách giảng; không cần có toàn bộ kho project cũ và không giả vờ đã đọc các file đó trên máy mới.
