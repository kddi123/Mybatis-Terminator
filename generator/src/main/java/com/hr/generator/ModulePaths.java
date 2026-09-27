package com.hr.generator;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 定位 generator 模块根目录，兼容多种启动方式：
 * - 工作目录是项目根目录 ftl_vm_fxml/
 * - 工作目录是 generator/ 子模块
 * - IDEA 从 classpath 资源反推（classes 在 generator/target/classes）
 */
public final class ModulePaths {

  private ModulePaths() {}

  /** 返回 generator 模块根目录（包含 src/main/resources 的目录）。 */
  public static Path generatorModuleRoot() {
    Path cwd = Paths.get("").toAbsolutePath().normalize();

    // 情况 1：工作目录是项目根目录，generator 是其子目录
    Path candidate = cwd.resolve("generator/src/main/resources");
    if (Files.isDirectory(candidate)) {
      return cwd.resolve("generator").normalize();
    }

    // 情况 2：工作目录已经是 generator/
    candidate = cwd.resolve("src/main/resources");
    if (Files.isDirectory(candidate)) {
      return cwd;
    }

    // 情况 3：从 classpath 资源反推（适用于 IDEA 任意 working directory）
    URL url = ModulePaths.class.getResource("/FXML/Index.fxml");
    if (url != null && "file".equalsIgnoreCase(url.getProtocol())) {
      try {
        Path resource = Paths.get(url.toURI()).toAbsolutePath().normalize();
        // resource 形如 .../generator/target/classes/FXML/Index.fxml
        Path moduleRoot = resource.getParent().getParent().getParent().getParent();
        if (Files.isDirectory(moduleRoot.resolve("src/main/resources"))) {
          return moduleRoot.normalize();
        }
      } catch (Exception ignored) {
        // 回退到抛异常
      }
    }

    throw new IllegalStateException(
        "无法定位 generator 模块根目录。请在 IDEA 运行配置中将 Working directory 设为 E:\\Sourcecodedirectory\\ftl_vm_fxml\\generator，"
            + "或在命令行 cd 到 generator/ 目录后再运行。");
  }
}
