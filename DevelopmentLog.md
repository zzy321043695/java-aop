# 开发日志

- 2022.10.11
  - 工作：初始化项目，third-part用来模拟平时开发中引入的第三方包（不可以直接修改其源代码）
  - 下一步计划：引入gradle transform来将第三方的函数调用修改成自定义的函数调用
- 2022.10.12 
  - 工作：引入神策的gradle transform编译插件和编译辅助类，支持增量编译和多线程编译。新增hook目录用于模拟开发中自定义hook方法
  - 神策插件：https://github.com/sensorsdata/sa-sdk-android-plugin2
  - 下一步计划：引入注解，编译期处理注解然后进行代码hook