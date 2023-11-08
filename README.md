# 校园论坛小程序用户端后端

## 项目简介

项目基于`Jdk17`开发，整体分为四大模块

- `bbs-generate`：代码生成器，自动生成实体类和Mapper代码
- `bbs-base`：被所有模块所依赖的基础模块，包含实体类、通用接口、`Mybatis`类型转换器等

- `bbs-content`：内容模块，为前端提供服务，暴露前端所需要的接口
- `bbs-task`：任务模块，包含异步任务和需要定时运行的任务

使用到的技术栈

| --         | --                                                  |
| ---------- | --------------------------------------------------- |
| 后端框架   | SpringBoot 3.1.0                                    |
| 权限认证   | SpringSecurity    SpringSecurityOAth2ResourseServer |
| 数据校验   | SpringBootValidation                                |
| 持久层框架 | Mybatis-flex 1.7.0                                  |
| Http请求   | Okhttp3                                             |
| 接口文档   | Springdoc-openapi 2.1.0                             |
| 数据库     | Postgresql                                          |
| 消息队列   | RabbitMQ                                            |
| 任务平台   | PowerJob                                            |

**后端整体架构图**

![Screenshot_20231026_094327](https://image.qiniu.vnc.ink/share/1.png)

## 如何使用

### 内容模块

内容模块的环境依赖有`Postgresql`和`RabbitMQ`需要提前部署，另外还需要配置`七牛云Bucket`和微信小程序`appid、secret`。下面给出使用`Docker`部署`Postgresql`和`RabbitMQ`的`docker-compose.yml`文件示例。

```yaml
version: '3.1'
services:
  # postgresql
  postgresql:
    container_name: postgresql
    image: postgres
    restart: always
    environment:
      POSTGRES_PASSWORD: admin
    ports:
      - 5432:5432
  # rabbitmq
  rabbitmq:
    container_name: rabbitmq
    restart: always
    image: rabbitmq:3-management
    environment:
      RABBITMQ_DEFAULT_USER: root
      RABBITMQ_DEFAULT_PASS: admin
    ports:
      - 5672:5672
      - 15672:15672
```

```bash
# 启动
docker-compose up -d
```

数据库启动成功后，创建`school_bbs`数据库，运行仓库中`database/postgresql.sql`文件生成数据表。

然后需要修改`application-{dev}.yml`配置文件，替换对应的配置项，所有功能均实现自动配置。

```yaml
spring:
  # postgresql数据库配置
  datasource:
    driver-class-name: org.postgresql.Driver
    url: 'jdbc:postgresql://10.10.10.10:5432/school_bbs'
    username: postgres
    password: admin
  # rabbitmq配置
  rabbitmq:
    host: 10.10.10.10
    port: 5672
    virtual-host: /
    username: root
    password: admin
# 微信小程序配置
wechat:
  appid: wxb4a5026fc9990e21
  secret: 9d68af054cf1f3ecb29c23398a9a06e7
# 本地敏感词典路径配置 支持 classpath: 路径
trie:
  path: /home/lisang/Documents/words_lines.txt
# jwt Token配置
jwt:
  # Token的有效时间 单位：小时
  expire-hours: 48
  audience: school_bbs
  issuer: school_bbs
  # 是否使用随机公私钥 使用随机密钥每次服务重启后，之前颁发的Token自动失效
  random-key: false
  # 配置加密、解密Token的公私钥路径 避免服务重启后Token失效
  public-key-path: classpath:certs/public.pem
  private-key-path: classpath:certs/private.pem
# 七牛云配置
qiniu:
  access-key: '7ljXDMVJ50r9QHHvqqLT191IWDn8qy3bU3gCwMdY'
  secret-key: 'CymD_ID_-__AiYbnHbUkIMe1FTFXCnfC52NVEZG2'
  # bucket名称
  bucket: 'img-sr'
  # bucket域名，官方随机域名和自定义域名均可
  bucket-domain: 'https://image.qiniu.vnc.ink/'
  
# solr配置
solr:
  # solr地址
  address: 'http://10.10.10.12:8080/solr'
```

自动配置类，一般无需干预

```java
/**
 * @Author: lisang
 * @DateTime: 2023-10-13 20:14:44
 * @Description: 七牛云上传自动配置类
 */
@Configuration
@ConditionalOnClass(Auth.class)
@EnableConfigurationProperties(QiniuProperties.class)
@Import({ QiniuConfigurations.QiniuAuthConfiguration.class, QiniuConfigurations.QiniuServiceConfiguration.class })
public class QiniuAutoConfiguration {
}
```

项目启动成功后，监听端口为`8040`，访问[http://localhost:8040/swagger-ui/index.html](http://localhost:8040/swagger-ui/index.html)即可查看内容模块的接口文档。

前端访问后端接口时需要先使用微信登录的临时`Code`调用后端登录接口拿到访问`Token`，而后将`Token`放到请求头的`Authorization`字段（具体格式为`Authorization: Bearer {Token}`），再请求后端接口。如果未携带有效`Token`信息或者`Token`无效，后端直接返回`401`错误，无报错信息。

后端的返回响应码有5种：

- 200：请求成功
- 401：无权限或未认证
- 400：请求参数错误
- 404：请求的资源找不到，常见于请求某个详情时，资源找不到
- 500：后端内部错误

> 额外说明：
>
> 本地的敏感词典会在用户发布帖子/话题、发布评论时对文本内容进行初步匹配（基于Trie字典树实现），匹配通过保存数据库，然后再调用审核平台审核。如果匹配到敏感词会将匹配到的敏感词通过错误消息返回给前端。
>
> 视频上传接口响应时间可能非常久，因为需要从请求中读取输入流再上传到七牛云，前端需要加长请求超时时间
> 
> Solr中的索引会在帖子/话题被删除时同步删除，在帖子/话题审核通过后添加到索引库中。调用Solr进行搜索时，如果调用失败则会改用降级逻辑，直接使用数据库查询。

### 任务模块

任务模块除了依赖内容模块的`Postgresql`和`RabbitMQ`，还依赖`PwoerJob`任务平台和`Redis`。同样也需要修改讯飞自然语言处理和百度智能平台的配置。给出`Docker`部署`PowerJob`和`Redis`的`docker-compose.yml`文件示例。

```yaml
version: '3.1'
services: 
  # powerjob数据库
  powerjob-mysql:
    environment:
      MYSQL_ROOT_HOST: "%"
      MYSQL_ROOT_PASSWORD: No1Bug2Please3!
    restart: always
    container_name: powerjob-mysql
    image: powerjob/powerjob-mysql:latest
    ports:
      - "3308:3306"
    volumes:
      - ./powerjob/powerjob-mysql:/var/lib/mysql
    command: --lower_case_table_names=1
  # powerjob调度平台
  powerjob-server:
    container_name: powerjob-server
    image: powerjob/powerjob-server:latest
    restart: always
    depends_on:
      - powerjob-mysql
    environment:
      JVMOPTIONS: "-Xmx256m"
      PARAMS: "--oms.mongodb.enable=false --spring.datasource.core.jdbc-url=jdbc:mysql://localhost:3308/powerjob-daily?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai"
    network_mode: host
    volumes:
      - ./powerjob/powerjob-server:/root/powerjob/server/
  # redis
  redis:
    container_name: redis
    image: redis/redis-stack-server
    restart: always
    ports:
      - 6379:6379
    environment:
      REDIS_ARGS: "--requirepass admin"
```

```bash
# 启动
docker-compose up -d
```

启动成功后访问[http://localhost:7700](http://localhost:770)即可访问调度平台（首次使用需注册应用程序）

然后修改`application-{env}.yml`配置文件

```yaml
spring:
  # Postgresql
  datasource:
    driver-class-name: org.postgresql.Driver
    url: 'jdbc:postgresql://10.10.10.12:5432/school_bbs'
    username: postgres
    password: admin
  # RabbitMQ
  rabbitmq:
    host: 10.10.10.12
    port: 5672
    virtual-host: /
    username: root
    password: admin
  # Redis配置
  data:
    redis:
      host: 10.10.10.12
      port: 6379
      password: admin
      database: 3
powerjob:
  worker:
    app-name: bbs-task
    server-address: 10.10.10.12:7700
    store-strategy: memory
    protocol: http
# 讯飞自然语言处理
xunfei:
  analytics:
    request-url: 'https://ltpapi.xfyun.cn/v1/ke'
    appid: '49681549'
    api-key: '919495c6d603fd43d9724e94d6056a4c'
    type: 'dependent'
analysis:
  size: 4
# 百度内容审核
baidu:
  app-id: '41596274'
  app-key: '5qPg3mUZ5CB5HSKuueEBsqaF'
  secret-key: 'acnSBKEBSBTtL98SlgpFOZqGGhg6ngKO'
  grant-type: 'client_credentials'
```

任务模块启动成功后，没有暴露`Web`端口，可以通过任务调度平台查看模块信息并添加、运行任务

![Screenshot_20231026_111425](https://image.qiniu.vnc.ink/share/2.png)

![Screenshot_20231026_111538](https://image.qiniu.vnc.ink/share/3.png)

任务添加时可以添加任务参数，该参数在任务执行器方法的上下文中可以拿到，方便定制任务运行逻辑。任务运行过程中在任务实例可以看到当前任务的实时日志。



> 额外说明：
>
> 任务模块接受到消息生成帖子关键字和审核帖子使用多线程并行执行。审核帖子时，审核文本、图像、视频也使用异步任务并行执行，形成异步任务链。由于审核任务调用时间较久，当有大量帖子或话题发布时可能存在消息积压。
>
> 如果在审核过程中某个接口调用错误会将此次审核写入到审核异常记录表，由审核异常处理定时任务进行重试，尽量减少接口调用失败对帖子审核的影响

## 打包部署

**在进行部署前需要先部署好所依赖的环境**

打包部署的方式推荐使用`Docker`，任务模块和内容模块分别使用`Maven`进行打包，得到`jar`包。然后可以使用下面示例的镜像编译文件和`docker-compose.yml`文件进行部署

内容模块的镜像编译文件

```dockerfile
# 使用自编译的jre镜像 没有可以使用openjdk
FROM debian-jre:17
ADD *.jar app.jar
EXPOSE 8040
ENTRYPOINT ["java","-jar","-Xmx256m","-Xms256m","-XX:+UseZGC","-XX:MaxGCPauseMillis=200","app.jar","--spring.profiles.active=test"]
```

任务模块的镜像编译文件

```dockerfile
FROM debian-jre:17
ADD *.jar app.jar
ENTRYPOINT ["java","-jar","-Xmx256m","-Xms256m","-XX:+UseZGC","-XX:MaxGCPauseMillis=200","app.jar","--spring.profiles.active=test"]
```

`docker-compose`部署文件

```yaml
version: '3.1'
services:
  bbs-server:
    container_name: bbs-server
    image: bbs-server:latest
    restart: always
    ports:
      - 8040:8040
  bbs-task:
    container_name: bbs-task
    image: bbs-task:latest
    restart: always
```

```bash
# 运行
docker-compose up -d
```