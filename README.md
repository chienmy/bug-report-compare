# bug-report-compare

![](https://img.shields.io/badge/language-java-blue.svg) ![](https://img.shields.io/badge/platform-linux-lightgrey.svg)

## 简介

本项目用于获取开源项目不同版本的代码，扫描漏洞生成报告并根据版本间的差异进行正误报的标记。

## 功能

- 通过Maven仓库的链接获取源代码和Jar包
- 通过Git仓库的Tag获取源代码
- 使用findbugs生成漏洞扫描报告
- 结合源代码和扫描报告进行正误报标记

## 运行环境

- Java 1.8+
- Linux（需要用到`wget`和`unzip`）

## 用法

### 对含有源代码的Maven项目的处理

运行`JarAnalyzer`可完成以下操作：

1. 输入参数：Maven仓库URL，参数要求：
   1. 该路径下包含`maven-metadata.xml`文件
   2. 路径必须以`/`结尾
2. 遍历不同版本，下载**含有源码版本**的源码包和Jar包
3. 解压源码包
4. 使用findBugs扫描不同版本的Jar包得到扫描报告
5. 比对相邻版本的源码和报告标记正误报

```
# 参数说明
--all-steps			完成所有步骤
--only-download		完成步骤1-3
```

