/**
 * ezFlowMoney - Supabase Authentication & Cloud Database Service
 * Provides free Cloud User Auth (Email/Google), Multi-user separation (RLS), and Family Budget Sync
 */

class SupabaseService {
  static CLIENT_URL_KEY = 'ezflowmoney_supabase_url';
  static CLIENT_ANON_KEY = 'ezflowmoney_supabase_anon_key';

  static client = null;
  static currentUser = null;
  static subscriptionChannel = null;

  /**
   * Khởi tạo Supabase Client
   */
  static init() {
    const url = this.getUrl();
    const key = this.getAnonKey();

    if (url && key && window.supabase) {
      try {
        this.client = window.supabase.createClient(url, key, {
          auth: {
            persistSession: true,
            autoRefreshToken: true,
            detectSessionInUrl: true
          }
        });

        // Lắng nghe thay đổi trạng thái đăng nhập
        this.client.auth.onAuthStateChange((event, session) => {
          this.currentUser = session?.user || null;
          if (window.onAuthChangeCallback) {
            window.onAuthChangeCallback(this.currentUser);
          }
        });

        // Kiểm tra session hiện tại
        this.client.auth.getSession().then(({ data: { session } }) => {
          this.currentUser = session?.user || null;
          if (window.onAuthChangeCallback) {
            window.onAuthChangeCallback(this.currentUser);
          }
        });

        return true;
      } catch (err) {
        console.error('Lỗi khởi tạo Supabase:', err);
      }
    }
    return false;
  }

  static isConfigured() {
    return !!(this.getUrl() && this.getAnonKey());
  }

  static DEFAULT_URL = 'https://ukosxndhvlaiahureuep.supabase.co';
  static DEFAULT_ANON_KEY = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVrb3N4bmRodmxhaWFodXJldWVwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTA3NTM4NDEsImV4cCI6MjEwNjMyOTg0MX0.zw0LyAewMSnp5JjlF84AHDHzB4ixKcOp1RewLqsCOuM';

  static getUrl() {
    return localStorage.getItem(this.CLIENT_URL_KEY) || this.DEFAULT_URL;
  }

  static getAnonKey() {
    return localStorage.getItem(this.CLIENT_ANON_KEY) || this.DEFAULT_ANON_KEY;
  }

  static saveConfig(url, key) {
    if (url) localStorage.setItem(this.CLIENT_URL_KEY, url.trim());
    if (key) localStorage.setItem(this.CLIENT_ANON_KEY, key.trim());
    return this.init();
  }

  /**
   * Đăng ký tài khoản mới bằng Email + Mật khẩu
   */
  static async signUp(email, password) {
    if (!this.client) throw new Error('Chưa cấu hình kết nối Supabase Cloud!');
    const { data, error } = await this.client.auth.signUp({
      email,
      password
    });
    if (error) throw error;
    return data;
  }

  /**
   * Đăng nhập bằng Email + Mật khẩu
   */
  static async signIn(email, password) {
    if (!this.client) throw new Error('Chưa cấu hình kết nối Supabase Cloud!');
    const { data, error } = await this.client.auth.signInWithPassword({
      email,
      password
    });
    if (error) throw error;
    this.currentUser = data.user;
    return data;
  }

  /**
   * Đăng nhập với tài khoản Google
   */
  static async signInWithGoogle() {
    if (!this.client) throw new Error('Chưa cấu hình kết nối Supabase Cloud!');
    const { data, error } = await this.client.auth.signInWithOAuth({
      provider: 'google',
      options: {
        redirectTo: window.location.origin + window.location.pathname
      }
    });
    if (error) throw error;
    return data;
  }

  /**
   * Đăng xuất
   */
  static async signOut() {
    if (this.subscriptionChannel) {
      this.client.removeChannel(this.subscriptionChannel);
      this.subscriptionChannel = null;
    }
    if (this.client) {
      await this.client.auth.signOut();
    }
    this.currentUser = null;
  }

  /**
   * Tải dữ liệu tài chính của người dùng từ Supabase Database
   */
  static async fetchUserData(userId) {
    if (!this.client) return null;
    const { data, error } = await this.client
      .from('user_finance_data')
      .select('*')
      .eq('user_id', userId)
      .maybeSingle();

    if (error && error.code !== 'PGRST116') {
      console.error('Lỗi tải dữ liệu đám mây:', error);
      throw error;
    }
    return data;
  }

  /**
   * Lưu / Đồng bộ dữ liệu tài chính lên Supabase Database
   */
  static async syncUserData(userId, email, statePayload) {
    if (!this.client || !userId) return;

    const payload = {
      user_id: userId,
      user_email: email || this.currentUser?.email || '',
      cycle: statePayload.cycle,
      fixed_bills: statePayload.fixedBills,
      goal: statePayload.goal,
      transactions: statePayload.transactions,
      assets: statePayload.assets,
      updated_at: new Date().toISOString()
    };

    const { error } = await this.client
      .from('user_finance_data')
      .upsert(payload, { onConflict: 'user_id' });

    if (error) {
      console.error('Lỗi đồng bộ lên Supabase:', error);
      throw error;
    }
  }

  /**
   * Đăng ký mã Gia Đình để 2 vợ chồng dùng chung một ví
   */
  static async updateFamilyCode(userId, familyCode) {
    if (!this.client || !userId) return;
    const { error } = await this.client
      .from('user_finance_data')
      .update({ family_code: familyCode.trim().toUpperCase() })
      .eq('user_id', userId);
    if (error) throw error;
  }

  /**
   * Đăng ký lắng nghe thời gian thực (Real-time updates)
   */
  static subscribeToRealtime(userId, onRemoteUpdate) {
    if (!this.client || !userId) return;

    if (this.subscriptionChannel) {
      this.client.removeChannel(this.subscriptionChannel);
    }

    this.subscriptionChannel = this.client
      .channel('public:user_finance_data:' + userId)
      .on('postgres_changes', {
        event: 'UPDATE',
        schema: 'public',
        table: 'user_finance_data'
      }, payload => {
        if (payload.new) {
          onRemoteUpdate(payload.new);
        }
      })
      .subscribe();
  }
}

function getAnonKey() {
  return localStorage.getItem(SupabaseService.CLIENT_ANON_KEY) || '';
}

window.SupabaseService = SupabaseService;
