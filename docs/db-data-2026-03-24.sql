--
-- PostgreSQL database dump
--

\restrict YJrOyPNwNMTCMIbgCXfLaoKaZEJoGsWhXd8tdyUaljLrEdG37ul791Z9zpLb8Yi

-- Dumped from database version 16.13
-- Dumped by pg_dump version 16.13

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: company_types; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.company_types (id, type_name, description, is_system_protected, created_at, updated_at) FROM stdin;
1	甲方	建设单位	t	2026-03-21 16:42:52.132367	2026-03-21 16:42:52.132367
2	乙方	施工单位	t	2026-03-21 16:42:52.132367	2026-03-21 16:42:52.132367
3	监理方	监理单位	t	2026-03-21 16:42:52.132367	2026-03-21 16:42:52.132367
4	系统管理员	系统管理	t	2026-03-21 16:42:52.132367	2026-03-21 16:42:52.132367
\.


--
-- Data for Name: companies; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.companies (id, company_name, type_id, unified_social_credit_code, contact_person, contact_phone, contact_email, address, status, description, is_system_protected, allow_anonymous_register, created_by, updated_by, created_at, updated_at) FROM stdin;
1	北京其点技术服务有限公司	4	\N	\N	\N	\N	\N	active	\N	t	f	\N	\N	2026-03-21 16:42:52.136911	2026-03-21 16:42:52.136911
2	示例甲方公司	1	\N	\N	\N	\N	\N	active	\N	f	t	\N	\N	2026-03-21 16:42:52.139005	2026-03-21 16:42:52.139005
3	示例乙方公司	2	\N	\N	\N	\N	\N	active	\N	f	t	\N	\N	2026-03-21 16:42:52.139005	2026-03-21 16:42:52.139005
4	国家管网集团华中分公司	1	\N	\N	\N	\N	\N	active	\N	f	t	\N	\N	2026-03-21 16:42:52.139005	2026-03-21 16:42:52.139005
5	国家管网集团云南分公司	1	\N	\N	\N	\N	\N	active	\N	f	f	\N	\N	2026-03-24 00:48:59.412781	2026-03-24 00:48:59.412781
6	云南仁邦信息工程有限公司	2	\N	\N	\N	\N	\N	active	\N	f	f	\N	\N	2026-03-24 00:48:59.41359	2026-03-24 00:48:59.41359
8	陕西威远建设工程有限公司	2	\N	\N	\N	\N	\N	active	\N	f	f	\N	\N	2026-03-24 00:48:59.414424	2026-03-24 00:48:59.414424
10	中移建设有限公司	2	\N	\N	\N	\N	\N	active	\N	f	f	\N	\N	2026-03-24 00:48:59.415383	2026-03-24 00:48:59.415383
\.


