# Fondia 项目设计文档

本目录包含 Fondia 食养智能菜谱平台的三份正式设计文档（Word 格式），内容基于当前 JeecgBoot 微服务代码库（user、cuisine、interaction、search、support、member、payment 及 Flutter App）编写。

## 文档清单

| 文件名 | 说明 | 约页数 |
|--------|------|--------|
| `Fondia_软件需求分析说明书.docx` | 功能需求、非功能需求、用例规约、数据与接口需求 | ~100 页 |
| `Fondia_软件概要设计说明书.docx` | 系统架构、微服务划分、模块概要、安全与部署 | ~100 页 |
| `Fondia_软件详细设计说明书.docx` | 类设计、接口规格、流程时序、数据库与异常设计 | ~100 页 |
| `Fondia_全栈实战课程大纲.md` | 从需求到上架的完整培训大纲（后端 + Flutter + 鸿蒙） | 1 篇 |

## 全栈实战课程大纲

详见 **[Fondia_全栈实战课程大纲.md](./Fondia_全栈实战课程大纲.md)**，覆盖：

- M1～M2：需求分析、架构设计  
- M3～M4：JeecgBoot 微服务、AI/SSE/搜索  
- M5～M6：Flutter App、HarmonyOS App（含华为账号登录）  
- M7～M10：联调、部署运维、域名 HTTPS、应用市场上架与软著  


1. **生成目录**：Word → 引用 → 目录 → 自动目录 → 更新域
2. **统一版式**：可按公司模板调整页眉、页脚、封面 Logo
3. **核对页数**：视图 → 页数（不同字体/行距略有差异）

## 重新生成

```bash
pip install python-docx
python3 generate_fondia_docs.py
```

修改 `generate_fondia_docs.py` 中 `TARGET_PAGES = 100` 可调整目标页数。

## 软著鉴别材料（PDF）

运行以下命令生成**程序鉴别材料**与**文档鉴别材料**（各 60 页 = 前 30 页 + 后 30 页）：

```bash
pip install python-docx fpdf2
python3 generate_fondia_docs.py          # 若尚未生成设计文档
python3 generate_copyright_materials.py    # 生成 PDF
```

| 输出文件 | 用途 |
|----------|------|
| `软著_程序鉴别材料_Fondia食养智能菜谱平台_前30页后30页.pdf` | 程序鉴别材料（Flutter + Java 源码，每页 50 行） |
| `软著_文档鉴别材料_需求分析说明书_前30页后30页.pdf` | 文档鉴别材料（需求分析说明书前 30 + 后 30 页） |

修改 `generate_copyright_materials.py` 顶部 `SOFTWARE_NAME`、`VERSION` 须与软著申请表一致。

## 编制信息

- 项目名称：Fondia 食养智能菜谱平台
- 文档版本：V1.0
- 生成方式：脚本自动生成 + 项目真实模块结构
