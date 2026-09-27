package com.hr.generator;

import javafx.application.Application;

/**
 * 独立启动器。
 *
 * 背景：main 所在类若直接继承 javafx.application.Application，且 JavaFX jar 位于
 * classpath（而非 module-path，IDEA 默认非模块化运行即如此），JVM 启动时会直接报
 * “缺少 JavaFX 运行时组件”。本类不继承 Application，通过 Application.launch 间接启动，
 * 规避该检查 —— 因此在 IDEA 中直接右键运行本类即可，无需任何 VM 参数。
 *
 * 命令行等价方式：mvn javafx:run（javafx-maven-plugin 会把 JavaFX 放到 module-path）。
 */
public final class GeneratorLauncher {

  private GeneratorLauncher() {}

  public static void main(String[] args) {
    Application.launch(GeneratorApplication.class, args);
  }
}
