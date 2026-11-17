# React Native, Expo, JavaScript và TypeScript

## Điểm vào của target

- Đọc `package.json`, workspace, script, dependency/lockfile khi cần, cấu hình app và native project liên quan. Xác định dùng Expo, React Native trực tiếp hay cách lắp khác từ cấu hình thật.
- Theo entry registration: trường `main`, entry file, `AppRegistry`, Expo/router entry hoặc cơ chế khác đang được cấu hình. Không mặc định mọi app khởi động từ `App.tsx`.
- Tìm provider/root layout, navigation container/router, màn hình đầu, guard và deep link. Với file-based router, chứng minh convention đang được bật bằng entry/config thay vì chỉ nhìn thư mục.
- Phân biệt JS/TS dùng chung với file theo platform và mã Android/iOS. Chỉ kết luận platform nào dùng implementation nào sau khi kiểm tra resolver/config và file tương ứng nếu có.

## Từ sự kiện đến render tiếp theo

Theo interaction → handler → hook/action/thunk/query/service đang dùng → I/O nếu có → state/cache update → component subscribe/select → render. Một feature có thể dùng local state, Context, Redux, Zustand hoặc query cache; xác định bằng call site thực tế và giải thích ranh giới giữa chúng.

- Với hook, phân biệt render phase, event handler và effect. Đọc dependency array, cleanup và closure khi chúng làm thay đổi thời điểm/giá trị; không khẳng định effect chạy đúng một lần ở mọi môi trường.
- Chỉ ra state thuộc component, provider/store hay server cache. Props đi từ đâu, update đi qua setter/dispatch/mutation nào, consumer nào nhận thay đổi? Không diễn giải setter như lệnh lập tức thay mọi biến trong handler hiện tại.
- Với TypeScript, tách type/interface/generic dùng lúc kiểm tra tĩnh khỏi validation/runtime mapping. Một `interface` hoặc type assertion không tự kiểm tra dữ liệu API khi chạy.
- Theo Promise/`async`/`await`, lỗi, loading và hủy/cleanup nếu có. Đọc request wrapper/interceptor, storage và cache invalidation thực tế; không suy ra retry, token refresh hoặc request cancellation từ tên thư viện.
- Nếu native module/TurboModule/JSI/bridge hoặc Expo module tham gia, xác minh API JS, registration/config và implementation native có trong repo. Không gọi mọi giao tiếp native là cùng một bridge; nêu phần chưa thấy rõ.
- Phân biệt hành vi development, hot reload và production khi nó ảnh hưởng kết luận. Không suy ra hiệu năng luồng JS/UI từ việc nhìn một hàm async.

## Bằng chứng và lộ trình

Đi từ entry/root provider → router/screen → handler/custom hook → store/query/service → model/adapter → consumer/test. Một component test với service mock chỉ chứng minh contract trong môi trường mock, không xác minh backend hoặc native module thật. Giải thích mock boundary khi dùng test làm ví dụ và kiểm tra nơi đăng ký màn hình/provider để tránh giảng code chưa được sử dụng.
