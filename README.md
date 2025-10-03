# Todo List Website - Full Stack Application

Dự án Website To-Do List đầy đủ được xây dựng với Java Spring Boot (backend), MySQL (database), và HTML/CSS/JavaScript (frontend).

## 🎯 Tổng quan dự án

Ứng dụng Todo List này được thiết kế như một giải pháp hoàn chỉnh cho quản lý công việc cá nhân với xác thực mạnh mẽ, phân loại và các tính năng thời gian thực. Được xây dựng cho dự án khóa học đại học, nó minh họa các thực hành phát triển web hiện đại bao gồm xác thực JWT và thiết kế responsive.

## ✨ Tính năng chính

### Xác thực & Bảo mật
- **Đăng ký người dùng** với JWT Authentication
- **Đăng nhập/Đăng xuất** bảo mật
- **Mã hóa mật khẩu** với BCrypt
- **Validation đầu vào** với Spring Validation
- **Exception handling** tùy chỉnh

### Quản lý công việc
- **CRUD Operations** đầy đủ cho tasks
- **Phân loại công việc** theo categories có màu sắc
- **Mức độ ưu tiên** (Low, Medium, High, Urgent)
- **Đánh dấu hoàn thành** tasks
- **Lọc tasks** theo category và trạng thái
- **Giao diện thân thiện** và responsive

### Trải nghiệm người dùng
- **Thiết kế Responsive** hoạt động trên mọi thiết bị
- **Giao diện trực quan** với Material Design
- **Cập nhật thời gian thực** không cần tải lại trang
- **Lọc nâng cao** theo trạng thái, danh mục
- **AJAX fetch API** từ backend

## 🏗️ Kiến trúc hệ thống

### Backend (Spring Boot)
```
src/main/java/org/project/backend/hubt/todo_list/
├── config/          # Cấu hình Security, CORS
├── controller/      # REST API endpoints
├── dto/            # Data Transfer Objects
├── entity/         # JPA Entity classes (User, Task, Category)
├── exception/      # Custom exceptions và handlers
├── repository/     # Data access layer
├── security/       # JWT và security components
└── service/        # Business logic layer
```

### Frontend (HTML/CSS/JS)
```
frontend/
├── css/            # Stylesheets (style.css)
├── js/             # JavaScript modules (app.js, auth.js)
├── index.html      # Trang dashboard chính
├── login.html      # Trang đăng nhập
└── register.html   # Trang đăng ký
```

### Database Schema
```
┌─────────────┐    ┌──────────────┐    ┌─────────────┐
│    users    │    │ categories   │    │    tasks    │
├─────────────┤    ├──────────────┤    ├─────────────┤
│ id (PK)     │───┐│ id (PK)      │───┐│ id (PK)     │
│ username    │   ││ name         │   ││ title       │
│ email       │   ││ description  │   ││ description │
│ password    │   ││ color_code   │   ││ is_completed│
│ is_active   │   ││ user_id (FK) │◄──┤│ priority    │
│ created_at  │   │└──────────────┘   ││ due_date    │
│ updated_at  │   │                   ││ completed_at│
└─────────────┘   │                   ││ user_id (FK)│◄─┘
                  │                   ││ category_id │
                  │                   ││ created_at  │
                  │                   ││ updated_at  │
                  │                   │└─────────────┘
                  └───────────────────┘
```

## 🚀 Hướng dẫn chạy dự án

### Yêu cầu hệ thống
- Java 17 hoặc cao hơn
- Maven 3.6+
- MySQL 8.0+
- Trình duyệt web hiện đại

### Cài đặt và chạy

1. **Tải về dự án**
   ```bash
   git clone <repository-url>
   cd Todo_List
   ```

2. **Thiết lập MySQL Database**
   - Tạo database mới tên `todo_db`
   - Hoặc sử dụng cấu hình `createDatabaseIfNotExist=true` trong application.properties

3. **Cấu hình Database**
   Cập nhật `src/main/resources/application.properties`:
   ```properties
   # Database Configuration
   spring.datasource.url=jdbc:mysql://localhost:3306/todo_db
   spring.datasource.username=root
   spring.datasource.password=your_mysql_password

   # JWT Configuration
   app.jwt.secret=todoSecretKeyForJWTTokenGeneration
   app.jwt.expiration=86400000
   ```

4. **Load sample data (tùy chọn)**
   ```bash
   mysql -u root -p01022005 todolist_db
   ```

5. **Chạy ứng dụng**
   ```bash
   ./mvnw spring-boot:run
   ```
   Hoặc nếu có Maven:
   ```bash
   mvn spring-boot:run
   ```

6. **Truy cập ứng dụng**
   - Mở trình duyệt và truy cập: `http://localhost:8080`
   - Truy cập trang đăng ký: `http://localhost:8080/register.html`
   - Truy cập trang đăng nhập: `http://localhost:8080/login.html`

### Test Account
**Sử dụng sample data:**
- **Username**: testuser
- **Email**: test@example.com
- **Password**: password123

**Hoặc đăng ký tài khoản mới:**
- Click "Register here" trên trang login
- Điền thông tin và tạo tài khoản mới

## 📚 API Documentation

### Authentication Endpoints
| Method | Endpoint | Mô tả |
|--------|----------|-------------|
| POST | `/api/auth/register` | Đăng ký người dùng mới |
| POST | `/api/auth/login` | Đăng nhập |

