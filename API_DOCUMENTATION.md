# 讯极日历 (SynJi AI Calendar) 全面接口技术文档 (v1.3)

## 1. 基础约定

### 1.1 服务地址
- **局域网调试地址**: `http://192.168.0.105:8080`
- **模拟器访问地址**: `http://10.0.2.2:8080`

### 1.2 通用响应格式
所有接口均返回 JSON 格式，标准结构如下：
```json
{
  "code": 200,       // 状态码：200-成功，400-参数错误，401-未授权，500-服务器异常
  "message": "描述", // 状态描述信息
  "data": null       // 业务数据，可为对象、数组或空
}
```

### 1.3 数据格式规范
- **日期**: `yyyy-MM-dd` (例如: `2024-01-23`)
- **时间**: `HH:mm:ss` (例如: `10:30:00`)
- **完整时间**: `yyyy-MM-dd HH:mm:ss` (例如: `2024-01-23 10:30:00`)
- **字符编码**: `UTF-8`

### 1.4 安全鉴权
- 除“发送验证码”和“登录”接口外，其余所有接口均需在 **HTTP Header** 中携带：
  - `Authorization`: `登录返回的Token字符串`

---

## 2. 认证与用户模块 (Auth & User)

### 2.1 发送短信验证码
用于验证码登录/注册前的身份验证。
- **路径**: `/api/auth/send-code`
- **方法**: `POST`
- **请求体**:
```json
{ "phoneNumber": "13800138000" }
```
- **后端逻辑**: 生成6位随机码，存入数据库 (过期时间5分钟)，并将验证码打印在后端控制台 (Console) 供调试。

### 2.2 手机号验证码登录/注册
- **路径**: `/api/auth/login`
- **方法**: `POST`
- **请求体**:
```json
{
  "phoneNumber": "13800138000",
  "verifyCode": "123456"
}
```
- **响应数据 (`data`)**:
```json
{
  "token": "mock-token-10001-1706000000000",
  "user": {
    "userId": "10001",
    "phoneNumber": "13800138000",
    "nickname": "新用户8000",
    "isNewUser": true
  }
}
```

### 2.3 获取用户信息
用于自动登录检查或刷新资料。
- **路径**: `/api/user/info`
- **方法**: `GET`
- **Header**: `Authorization: <Token>`
- **响应数据 (`data`)**:
```json
{
  "userId": "10001",
  "phoneNumber": "13800138000",
  "nickname": "新用户8000",
  "isNewUser": false
}
```

---

## 3. 日程管理模块 (Schedule)

### 3.1 获取日程列表
获取当前用户的所有日程。
- **路径**: `/api/schedule/list`
- **方法**: `GET`
- **Header**: `Authorization: <Token>`
- **响应数据 (`data`)**:
```json
[
  {
    "id": 1,
    "title": "项目启动会",
    "date": "2024-01-23",
    "time": "09:00:00",
    "isAllDay": false,
    "location": "会议室3",
    "belonging": "工作",
    "important": true,
    "notes": "带上笔记本电脑",
    "isAiGenerated": true,  // 是否由AI自动提取生成
    "isViewed": false,      // 用户是否已在“添加记录”页点击查看过
    "createdAt": "2024-01-22 14:30:00" // [新增] 创建时间，用于前端排序
  }
]
```

### 3.2 新增日程
- **路径**: `/api/schedule/add`
- **方法**: `POST`
- **Header**: `Authorization: <Token>`
- **请求体**:
```json
{
  "title": "买菜",           // 必填
  "date": "2024-01-24",      // 必填，格式 yyyy-MM-dd
  "time": "18:00:00",        // 选填，格式 HH:mm:ss (默认为 00:00:00)
  "isAllDay": false,         // 选填 (默认为 false)
  "location": "超市",        // 选填
  "belonging": "生活",       // 选填 (默认为 "默认")
  "important": false,        // 选填，布尔值：是否重要
  "notes": "记得带购物袋",      // 选填，长文本备注
  "isAiGenerated": true,     // 必填：通过识别添加时传 true，手动添加时传 false
  "isViewed": false          // 必填：通过识别添加时传 false，手动添加时传 true
}
```

### 3.3 智能 AI 解析日程 (OCR -> AI)
该接口接收由客户端本地 OCR 提取的原始文本，通过 LLM 语义解析，返回结构化的日程对象列表。
- **路径**: `/api/schedule/ai-parse`
- **方法**: `POST`
- **Header**: `Authorization: <Token>`
- **请求体**:
```json
{
  "text": "明天下午两点在沃尔玛二楼开会，记得带电脑。另外后天晚上8点有个聚餐。"
}
```
- **响应数据 (`data`)**: 返回一个 `ScheduleExtractionData` 对象数组（即使只有一个日程，也返回数组）。
```json
[
  {
    "title": "沃尔玛二楼开会",
    "date": "2024-01-24",
    "time": "14:00:00",
    "isAllDay": false,
    "location": "沃尔玛二楼",
    "notes": "记得带电脑",
    "important": false
  },
  {
    "title": "聚餐",
    "date": "2024-01-25",
    "time": "20:00:00",
    "isAllDay": false,
    "location": null,
    "notes": null,
    "important": false
  }
]
```
- **注意**: 
  1. 如果 AI 解析结果中没有明确时间，`time` 字段将默认返回 `00:00:00`，以避免客户端空指针异常。
  2. 客户端应遍历返回的数组，并为每个日程对象调用 `/api/schedule/add` 接口进行保存，或者在前端提供批量确认界面。
  3. 返回的对象结构与 `Schedule` 实体略有不同（不包含 `id`, `userId`, `belonging` 等后端字段），仅包含 AI 提取的信息。

### 3.4 修改日程 / 标记已读
- **路径**: `/api/schedule/update`
- **方法**: `PUT`
- **Header**: `Authorization: <Token>`
- **请求体**: (同 3.2，但需包含 `id`)
- **说明**: 客户端点击带红点的日程时，会发送 `isViewed: true` 来消除红点。

### 3.5 删除日程
- **路径**: `/api/schedule/delete/{id}`
- **方法**: `DELETE`
- **Header**: `Authorization: <Token>`
- **URL参数**: `id` 为日程的长整型唯一标识。

---

## 4. 后端逻辑变更说明

1. **数据库字段**:
   - `is_ai_generated` (boolean): 标识日程来源。
   - `is_viewed` (boolean): 标识用户是否已查看自动生成的日程。
   - `created_at` (datetime): 记录创建时间，用于前端排序。
2. **逻辑判断**:
   - 前端“添加日程记录”页面仅显示 `isAiGenerated == true` 的记录。
   - 红点显示条件：`isAiGenerated == true && isViewed == false`。
   - 排序规则：前端可根据 `createdAt` 字段进行降序排列（最新添加的在最前）。
