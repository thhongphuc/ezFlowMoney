# 🚀 Hướng Dẫn Triển Khai ezFlowMoney Lên Web Online Miễn Phí (Có Phân Quyền & Cloud Sync)

Tài liệu này hướng dẫn bạn cách đưa ứng dụng web **ezFlowMoney** lên mạng internet hoàn toàn **MIỄN PHÍ 100% TRỌN ĐỜI**, có đầy đủ tính năng:
1. **Đăng nhập / Đăng ký**: Hỗ trợ Email/Mật khẩu và Google Authentication.
2. **Phân quyền người dùng (User Authorization)**: Mỗi người dùng có một cơ sở dữ liệu tài chính riêng biệt, được bảo mật ở tầng database nhờ **Row Level Security (RLS)** của PostgreSQL.
3. **Chia sẻ ví Gia Đình**: Hai vợ chồng chỉ cần nhập chung một **Mã Gia Đình bí mật** là có thể dùng chung một ví và tự động đồng bộ thời gian thực (Real-time).
4. **Offline-First**: Mất mạng vẫn xem và chi tiêu bình thường, khi có mạng app tự đồng bộ lên đám mây.

---

## 🛠️ Công Nghệ Sử Dụng (Miễn Phí 100%)
- **Frontend Hosting**: **Vercel** (hoặc GitHub Pages / Cloudflare Pages / Netlify) - Miễn phí không giới hạn băng thông và có sẵn HTTPS/SSL tự động.
- **Backend & Database**: **Supabase** (BaaS mã nguồn mở hàng đầu) - Miễn phí 500MB PostgreSQL, 50,000 người dùng hoạt động/tháng (MAU), cơ chế bảo mật RLS và Real-time WebSockets.

---

## BƯỚC 1: KHỞI TẠO BACKEND SUPABASE (Mất khoảng 2 phút)

