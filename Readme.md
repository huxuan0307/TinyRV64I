# 参与一生一芯期间所做的小项目

## 使用`scala`和`chisel3`实现CPU核心
+ 实现RV64I指令集
+ 完成cpu-test测试

## TODO
+ 实现特权级指令
+ 流水线化
+ 移植RT-Thread操作系统

## Tips
+ 如何将`ChiselGeneratorAnnotation`生成的verilog放在指定的文件夹中，而不是根目录
  + 在运行TopMain是加上参数`--target-dir %outdir%`
  + 例如，我希望输出目录是`build/` 那么添加参数`--target-dir build`即可
    