-- ==============================================================================
-- EZFLOWMONEY - SUPABASE CLOUD DATABASE SETUP & ROW LEVEL SECURITY (RLS)
-- ==============================================================================
-- Hướng dẫn:
-- 1. Đăng ký tài khoản miễn phí tại https://supabase.com
-- 2. Tạo một Project mới (chọn Region Singapore để tốc độ nhanh nhất)
-- 3. Vào mục "SQL Editor" ở menu bên trái -> Bấm "New query"
-- 4. Dán toàn bộ nội dung script này vào và bấm "Run"
-- ==============================================================================

-- 1. Bảng lưu trữ dữ liệu tài chính của người dùng (User Finance Data)
create table if not exists public.user_finance_data (
    user_id uuid references auth.users(id) on delete cascade not null primary key,
    user_email text,
    family_code text, -- Mã gia đình dùng để vợ chồng chia sẻ chung 1 ngân sách
    cycle jsonb not null default '{
        "totalIncome": 42000000,
        "savingsTarget": 12000000,
        "fixedBills": 17000000,
        "variablePool": 13000000,
        "month": 9,
        "year": 2026,
        "daysRemaining": 22,
        "daysInMonth": 30
    }'::jsonb,
    fixed_bills jsonb not null default '[
        {"id": "rent", "name": "Tiền thuê nhà", "amount": 8000000, "isPaid": true, "dueDate": "Hạn ngày 05 hàng tháng", "icon": "fa-house"},
        {"id": "tuition", "name": "Học phí 2 bé (Mầm non & Tiểu học)", "amount": 7500000, "isPaid": false, "dueDate": "Hạn ngày 10 hàng tháng", "icon": "fa-graduation-cap"},
        {"id": "utilities", "name": "Điện nước sinh hoạt (EVN & Cấp nước)", "amount": 1500000, "isPaid": false, "dueDate": "Hạn ngày 15 hàng tháng", "icon": "fa-bolt"}
    ]'::jsonb,
    goal jsonb not null default '{
        "birthYear": 1994,
        "targetAge": 40,
        "targetAmount": 600000000,
        "expectedAnnualReturn": 8.0
    }'::jsonb,
    transactions jsonb not null default '[]'::jsonb,
    assets jsonb not null default '[
        {"id": 1, "bucket": "BANK_SAVINGS", "name": "Sổ tiết kiệm VCB 6 tháng (5.2%/năm)", "units": 1, "unitPrice": 120000000, "total": 120000000, "label": "Sổ"},
        {"id": 2, "bucket": "GOLD_9999", "name": "Vàng nhẫn trơn 9999 PNJ", "units": 6, "unitPrice": 8200000, "total": 49200000, "label": "Chỉ"},
        {"id": 3, "bucket": "FUND_NAV", "name": "Chứng chỉ quỹ Dragon Capital DCDS", "units": 1850, "unitPrice": 28400, "total": 52540000, "label": "CCQ"}
    ]'::jsonb,
    updated_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- 2. Kích hoạt Row Level Security (RLS) để phân quyền chặt chẽ từng User
alter table public.user_finance_data enable row level security;

-- 3. Tạo chính sách bảo mật (Security Policies):
-- Người dùng chỉ được xem/sửa dữ liệu của chính mình (hoặc gia đình có chung mã family_code)

-- Policy: Cho phép người dùng đọc dữ liệu của mình (hoặc người cùng family_code)
create policy "Users can select own data or family data" 
on public.user_finance_data for select 
using (
    auth.uid() = user_id 
    or (
        family_code is not null 
        and family_code = (select u.family_code from public.user_finance_data u where u.user_id = auth.uid())
    )
);

-- Policy: Cho phép người dùng thêm mới bản ghi của chính mình
create policy "Users can insert own data" 
on public.user_finance_data for insert 
with check (auth.uid() = user_id);

-- Policy: Cho phép người dùng cập nhật dữ liệu của mình (hoặc cùng family_code)
create policy "Users can update own data or family data" 
on public.user_finance_data for update 
using (
    auth.uid() = user_id 
    or (
        family_code is not null 
        and family_code = (select u.family_code from public.user_finance_data u where u.user_id = auth.uid())
    )
);

-- Policy: Cho phép người dùng xóa dữ liệu của mình
create policy "Users can delete own data" 
on public.user_finance_data for delete 
using (auth.uid() = user_id);

-- 4. Bật tính năng Real-time để khi vợ/chồng thêm giao dịch, máy người kia tự cập nhật ngay lập tức
alter publication supabase_realtime add table public.user_finance_data;

-- 5. Tự động khởi tạo dữ liệu mẫu khi người dùng mới đăng ký tài khoản (Trigger)
create or replace function public.handle_new_user()
returns trigger as $$
begin
    insert into public.user_finance_data (user_id, user_email)
    values (new.id, new.email);
    return new;
end;
$$ language plpgsql security definer;

-- Gắn trigger vào bảng auth.users
drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
    after insert on auth.users
    for each row execute procedure public.handle_new_user();

-- Hoàn tất thiết lập!