1. Truy cập [https://supabase.com](https://supabase.com) và bấm **Start your project** (Đăng nhập nhanh bằng tài khoản GitHub hoặc Google).
2. Bấm **New Project**:
   - **Name**: `ezflowmoney` (hoặc tên tùy thích).
   - **Database Password**: Nhập mật khẩu quản trị DB của bạn (ghi nhớ lại).
   - **Region**: Chọn **Singapore (ap-southeast-1)** để có tốc độ truy cập từ Việt Nam nhanh nhất.
   - Bấm **Create new project** và đợi khoảng 1-2 phút để Supabase chuẩn bị máy chủ.
3. **Chạy Script Khởi Tạo Database & Phân Quyền (RLS)**:
   - Ở thanh menu bên trái của Supabase Dashboard, bấm vào biểu tượng **SQL Editor** (icon `>_`).
   - Bấm **New query**.
   - Mở file [`supabase_setup.sql`](./supabase_setup.sql) trong thư mục dự án này, copy toàn bộ nội dung dán vào ô soạn thảo.
   - Bấm nút **Run** (màu xanh lá cây) ở góc dưới bên phải.
   - Khi hiện thông báo *"Success. No rows returned"*, database và các chính sách phân quyền RLS đã được kích hoạt hoàn tất!
4. **Lấy API Keys để kết nối**:
   - Bấm vào icon bánh răng **Project Settings** (ở góc dưới cùng menu bên trái) &rarr; chọn **API**.
   - Copy 2 thông số:
     - **Project URL** (dạng `https://xxxxxxxxxxxx.supabase.co`)
     - **Project API keys -> anon (public)** (dạng chuỗi JWT dài bắt đầu bằng `eyJhbGciOiJIUz...`)

---

## BƯỚC 2: DEPLOY WEB LÊN VERCEL (Mất khoảng 1 phút)

Có 2 cách cực kỳ đơn giản để đưa web lên Vercel:

### Cách A: Deploy qua GitHub (Khuyên dùng - Tự động cập nhật khi đổi code)
1. Đẩy mã nguồn thư mục `c:\Projects\ezFlowMoney` lên kho lưu trữ **GitHub** của bạn.
2. Truy cập [https://vercel.com](https://vercel.com) và đăng nhập bằng tài khoản GitHub.
3. Bấm **Add New...** &rarr; **Project**.
4. Chọn repository `ezFlowMoney` vừa tạo.
5. Ở phần cấu hình:
   - **Framework Preset**: Chọn `Other`.
   - **Root Directory**: Để trống (nhờ file `vercel.json` và `index.html` đã được cấu hình tự động trỏ vào `/web`).
6. Bấm nút **Deploy**.
7. Sau 30 giây, Vercel sẽ cung cấp cho bạn một tên miền miễn phí dạng: `https://ezflowmoney-xxx.vercel.app`!

### Cách B: Deploy trực tiếp bằng lệnh (Vercel CLI - Không cần GitHub)
Nếu bạn đã cài sẵn Node.js trên máy tính:
```bash
# Cài đặt Vercel CLI (nếu chưa có)
npm install -g vercel

# Chạy deploy ngay tại thư mục dự án
cd c:\Projects\ezFlowMoney
vercel
```
Làm theo hướng dẫn bấm Enter (Yes) các bước mặc định, bạn sẽ nhận được ngay link web online công khai trong vài chục giây.

---

## BƯỚC 3: KẾT NỐI VÀ SỬ DỤNG PHÂN QUYỀN

1. Mở đường link web vừa deploy trên trình duyệt điện thoại hoặc máy tính (hoặc chạy thử trên máy tại `http://localhost:8080/web/index.html`).
2. Trên thanh menu trên cùng, bấm nút **Đăng nhập** (hoặc vào tab **Cài đặt & Sao lưu** &rarr; bấm **Kết nối Supabase**):
   - Nhập **Supabase Project URL** và **Anon Key** đã lấy ở Bước 1.
   - Bấm **Lưu & Kích Hoạt**.
3. Cửa sổ **Đăng Nhập / Đăng Ký** sẽ hiện ra:
   - Chuyển sang tab **Đăng ký**: Nhập Email của bạn và Mật khẩu &rarr; Bấm **Tạo Tài Khoản**.
   - Sau đó chuyển sang tab **Đăng nhập** và đăng nhập vào ứng dụng.
4. **Trải nghiệm phân quyền & lưu trữ riêng biệt**:
   - Khi bạn đăng nhập, góc trên bên phải sẽ hiển thị huy hiệu tài khoản email của bạn kèm icon Đám mây xanh.
   - Toàn bộ ngân sách, thu nhập 42 Tr, các hóa đơn 17 Tr và 3 Lọ tích sản sẽ được lưu riêng cho tài khoản của bạn trên Supabase PostgreSQL.
   - Người khác mở link web của bạn sẽ chỉ thấy dữ liệu mặc định của họ hoặc tài khoản của riêng họ, **tuyệt đối không thể xem trộm** số tiền của bạn nhờ Row Level Security.

---

## 👨‍👩‍👧‍👦 TÍNH NĂNG ĐẶC BIỆT: CHIA SẺ VÍ GIA ĐÌNH (VỢ / CHỒNG DÙNG CHUNG)

Bạn muốn vợ và chồng cùng cài app trên điện thoại riêng và dùng chung một ngân sách chi tiêu?
1. Vợ tạo tài khoản riêng (VD: `vo@gmail.com`).
2. Chồng tạo tài khoản riêng (VD: `chong@gmail.com`).
3. Cả hai người mở menu Tài khoản (bấm vào icon Avatar ở góc phải trên) &rarr; tìm mục **Chia sẻ ngân sách Gia Đình**.
4. Cả hai cùng nhập một **Mã Gia Đình bí mật** giống nhau (Ví dụ: `HẠNHPHUC2026`) &rarr; Bấm **Lưu Mã**.
5. **Kỳ diệu:** Nhờ chính sách RLS `family_code` và cơ chế Supabase Real-time, mỗi khi chồng quẹt thẻ chi tiêu hoặc vợ bấm tích dấu đã trả hóa đơn, điện thoại của người kia sẽ tự động nhảy số biến động số dư tức thì!

---

## 📱 CÀI ĐẶT LÀM APP DI ĐỘNG (PWA)
Sau khi đưa lên web HTTPS của Vercel:
- **Trên iPhone (Safari)**: Bấm nút Share (Chia sẻ) &rarr; chọn **Thêm vào MH chính (Add to Home Screen)**.
- **Trên Android (Chrome)**: Bấm menu 3 chấm &rarr; chọn **Cài đặt ứng dụng (Install App)**.
Ứng dụng sẽ hoạt động full màn hình như một ứng dụng Native, có icon trên màn hình chính và hoạt động mượt mà ngay cả khi không có mạng internet.