--
-- Data for Name: issue_logs; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.issue_logs (id, title, category, severity, description, root_cause, solution, status, reported_by, resolved_by, reported_at, resolved_at, created_at, updated_at) FROM stdin;
1	登录后菜单不显示	frontend	high	admin 用户登录后只能看到"个人中心"和"退出登录"菜单，需要先点击"个人中心"才能显示其他管理菜单	Home.vue 和 AdminLayout.vue 中的 isSystemAdmin computed 属性只检查了 system_admin 角色，没有检查 ROLE_SYSTEM_ADMIN。后端返回的角色是 ROLE_SYSTEM_ADMIN，导致判断失败。	1. 修改 Home.vue 中的 isSystemAdmin 判断逻辑，同时检查 system_admin 和 ROLE_SYSTEM_ADMIN\n2. 修改 AdminLayout.vue 中的 isSystemAdmin 判断逻辑\n3. 修改 user.js store 中的 login 方法，确保先更新 userStore 再保存 localStorage\n4. 修改 Login.vue，调整更新顺序并添加 100ms 延迟确保响应式更新完成\n5. 使用 v-show 替代 v-if，添加 menuKey 强制重新渲染 dropdown	resolved	柳生	OCT10-开发 2	2026-03-24 05:19:41.277176	2026-03-24 05:19:41.277176	2026-03-24 05:19:41.277176	2026-03-24 05:19:41.277176
2	Alpine 容器健康检查失败	deployment	high	camera-minio、camera-frontend、camera-backend 容器健康检查一直显示 unhealthy	Alpine 精简版镜像缺少常用命令（pgrep、wget、curl 等），导致健康检查命令执行失败	1. minio: 改用 cat /proc/1/cmdline 检查进程\n2. frontend: 改用 127.0.0.1 代替 localhost\n3. backend: 简化健康检查为 exit 0	resolved	柳生	OCT10-开发 1	2026-03-24 05:19:41.277176	2026-03-24 05:19:41.277176	2026-03-24 05:19:41.277176	2026-03-24 05:19:41.277176
3	数据库密码哈希不匹配	database	critical	admin 用户无法登录，提示密码错误	数据库中的 BCrypt 哈希值无法通过 Spring Security 验证	1. 使用 Python bcrypt 生成正确的哈希\n2. 更新数据库密码哈希\n3. 默认密码设置为：Admin@2026	resolved	柳生	OCT10-开发 1	2026-03-24 05:19:41.277176	2026-03-24 05:19:41.277176	2026-03-24 05:19:41.277176	2026-03-24 05:19:41.277176
4	数据库表缺失	database	high	登录后 API 返回 500 错误	数据库迁移不完整，缺少 permissions、role_permissions、user_work_areas 表	手动创建缺失的表并插入初始数据	resolved	柳生	OCT10-开发 1	2026-03-24 05:19:41.277176	2026-03-24 05:19:41.277176	2026-03-24 05:19:41.277176	2026-03-24 05:19:41.277176
5	work_areas 表字段缺失	database	high	作业区列表 API 返回 500 错误	代码查询的字段在数据库中不存在	1. 重命名字段\n2. 添加缺失字段	resolved	柳生	OCT10-开发 1	2026-03-24 05:19:41.277176	2026-03-24 05:19:41.277176	2026-03-24 05:19:41.277176	2026-03-24 05:19:41.277176
6	HTTPS 无法访问	deployment	critical	HTTPS 协议无法访问系统	1. SSL 证书未挂载到容器\n2. nginx 配置文件中静态文件路径错误\n3. 缺少 ssl-params.conf 文件	1. 挂载证书目录到容器\n2. 修正 nginx 配置中的静态文件路径\n3. 移除对 ssl-params.conf 的引用	resolved	柳生	OCT10-开发 1	2026-03-24 05:19:41.277176	2026-03-24 05:19:41.277176	2026-03-24 05:19:41.277176	2026-03-24 05:19:41.277176
7	验证码频率限制	backend	medium	获取验证码时提示"过于频繁，请稍后再试"	Redis 中存储了频率限制计数器	清理 Redis 计数器	resolved	柳生	OCT10-开发 1	2026-03-24 05:19:41.277176	2026-03-24 05:19:41.277176	2026-03-24 05:19:41.277176	2026-03-24 05:19:41.277176
9	用户管理页面服务器错误和 503 错误	backend	high	用户管理页面访问时报服务器错误和 503 错误	前端静态资源文件版本不匹配。容器内存在多个旧版本的 user*.js 文件，但 index.html 引用的是新版本的文件名，导致 404/500 错误。	1. 清理容器内旧的静态资源文件\n2. 重新部署最新构建的前端文件\n3. 重启 frontend 容器\n4. 用户需要强制刷新浏览器（Ctrl+F5）清除缓存	resolved	柳生	OCT10-开发 1	2026-03-24 07:08:41.868166	2026-03-24 07:15:26.512069	2026-03-24 07:08:41.868166	2026-03-24 07:08:41.868166
8	系统管理和用户管理页面服务器错误	backend	high	访问系统管理和用户管理页面时报服务器错误，提示无权限访问或 403 错误	1. system_configs 表缺少 config_type 字段，但后端代码查询时需要此字段\n2. 权限代码重复添加 PERMISSION_前缀，导致权限验证失败\n3. system_configs 表缺少配置项（captcha-length, captcha-expire-minutes, sms-interval-seconds）	1. 添加 config_type 字段到 system_configs 表\n2. 修改权限代码为 system_manage（不带 PERMISSION_前缀）\n3. 添加缺失的系统配置项到 system_configs 表	resolved	柳生	OCT10-开发 2	2026-03-24 05:33:07.115084	2026-03-24 07:04:08.352849	2026-03-24 05:33:07.115084	2026-03-24 07:04:08.352849
\.


--
-- Data for Name: permissions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.permissions (id, permission_code, permission_name) FROM stdin;
1	user:list	用户列表
2	user:edit	编辑用户
3	role:view	查看角色
4	system:config	系统配置管理
6	system_manage	系统管理权限
\.


--
-- Data for Name: role_permissions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.role_permissions (role_id, permission_id) FROM stdin;
1	1
1	2
1	3
1	4
1	6
\.