### Task Management Endpoints
| Method | Endpoint | Mô tả |
|--------|----------|-------------|
| GET | `/api/tasks` | Lấy danh sách tasks của user |
| GET | `/api/tasks?categoryId={id}` | Lọc tasks theo category |
| GET | `/api/tasks?isCompleted={true/false}` | Lọc tasks theo trạng thái |
| POST | `/api/tasks` | Tạo task mới |
| PUT | `/api/tasks/{id}` | Cập nhật task |
| PATCH | `/api/tasks/{id}/toggle` | Đổi trạng thái hoàn thành |
| DELETE | `/api/tasks/{id}` | Xóa task |

### Category Management Endpoints
| Method | Endpoint | Mô tả |
|--------|----------|-------------|
| GET | `/api/categories` | Lấy danh sách categories của user |
| POST | `/api/categories` | Tạo category mới |
| PUT | `/api/categories/{id}` | Cập nhật category |
| DELETE | `/api/categories/{id}` | Xóa category |

### Ví dụ API Request/Response

**Đăng ký user:**
```bash
POST /api/auth/register
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "password123"
}
```

**Tạo task mới:**
```bash
POST /api/tasks
Authorization: Bearer {token}
{
  "title": "Hoàn thành dự án",
  "description": "Viết code và test",
  "priority": "HIGH",
  "categoryId": 1,
  "dueDate": "2024-12-31T23:59:59"
}
```

## 🔧 Công nghệ sử dụng

### Backend Technologies
- **Java 17** - Ngôn ngữ lập trình
- **Spring Boot 3.5.6** - Framework ứng dụng
- **Spring Security** - Xác thực và phân quyền
- **Spring Data JPA** - Persistence layer
- **MySQL 8.0** - Cơ sở dữ liệu chính
- **JWT (JSON Web Tokens)** - Xác thực stateless
- **Maven** - Quản lý dependencies
- **Lombok** - Giảm boilerplate code

### Frontend Technologies
- **HTML5** - Markup language
- **CSS3** - Styling với modern features
- **Vanilla JavaScript** - Client-side logic
- **Fetch API** - HTTP requests
- **Local Storage** - Session management phía client

### Security Features
- **BCrypt** - Password hashing
- **JWT** - Token-based authentication
- **CORS** - Cross-origin resource sharing
- **Input Validation** - Validation server và client
- **SQL Injection Protection** - JPA/Hibernate ORM
- **Exception Handling** - Custom exceptions

## 📱 Giao diện người dùng

### Trang đăng nhập (login.html)
- Thiết kế clean, modern với gradient background
- Form đăng nhập với username/email và password
- Validation và error handling
- Chuyển hướng tự động sau khi đăng nhập thành công

### Trang đăng ký (register.html)
- Form đăng ký với validation real-time
- Kiểm tra password confirmation
- Error messages chi tiết
- Tự động đăng nhập sau khi đăng ký thành công

### Dashboard chính (index.html)
- **Task Board Layout** với 3 cột: To Do, In Progress, Completed
- **Drag & Drop** - Kéo thả tasks giữa các cột để thay đổi trạng thái
- **Glass Morphism Design** - Hiệu ứng kính mờ hiện đại
- **Gradient Backgrounds** - Màu sắc gradient đẹp mắt
- Form thêm task mới với đầy đủ fields (title, description, priority, category, due date)
- **Real-time filtering** theo category và status
- **Modal quản lý categories** với color picker
- **Responsive design** hoạt động tốt trên mobile

### Cách sử dụng Drag & Drop
1. **Kéo task từ To Do sang Completed**: Tự động đánh dấu task hoàn thành
2. **Kéo task từ Completed về To Do**: Tự động đánh dấu task chưa hoàn thành
3. **Visual feedback**: Task sẽ có animation khi kéo và drop zone sẽ highlight
4. **Touch support**: Hoạt động trên thiết bị cảm ứng

## 🧪 Kiểm thử

### Checklist kiểm thử thủ công
- [ ] **Authentication**: Đăng ký user mới, đăng nhập/đăng xuất
- [ ] **Task Management**: CRUD operations cho tasks với đầy đủ fields
- [ ] **Category Management**: CRUD operations cho categories với color picker
- [ ] **Drag & Drop**: Kéo thả tasks giữa các cột To Do ↔ Completed
- [ ] **Filtering**: Lọc tasks theo category và completion status
- [ ] **Responsive Design**: Test trên desktop, tablet, mobile
- [ ] **JWT Authentication**: Token persistence, auto logout khi expired
- [ ] **Glass Morphism UI**: Kiểm tra hiệu ứng visual trên các browser
- [ ] **Real-time Updates**: Tasks update ngay lập tức sau actions

### Các tình huống test
1. **Authentication Flow**
   - Đăng ký → Đăng nhập → Dashboard
   - Logout và login lại
   - Token expiration handling

2. **Task Management**
   - Tạo tasks với priorities khác nhau
   - Cập nhật task status và details
   - Xóa tasks và categories
   - Filter tasks theo nhiều tiêu chí

3. **Data Persistence**
   - Logout và login để verify data
   - Refresh browser scenarios
   - Cross-browser compatibility

---

**Todo List Website** - Giải pháp quản lý công việc cá nhân hoàn chỉnh với công nghệ web hiện đại.
