# 演唱会视频分享平台 - 常见问题FAQ

## 📋 目录
- [环境配置问题](#环境配置问题)
- [后端启动问题](#后端启动问题)
- [前端启动问题](#前端启动问题)
- [功能使用问题](#功能使用问题)
- [开发相关问题](#开发相关问题)

## 🔧 环境配置问题

### Q1: 如何检查Java版本？
```bash
java -version
```
确保版本为JDK 17或更高。如果版本过低，请下载安装最新的JDK。

### Q2: Maven下载依赖很慢怎么办？
**解决方案**：配置国内镜像源

编辑 `~/.m2/settings.xml`（如果没有则创建）：
```xml
<mirrors>
  <mirror>
    <id>aliyun</id>
    <mirrorOf>central</mirrorOf>
    <name>Aliyun Maven</name>
    <url>https://maven.aliyun.com/repository/public</url>
  </mirror>
</mirrors>
```

### Q3: npm安装依赖失败怎么办？
**解决方案1**：使用国内镜像
```bash
npm config set registry https://registry.npmmirror.com
npm install
```

**解决方案2**：清理缓存重试
```bash
npm cache clean --force
rm -rf node_modules package-lock.json
npm install
```

**解决方案3**：使用安装脚本
```bash
# Windows
.\install-dependencies.bat
```

## 🚀 后端启动问题

### Q4: 后端启动报错"数据库连接失败"
**可能原因**：
1. MySQL服务未启动
2. 数据库配置错误
3. 数据库不存在

**解决方案**：
```bash
# 1. 检查MySQL服务
# Windows: 服务管理器中查看MySQL80服务状态
# 或命令行：
sc query MySQL80

# 2. 检查数据库是否存在
mysql -u root -p
SHOW DATABASES;

# 3. 如果数据库不存在，创建并导入
CREATE DATABASE cvs_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cvs_db;
source D:/BaiduSyncdisk/university/毕业/毕业设计/CVS-system/CVS-port/init.sql;

# 4. 检查application.properties配置
# 确保用户名密码正确
```

### Q5: 后端启动报错"端口8080被占用"
**解决方案**：

**方式一**：修改端口
编辑 `application.properties`：
```properties
server.port=8081
```

**方式二**：关闭占用端口的进程
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <进程ID> /F
```

### Q6: 后端启动报错"找不到Mapper"
**可能原因**：MyBatis配置问题

**解决方案**：
1. 检查启动类是否有 `@MapperScan` 注解
2. 检查Mapper接口是否在正确的包路径下
3. 清理重新编译：`mvn clean install`

### Q6.1: 后端启动报错"Unable to find a single main class"
**错误信息**：
```
Unable to find a single main class from the following candidates [...]
```

**原因**：项目中存在多个主启动类

**解决方案**：
1. 删除多余的启动类，只保留一个
2. 或在pom.xml中指定主类：
```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <mainClass>cn.edu.seig.fnds.CvsPortApplication</mainClass>
    </configuration>
</plugin>
```

### Q7: 后端启动报错"Invalid value type for attribute 'factoryBeanObjectType'"
**错误信息**：
```
java.lang.IllegalArgumentException: Invalid value type for attribute 'factoryBeanObjectType': java.lang.String
```

**原因**：MyBatis Plus版本与Spring Boot 3.2.1不兼容

**解决方案**：
已修复！MyBatis Plus版本已从3.5.5升级到3.5.7。

重新运行：
```bash
cd CVS-port
mvn clean install
mvn spring-boot:run
```

### Q8: 文件上传失败
**可能原因**：
1. 上传目录不存在
2. 文件大小超限
3. 文件类型不支持

**解决方案**：
1. 确保 `uploads` 目录存在且有写权限
2. 检查 `application.properties` 中的文件大小配置：
```properties
spring.servlet.multipart.max-file-size=500MB
spring.servlet.multipart.max-request-size=500MB
```

## 🎨 前端启动问题

### Q8: 前端启动报错"Cannot find module"
**解决方案**：
```bash
# 删除node_modules重新安装
rm -rf node_modules package-lock.json
npm install
```

### Q9: 前端页面空白或报错
**可能原因**：
1. 后端服务未启动
2. API接口地址错误
3. 跨域问题

**解决方案**：
1. 确保后端服务已启动（http://localhost:8080）
2. 检查 `vite.config.js` 中的代理配置
3. 打开浏览器开发者工具查看具体错误信息

### Q10: 前端请求后端接口404
**解决方案**：
1. 检查后端服务是否正常运行
2. 检查API路径是否正确
3. 检查 `vite.config.js` 代理配置：
```javascript
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true
  }
}
```

### Q11: Element Plus组件不显示
**解决方案**：
```bash
# 重新安装Element Plus
npm install element-plus@^2.4.0
npm install @element-plus/icons-vue@^2.1.0
```

确保 `main.js` 中正确引入：
```javascript
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
app.use(ElementPlus)
```

## 💡 功能使用问题

### Q12: 注册后无法登录
**可能原因**：
1. 密码输入错误
2. 用户状态异常
3. 数据库问题

**解决方案**：
1. 检查数据库中用户记录是否正常
2. 检查用户状态字段（status应为0）
3. 尝试使用测试账号登录（admin/123456）

### Q13: 上传视频后看不到
**原因**：视频需要管理员审核

**解决方案**：
1. 使用管理员账号登录（admin/123456）
2. 访问管理后台
3. 在待审核列表中审核通过该视频

### Q14: 点赞/收藏不生效
**可能原因**：
1. 未登录
2. Token过期
3. 后端接口异常

**解决方案**：
1. 确保已登录
2. 重新登录获取新Token
3. 查看浏览器控制台和后端日志

### Q15: 评论无法发表
**检查项**：
1. 是否已登录
2. 评论内容是否为空
3. 后端接口是否正常

## 🛠️ 开发相关问题

### Q16: 如何添加新的API接口？
**步骤**：
1. 在Entity中定义实体类
2. 在Mapper中定义数据访问接口
3. 在Service中实现业务逻辑
4. 在Controller中定义REST接口
5. 在前端api目录中添加接口调用方法

### Q17: 如何修改数据库表结构？
**步骤**：
1. 修改 `init.sql` 中的建表语句
2. 修改对应的Entity实体类
3. 重新创建数据库或执行ALTER语句
4. 更新相关的Service和Controller

### Q18: 如何添加新的前端页面？
**步骤**：
1. 在 `src/views` 中创建新的Vue组件
2. 在 `main.js` 中添加路由配置
3. 在导航菜单中添加链接（如需要）
4. 实现页面逻辑和API调用

### Q19: 如何调试后端代码？
**方法**：
1. 使用IDE的Debug模式启动
2. 在关键位置设置断点
3. 查看控制台日志输出
4. 使用Postman测试API接口

### Q20: 如何调试前端代码？
**方法**：
1. 使用浏览器开发者工具（F12）
2. 在代码中添加 `console.log()`
3. 使用Vue DevTools浏览器插件
4. 查看Network标签页的网络请求

## 📊 性能优化问题

### Q21: 视频列表加载很慢
**优化方案**：
1. 使用分页查询（已实现）
2. 添加数据库索引
3. 使用缓存（Redis）
4. 图片懒加载

### Q22: 文件上传很慢
**优化方案**：
1. 使用云存储服务（OSS）
2. 实现断点续传
3. 压缩视频文件
4. 使用CDN加速

## 🔐 安全相关问题

### Q23: 如何修改管理员密码？
**方法**：
```sql
-- 直接在数据库中修改
UPDATE user SET password = '新密码' WHERE username = 'admin';
```

**注意**：生产环境应使用加密密码（BCrypt）

### Q24: Token过期时间如何设置？
当前使用简单的UUID Token，建议升级为JWT并设置过期时间。

## 📞 获取帮助

如果以上FAQ无法解决你的问题：

1. **查看日志**：
   - 后端日志：控制台输出
   - 前端日志：浏览器Console

2. **查看文档**：
   - [项目启动指南](./项目启动指南.md)
   - [API接口文档](./API接口文档.md)
   - [功能说明文档](./功能说明文档.md)

3. **检查代码**：
   - 查看相关的Controller、Service、Mapper
   - 查看前端组件和API调用

4. **搜索错误信息**：
   - 复制错误信息到搜索引擎
   - 查看Stack Overflow等技术社区