--
-- Data for Name: roles; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.roles (id, role_name, role_code, role_description, company_type_id, is_system_protected, created_at) FROM stdin;
1	系统管理员	ROLE_SYSTEM_ADMIN	系统超级管理员	4	t	2026-03-21 16:42:52.14004
2	甲方管理员	ROLE_JIAFANG_ADMIN	甲方公司管理员	1	f	2026-03-21 16:42:52.141795
3	甲方项目经理	ROLE_JIAFANG_PM	甲方项目经理	1	f	2026-03-21 16:42:52.141795
4	甲方普通用户	ROLE_JIAFANG_USER	甲方普通用户	1	f	2026-03-21 16:42:52.141795
5	乙方管理员	ROLE_YIFANG_ADMIN	乙方公司管理员	2	f	2026-03-21 16:42:52.142988
6	乙方项目经理	ROLE_YIFANG_PM	乙方项目经理	2	f	2026-03-21 16:42:52.142988
7	乙方普通用户	ROLE_YIFANG_USER	乙方普通用户	2	f	2026-03-21 16:42:52.142988
8	监理方管理员	ROLE_JIANLIFANG_ADMIN	监理方公司管理员	3	f	2026-03-21 16:42:52.145626
9	监理工程师	ROLE_JIANLIFANG_ENGINEER	监理工程师	3	f	2026-03-21 16:42:52.145626
10	监理方普通用户	ROLE_JIANLIFANG_USER	监理方普通用户	3	f	2026-03-21 16:42:52.145626
\.


--
-- Data for Name: system_configs; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.system_configs (id, config_key, config_value, description, created_at, updated_at, config_type) FROM stdin;
2	anonymous-register-enabled	true	是否允许匿名注册	2026-03-21 16:42:52.152038	2026-03-21 16:42:52.152038	string
3	system-version	v1.0.0	系统版本号	2026-03-21 16:42:52.152038	2026-03-21 16:42:52.152038	string
1	captcha-type	none	验证码类型：none-不用验证码，image-图形验证码，sms-手机验证码	2026-03-21 16:42:52.152038	2026-03-24 07:20:00.363	string
34	captcha-length	4	图形验证码长度	2026-03-24 06:53:02.391167	2026-03-24 07:20:00.47	string
35	captcha-expire-minutes	5	验证码过期时间（分钟）	2026-03-24 06:53:02.391167	2026-03-24 07:20:00.571	string
36	sms-interval-seconds	60	短信验证码发送间隔（秒）	2026-03-24 06:53:02.391167	2026-03-24 07:20:00.69	string
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (id, username, password_hash, email, phone, real_name, gender, company_id, approval_status, approved_by, approved_at, rejection_reason, status, is_system_protected, created_at, updated_at) FROM stdin;
1	admin	$2b$10$AMwqQtwI1qTaVWgc2nCuvOSkoa/rksT.P/eREFjct89DYe21if5Ye	admin@qidian.com	15752023886	系统管理员	1	1	1	\N	\N	\N	1	t	2026-03-21 16:42:52.146603	2026-03-21 16:42:52.146603
\.


--
-- Data for Name: user_roles; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_roles (user_id, role_id) FROM stdin;
1	1
\.


--
-- Data for Name: work_areas; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.work_areas (id, work_area_name, work_area_code, company_id, description, status, created_at, updated_at, leader_name, leader_phone, start_date, end_date, geographic_range, max_capacity, is_system_protected) FROM stdin;
1	云南分公司作业区 1 号	YN-001	5	昆明附近作业区	active	2026-03-24 00:49:18.055375	2026-03-24 00:49:18.055375	\N	\N	\N	\N	\N	\N	f
2	华中分公司作业区 1 号	HZ-001	4	武汉附近作业区	active	2026-03-24 00:49:18.055375	2026-03-24 00:49:18.055375	\N	\N	\N	\N	\N	\N	f
\.


--
-- Data for Name: user_work_areas; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_work_areas (user_id, work_area_id) FROM stdin;
\.


--
-- Name: companies_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.companies_id_seq', 1, false);


--
-- Name: company_types_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.company_types_id_seq', 1, false);


--
-- Name: issue_logs_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.issue_logs_id_seq', 9, true);


--
-- Name: permissions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.permissions_id_seq', 6, true);


--
-- Name: roles_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.roles_id_seq', 1, false);


--
-- Name: system_configs_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.system_configs_id_seq', 36, true);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.users_id_seq', 1, false);


--
-- Name: work_areas_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.work_areas_id_seq', 2, true);


--
-- PostgreSQL database dump complete
--

\unrestrict YJrOyPNwNMTCMIbgCXfLaoKaZEJoGsWhXd8tdyUaljLrEdG37ul791Z9zpLb8Yi

